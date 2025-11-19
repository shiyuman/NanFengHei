package cn.sym.service.impl;

import cn.sym.dto.CreateOrderDTO;
import cn.sym.dto.OrderProductDTO;
import cn.sym.dto.OrderQueryDTO;
import cn.sym.entity.OrderDO;
import cn.sym.entity.ProductDO;
import cn.sym.entity.UserDO;
import cn.sym.repository.ProductMapper;
import cn.sym.common.response.RestResult;
import cn.sym.common.constant.ResultCodeConstant;
import cn.sym.repository.UserMapper;
import cn.sym.service.OrderService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import cn.sym.utils.DataSourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import cn.sym.dto.OrderExportQueryDTO;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import cn.sym.common.exception.BusinessException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import cn.sym.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import cn.sym.dto.OrderQuery;
import cn.sym.dto.OrderDTO;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.messaging.support.MessageBuilder;
import cn.sym.dto.StockDeductionMessage;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 添加订单详情相关导入
import cn.sym.entity.OrderItemDO;
import cn.sym.repository.OrderItemMapper;

// 添加读写分离相关导入
import cn.sym.config.DataSource;
import cn.sym.config.DataSourceContextHolder;

import cn.sym.entity.LimitPurchaseDO;
import cn.sym.dto.LimitPurchaseQueryDTO;
import cn.sym.service.LimitPurchaseService;

import cn.sym.entity.PreSaleTicketDO;
import cn.sym.service.PreSaleTicketService;

/**
 * 订单服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "STOCK_DEDUCTION_TOPIC", consumerGroup = "STOCK-DEDUCTION-CONSUMER")
@Component
public class OrderServiceImpl implements OrderService, RocketMQListener<StockDeductionMessage> {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final RocketMQTemplate rocketMQTemplate;
    private final OrderItemMapper orderItemMapper;
    private final LimitPurchaseService limitPurchaseService;
    private final PreSaleTicketService preSaleTicketService;

    /**
     * 创建订单
     * @param createOrderDTO 创建订单参数
     * @return 响应结果
     */
    @Override
    @DataSource()
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RestResult<String> createOrder(CreateOrderDTO createOrderDTO) {
        try {
            log.info("开始创建订单，用户ID: {}", createOrderDTO.getUserId());
            
            // 数据校验：校验用户是否存在
            UserDO user = userMapper.selectById(createOrderDTO.getUserId());
            if (user == null) {
                log.warn("创建订单失败，用户不存在，用户ID: {}", createOrderDTO.getUserId());
                return new RestResult<>(ResultCodeConstant.CODE_000001, "用户不存在");
            }
            
            // 幂等性检查：检查是否已经存在相同requestId的订单
            OrderDO existingOrder = orderMapper.selectByRequestId(createOrderDTO.getRequestId());
            if (existingOrder != null) {
                log.info("订单已存在，订单号: {}", existingOrder.getOrderNo());
                return new RestResult<>(ResultCodeConstant.CODE_000000, "订单已存在", existingOrder.getOrderNo());
            }
            
            // 生成订单编号：时间戳+随机数
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNo = "ORD" + sdf.format(new Date()) + String.format("%04d", new Random().nextInt(10000));
            
            // 构建订单对象
            OrderDO orderDO = new OrderDO();
            BeanUtils.copyProperties(createOrderDTO,orderDO);
            orderDO.setOrderNo(orderNo);
            orderDO.setStatus(1); // 待支付状态
            
            // 创建一个线程安全的引用变量totalAmount，用于累计订单总金额，初始值为0。使用AtomicReference是因为在lambda表达式中需要修改这个变量
            AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
            
            // 校验商品是否上架且库存充足（仅检查，不扣减）
            for (OrderProductDTO item : createOrderDTO.getProductList()) {
                ProductDO product = productMapper.findByIdAndStatus(item.getProductId(), 1);
                if (product == null) {
                    log.warn("创建订单失败，商品未上架或不存在，商品ID: {}", item.getProductId());
                    return new RestResult<>(ResultCodeConstant.CODE_000001, "商品[" + item.getProductId() + "]不存在或未上架");
                }
                
                if (product.getStock() < item.getQuantity()) {
                    log.warn("创建订单失败，商品库存不足，商品ID: {}，需求数量: {}，当前库存: {}", 
                            item.getProductId(), item.getQuantity(), product.getStock());
                    return new RestResult<>(ResultCodeConstant.CODE_000001, "商品[" + item.getProductId() + "]库存不足");
                }
                
                // 检查限购
                RestResult<LimitPurchaseDO> limitResult = checkLimitPurchase(createOrderDTO.getUserId(), item.getProductId(), item.getQuantity());
                if (!ResultCodeConstant.CODE_000000.equals(limitResult.getCode())) {
                    return new RestResult<>(limitResult.getCode(), limitResult.getMsg());
                }
                
                /*计算并累计订单总金额：
                    product.getPrice() - 获取商品单价
                    item.getQuantity() - 获取该商品的购买数量
                    BigDecimal.valueOf() - 将整数转换为BigDecimal，确保精确计算
                    multiply() - 计算该商品的小计金额(单价×数量)
                    totalAmount.get() - 获取当前累计金额
                    add() - 将该商品小计加到累计金额上
                    totalAmount.set() - 更新累计金额
                * */
                // 检查是否有有效的早鸟票价格
                BigDecimal unitPrice = checkPreSaleTicketPrice(item.getProductId(), BigDecimal.valueOf(product.getPrice()));
                totalAmount.set(totalAmount.get()
                        .add(
                                unitPrice.multiply(
                                                BigDecimal.valueOf(item.getQuantity())
                                        )
                        )
                );
            }
            
            // 设置订单总金额
            orderDO.setTotalAmount(totalAmount.get());
            
            // 保存订单到数据库
            int insertResult = orderMapper.insert(orderDO);
            if (insertResult <= 0) {
                log.error("创建订单失败，订单保存到数据库失败，订单号: {}", orderNo);
                return new RestResult<>(ResultCodeConstant.CODE_999999, "订单创建失败");
            }
            
            // 保存订单详情
            List<OrderItemDO> orderItems = new ArrayList<>();
            for (OrderProductDTO item : createOrderDTO.getProductList()) {
                ProductDO product = productMapper.findByIdAndStatus(item.getProductId(), 1);
                
                // 获取商品单价（考虑早鸟票价格）
                BigDecimal unitPrice = checkPreSaleTicketPrice(item.getProductId(), BigDecimal.valueOf(product.getPrice()));
                
                OrderItemDO orderItem = new OrderItemDO();
                BeanUtils.copyProperties(orderDO,orderItem);
                orderItem.setProductName(product.getName());
                orderItem.setPrice(unitPrice);
                orderItem.setSubtotalAmount(orderItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                
                int itemInsertResult = orderItemMapper.insert(orderItem);
                if (itemInsertResult <= 0) {
                    log.error("创建订单失败，订单详情保存失败，订单号: {}，商品ID: {}", orderNo, item.getProductId());
                    throw new BusinessException(ResultCodeConstant.CODE_999999, "订单详情保存失败");
                }
                orderItems.add(orderItem);
            }
            
            // 发送库存扣减消息到RocketMQ，实现最终一致性
            StockDeductionMessage stockDeductionMessage = new StockDeductionMessage();
            BeanUtils.copyProperties(orderDO,stockDeductionMessage);
            stockDeductionMessage.setOrderId(orderDO.getId());
            
            // 发送消息到RocketMQ
            rocketMQTemplate.sendOneWay("STOCK_DEDUCTION_TOPIC", 
                                      MessageBuilder.withPayload(stockDeductionMessage).build());
            
            log.info("订单创建成功，订单号: {}，订单总金额: {}，已发送库存扣减消息", orderNo, totalAmount.get());
            
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, orderNo);
            
        } catch (BusinessException e) {
            log.error("创建订单业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, ResultCodeConstant.CODE_999999_MSG);
        }
    }
    
    /**
     * 回滚已扣减的库存
     * @param deductedProductMap 已扣减库存的商品ID和数量映射
     */
    private void rollbackDeductedStock(Map<Long, Integer> deductedProductMap) {
        log.info("开始回滚已扣减的库存，商品数量: {}", deductedProductMap.size());
        
        for (Map.Entry<Long, Integer> entry : deductedProductMap.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            try {
                // 查询当前商品信息，获取最新的版本号
                ProductDO product = productMapper.selectById(productId);
                if (product != null) {
                    // 使用乐观锁恢复库存，传入最新的版本号
                    int updateCount = productMapper.increaseStockWithVersion(productId, quantity, product.getVersion());
                    if (updateCount <= 0) {
                        log.warn("恢复商品[{}]库存失败，可能由于并发更新导致版本号不匹配，需要人工干预", productId);
                    } else {
                        log.info("成功恢复商品[{}]库存{}件，恢复后库存: {}", productId, quantity, product.getStock() + quantity);
                    }
                } else {
                    log.warn("回滚库存时未找到商品[{}]，可能已被删除", productId);
                }
            } catch (Exception ex) {
                log.error("回滚商品[{}]库存失败", productId, ex);
            }
        }
        
        log.info("库存回滚操作完成");
    }

    /**
     * 通过订单号查询
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
    @DataSource(DataSourceContextHolder.SLAVE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RestResult<Object> queryOrderDetail(OrderQueryDTO orderQueryDTO) {
        OrderDO orderDO = orderMapper.selectById(orderQueryDTO.getOrderNo());
        if (orderDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000003, ResultCodeConstant.CODE_000003_MSG);
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, orderDO);
    }

    /**
     * 取消订单
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
    @DataSource()
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RestResult<Boolean> cancelOrder(OrderQueryDTO orderQueryDTO) {
        log.info("开始处理订单取消，订单号: {}", orderQueryDTO.getOrderNo());
        
        OrderDO orderDO = orderMapper.selectById(orderQueryDTO.getOrderNo());
        if (orderDO == null) {
            log.warn("订单取消失败，订单不存在，订单号: {}", orderQueryDTO.getOrderNo());
            return new RestResult<>(ResultCodeConstant.CODE_000003, ResultCodeConstant.CODE_000003_MSG);
        }
        
        // 判断订单状态是否允许取消（这里简单设定只有待支付状态可以取消）
        if (orderDO.getStatus() != 1) {
            log.warn("订单取消失败，订单状态不允许取消，订单号: {}，当前状态: {}", orderQueryDTO.getOrderNo(), orderDO.getStatus());
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单状态不允许取消");
        }
        
        // 已取消
        orderDO.setStatus(4);
        orderDO.setUpdateTime(new Date());
        int updateResult = orderMapper.updateById(orderDO);
        if (updateResult <= 0) {
            log.error("订单取消失败，更新订单状态失败，订单号: {}", orderQueryDTO.getOrderNo());
            return new RestResult<>(ResultCodeConstant.CODE_999999, "订单取消失败");
        }
        
        log.info("订单取消成功，订单号: {}，订单状态已更新为已取消", orderQueryDTO.getOrderNo());
        
        // 强制刷新机制：设置下次读取使用主库，确保能读取到最新数据
        DataSourceUtil.forceMaster();
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    /**
     * 支付订单
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
    @DataSource()
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RestResult<Boolean> payOrder(OrderQueryDTO orderQueryDTO) {
        log.info("开始处理订单支付，订单号: {}", orderQueryDTO.getOrderNo());
        
        OrderDO orderDO = orderMapper.selectById(orderQueryDTO.getOrderNo());
        if (orderDO == null) {
            log.warn("订单支付失败，订单不存在，订单号: {}", orderQueryDTO.getOrderNo());
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单不存在");
        }
        
        // 判断订单是否处于待支付状态
        if (orderDO.getStatus() != 1) {
            log.warn("订单支付失败，订单状态不允许支付，订单号: {}，当前状态: {}", orderQueryDTO.getOrderNo(), orderDO.getStatus());
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单不可支付");
        }
        
        // 已支付
        orderDO.setStatus(2);
        orderDO.setUpdateTime(new Date());
        int updateResult = orderMapper.updateById(orderDO);
        if (updateResult <= 0) {
            log.error("订单支付失败，更新订单状态失败，订单号: {}", orderQueryDTO.getOrderNo());
            return new RestResult<>(ResultCodeConstant.CODE_999999, "订单支付失败");
        }
        
        log.info("订单支付成功，订单号: {}，订单状态已更新为已支付", orderQueryDTO.getOrderNo());
        
        // 强制刷新机制：设置下次读取使用主库，确保能读取到最新数据
        DataSourceUtil.forceMaster();
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    @Override
    @DataSource(DataSourceContextHolder.SLAVE)
    public void exportOrders(OrderExportQueryDTO query, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(query.getOrderNo() != null && !query.getOrderNo().isEmpty(), OrderDO::getOrderNo, query.getOrderNo())
               .eq(query.getUserId() != null, OrderDO::getUserId, query.getUserId())
               .eq(query.getDeliveryType() != null, OrderDO::getDeliveryType, query.getDeliveryType())
               .eq(query.getStatus() != null, OrderDO::getStatus, query.getStatus())
               .ge(query.getStartTime() != null, OrderDO::getCreateTime, query.getStartTime())
               .le(query.getEndTime() != null, OrderDO::getUpdateTime, query.getEndTime());
        
        List<OrderDO> orderList = orderMapper.selectList(wrapper);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("订单数据");
        // 设置列标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"订单ID", "用户ID", "订单编号", "订单总金额", "配送方式", "订单状态", "创建时间"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 填充数据
        int rowNum = 1;
        for (OrderDO order : orderList) {
            Row row = sheet.createRow(rowNum++);
            Object[] rowData = {
                order.getId(),
                order.getUserId(),
                order.getOrderNo(),
                order.getTotalAmount().toString(),
                order.getDeliveryType() == 1 ? "自取" : "快递",
                getStatusText(order.getStatus()),
                sdf.format(order.getCreateTime())
            };
            
            for (int i = 0; i < rowData.length; i++) {
                row.createCell(i).setCellValue(rowData[i].toString());
            }
        }
        // 自动调整列宽
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
        // 写入响应流
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("订单数据.xlsx", StandardCharsets.UTF_8)
                .build();
        response.setHeader("Content-Disposition", contentDisposition.toString());
        workbook.write(response.getOutputStream());
        try {
            workbook.close();
        } catch (IOException e) {
            log.error("导出订单数据，关闭Excel工作簿时发生异常", e);
        }
    }

    /**
     * 获取订单状态文本描述
     *
     * @param status 状态值
     * @return 对应的状态文本
     */
    private String getStatusText(Integer status) {
        switch(status) {
            case 1:
                return "待支付";
            case 2:
                return "已支付";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知状态";
        }
    }

    @Override
    @DataSource()
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Boolean importOrders(List<OrderDO> orders) {
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "导入的数据为空");
        }
        // 进行数据校验和插入操作
        try {
            for (OrderDO order : orders) {
                // 校验必要字段
                Map<String, Object> requiredFields = new HashMap<>();
                requiredFields.put("订单编号", order.getOrderNo());
                requiredFields.put("用户ID", order.getUserId());
                requiredFields.put("订单总金额", order.getTotalAmount());
                
                requiredFields.forEach((fieldName, fieldValue) -> {
                    if (fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty())) {
                        throw new BusinessException(ResultCodeConstant.CODE_000001, fieldName + "不能为空");
                    }
                });
                
                // 检查订单编号是否已存在
                if (orderMapper.selectById(order.getOrderNo()) != null) {
                    throw new BusinessException(ResultCodeConstant.CODE_000001, "订单编号已存在: " + order.getOrderNo());
                }
                // 插入数据库
                orderMapper.insert(order);
            }
        } catch (Exception e) {
            log.error("批量导入订单数据失败", e);
            throw e;
        }
        return true;
    }

    @Override
    @DataSource()
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Boolean addOrder(OrderDTO orderDTO) {
        // 校验订单号是否重复
        if (orderMapper.selectById(orderDTO.getOrderNo()) != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000005, ResultCodeConstant.CODE_000005_MSG);
        }
        // 插入新订单
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderDTO,orderDO);
        // 默认为待支付状态
        orderDO.setStatus(1);
        return orderMapper.insert(orderDO) >0;
    }

    @Override
    @DataSource()
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Boolean updateOrderStatus(OrderDTO orderDTO) {
        // 验证订单是否存在
        OrderDO orderDO = orderMapper.selectById(orderDTO.getOrderId());
        if (orderDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000003, ResultCodeConstant.CODE_000003_MSG);
        }
        // 更新订单状态
        orderDO.setStatus(orderDTO.getStatus());
        return orderMapper.updateById(orderDO) >0;
    }

    //通过订单ID查询
    @Override
    @DataSource(DataSourceContextHolder.SLAVE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public OrderDO orderInfo(OrderQuery orderQuery) {
        OrderDO orderDO = orderMapper.selectById(orderQuery.getOrderId());
        if (orderDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000003, ResultCodeConstant.CODE_000003_MSG);
        }
        return orderDO;
    }

    @Override
    @DataSource(DataSourceContextHolder.SLAVE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Page<OrderDO> listOrders(OrderQueryDTO dto) {
        Page<OrderDO> orderPage = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        if (dto.getUserId() != null) {
            wrapper.eq(OrderDO::getUserId, dto.getUserId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(OrderDO::getStatus,  dto.getStatus());
        }
        wrapper.orderByDesc(OrderDO::getCreateTime);
        return orderMapper.selectPage(orderPage, wrapper);
    }

    /**
     * 定时任务补偿机制：检查和修复数据不一致问题
     * 每隔10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟执行一次
    @DataSource()
    public void checkAndFixDataInconsistency() {
        log.info("开始执行数据一致性检查任务");
        
        try {
            int fixedCount = 0;
            
            // 查找状态为"待支付"但创建时间超过30分钟的订单（可能存在数据不一致）
            Date halfHourAgo = new Date(System.currentTimeMillis() - 30 * 60 * 1000L);
            LambdaQueryWrapper<OrderDO> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(OrderDO::getStatus, 1); // 待支付状态
            pendingWrapper.lt(OrderDO::getCreateTime, halfHourAgo);
            List<OrderDO> pendingOrders = orderMapper.selectList(pendingWrapper);
            
            for (OrderDO order : pendingOrders) {
                log.warn("发现长时间处于待支付状态的订单，订单号: {}，创建时间: {}", 
                        order.getOrderNo(), order.getCreateTime());
                // 对于长时间待支付的订单，可以考虑自动取消或提醒用户
            }
            
            // 查找状态为"已支付"但库存未扣减的订单（可能存在数据不一致）
            LambdaQueryWrapper<OrderDO> paidWrapper = new LambdaQueryWrapper<>();
            paidWrapper.eq(OrderDO::getStatus, 2); // 已支付状态
            List<OrderDO> paidOrders = orderMapper.selectList(paidWrapper);
            
            for (OrderDO order : paidOrders) {
                // 检查该订单是否已经正确扣减了库存
                if (!isStockDeductedCorrectly(order)) {
                    // 如果库存未正确扣减，则重新发送库存扣减消息
                    resendStockDeductionMessage(order);
                    fixedCount++;
                    log.info("发现订单[{}]库存未正确扣减，已重新发送库存扣减消息", order.getOrderNo());
                }
            }
            
            log.info("数据一致性检查任务完成，共修复 {} 个订单的库存问题", fixedCount);
        } catch (Exception e) {
            log.error("执行数据一致性检查任务时发生异常", e);
        }
    }
    
    /**
     * 检查订单的库存是否已正确扣减
     * @param order 订单对象
     * @return 是否已正确扣减库存
     */
    private boolean isStockDeductedCorrectly(OrderDO order) {
        try {
            log.info("开始检查订单库存扣减情况，订单号: {}", order.getOrderNo());
            
            // 获取订单商品列表
            List<OrderItemDO> orderItems = orderItemMapper.selectByOrderId(order.getId());
            
            // 检查订单是否有商品
            if (orderItems.isEmpty()) {
                log.warn("订单[{}]没有商品信息", order.getOrderNo());
                return false;
            }
            
            // 检查每个商品的库存是否正确扣减
            for (OrderItemDO item : orderItems) {
                // 查询当前商品信息
                ProductDO product = productMapper.selectById(item.getProductId());
                if (product == null) {
                    log.warn("订单[{}]中的商品[{}]不存在", order.getOrderNo(), item.getProductId());
                    return false;
                }
                
                // 检查商品库存是否足够（这里应该检查扣减后的库存是否正确）
                if (product.getStock() < 0) {
                    log.warn("订单[{}]中的商品[{}]库存异常，当前库存: {}", 
                            order.getOrderNo(), item.getProductId(), product.getStock());
                    return false;
                }
            }
            
            log.info("订单[{}]库存扣减情况检查完成，未发现异常", order.getOrderNo());
            return true;
        } catch (Exception e) {
            log.error("检查订单[{}]库存扣减情况时发生异常", order.getOrderNo(), e);
            return false;
        }
    }
    
    /**
     * 重新发送库存扣减消息
     * @param order 订单对象
     */
    private void resendStockDeductionMessage(OrderDO order) {
        try {
            // 获取订单商品列表
            List<OrderItemDO> orderItems = orderItemMapper.selectByOrderId(order.getId());
            List<OrderProductDTO> productList = new ArrayList<>();
            
            // 转换为OrderProductDTO列表
            for (OrderItemDO item : orderItems) {
                OrderProductDTO productDTO = new OrderProductDTO();
                productDTO.setProductId(item.getProductId());
                productDTO.setQuantity(item.getQuantity());
                productList.add(productDTO);
            }
            
            // 构造库存扣减消息并重新发送
            StockDeductionMessage stockDeductionMessage = new StockDeductionMessage();
            BeanUtils.copyProperties(order,stockDeductionMessage);
            stockDeductionMessage.setOrderId(order.getId());
            stockDeductionMessage.setProductList(productList);
            
            // 发送消息到RocketMQ
            rocketMQTemplate.sendOneWay("STOCK_DEDUCTION_TOPIC", 
                    MessageBuilder.withPayload(stockDeductionMessage).build());
            
            log.info("重新发送库存扣减消息，订单号: {}", order.getOrderNo());
        } catch (Exception e) {
            log.error("重新发送订单[{}]库存扣减消息时发生异常", order.getOrderNo(), e);
        }
    }
    
    /**
     * 监听库存扣减消息
     * @param message 库存扣减消息
     */
    @Override
    @DataSource(DataSourceContextHolder.MASTER)
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(StockDeductionMessage message) {
        log.info("接收到库存扣减消息，订单号: {}", message.getOrderNo());
        
        // 数据校验：检查订单是否存在
        OrderDO order = orderMapper.selectById(message.getOrderId());
        if (order == null) {
            log.warn("库存扣减失败，订单不存在，订单ID: {}", message.getOrderId());
            throw new RuntimeException("订单不存在");
        }
        
        // 数据校验：检查订单状态是否为待支付
        if (order.getStatus() != 1) {
            log.warn("库存扣减失败，订单状态不正确，订单号: {}，当前状态: {}", message.getOrderNo(), order.getStatus());
            throw new RuntimeException("订单状态不正确");
        }
        
        // 用于记录已扣减库存的商品，以便在出现异常时回滚
        Map<Long, Integer> deductedProductMap = new HashMap<>();
        
        try {
            // 扣减商品库存
            for (OrderProductDTO item : message.getProductList()) {
                // 查询商品信息获取当前版本号
                ProductDO product = productMapper.selectById(item.getProductId());
                if (product == null) {
                    log.error("库存扣减失败，商品不存在，商品ID: {}", item.getProductId());
                    throw new RuntimeException("商品[" + item.getProductId() + "]不存在");
                }
                
                // 检查库存是否充足
                if (product.getStock() < item.getQuantity()) {
                    log.error("库存扣减失败，商品库存不足，商品ID: {}，当前库存: {}，需扣减: {}", 
                            item.getProductId(), product.getStock(), item.getQuantity());
                    throw new RuntimeException("商品[" + item.getProductId() + "]库存不足，当前库存: " + product.getStock() + "，需扣减: " + item.getQuantity());
                }
                
                // 使用乐观锁扣减库存
                int updateCount = productMapper.deductStockWithVersion(
                        item.getProductId(), 
                        item.getQuantity(), 
                        product.getVersion());
                
                if (updateCount <= 0) {
                    log.error("库存扣减失败，可能由于并发更新导致版本号不匹配，商品ID: {}", item.getProductId());
                    throw new RuntimeException("商品[" + item.getProductId() + "]库存扣减失败，可能由于并发更新导致版本号不匹配");
                }
                
                // 记录已扣减库存的商品
                deductedProductMap.put(item.getProductId(), item.getQuantity());
                
                log.info("商品[{}]库存扣减成功，扣减数量: {}，扣减后库存: {}", 
                        item.getProductId(), item.getQuantity(), product.getStock() - item.getQuantity());
            }
            
            // 更新订单状态为已支付
            order.setStatus(2);
            order.setUpdateTime(new Date());
            int updateResult = orderMapper.updateById(order);
            if (updateResult <= 0) {
                log.error("更新订单状态失败，订单号: {}", message.getOrderNo());
                throw new RuntimeException("更新订单状态失败");
            }
            
            log.info("订单[{}]库存扣减全部完成，订单状态已更新为已支付", message.getOrderNo());
            
        } catch (Exception e) {
            log.error("库存扣减失败，订单号: {}，错误信息: {}", message.getOrderNo(), e.getMessage(), e);
            
            // 回滚已扣减的库存
            rollbackDeductedStock(deductedProductMap);
            
            // 重新抛出异常，让RocketMQ重新投递消息
            throw new RuntimeException("库存扣减失败，消息将重新投递", e);
        }
    }
    
    /**
     * 检查商品限购
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 购买数量
     * @return 检查结果
     */
    private RestResult<LimitPurchaseDO> checkLimitPurchase(Long userId, Long productId, Integer quantity) {
        // 查询该商品是否有限购配置
        LimitPurchaseQueryDTO queryDTO = new LimitPurchaseQueryDTO();
        queryDTO.setProductId(productId);
        RestResult<LimitPurchaseDO> limitResult = limitPurchaseService.getLimitPurchaseDetail(queryDTO);
        
        // 如果没有限购配置，直接返回成功
        if (!ResultCodeConstant.CODE_000000.equals(limitResult.getCode()) || limitResult.getData() == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, (LimitPurchaseDO) null);
        }
        
        LimitPurchaseDO limitPurchase = limitResult.getData();
        
        // 查询用户已购买该商品的数量
        Integer purchasedQuantity = orderItemMapper.selectPurchasedQuantityByUserAndProduct(userId, productId);
        
        // 检查是否超过限购数量
        if (purchasedQuantity + quantity > limitPurchase.getMaxQuantity()) {
            int remainingQuantity = limitPurchase.getMaxQuantity() - purchasedQuantity;
            if (remainingQuantity <= 0) {
                return new RestResult<>(ResultCodeConstant.CODE_000001, "您已达到该商品的购买上限(" + limitPurchase.getMaxQuantity() + ")");
            } else {
                return new RestResult<>(ResultCodeConstant.CODE_000001, "该商品限购" + limitPurchase.getMaxQuantity() + "件，您还可以购买" + remainingQuantity + "件");
            }
        }
        
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, limitPurchase);
    }
    
    /**
     * 检查商品是否有有效的早鸟票价格
     * @param productId 商品ID
     * @param regularPrice 商品正常价格
     * @return 商品价格（早鸟票价格或正常价格）
     */
    private BigDecimal checkPreSaleTicketPrice(Long productId, BigDecimal regularPrice) {
        try {
            // 构造查询条件
            cn.sym.dto.PreSaleTicketQueryDTO queryDTO = new cn.sym.dto.PreSaleTicketQueryDTO();
            queryDTO.setProductId(productId);
            
            // 查询早鸟票信息
            RestResult<PreSaleTicketDO> preSaleResult = preSaleTicketService.getPreSaleTicketDetail(queryDTO);
            
            // 如果查询成功且存在早鸟票信息
            if (ResultCodeConstant.CODE_000000.equals(preSaleResult.getCode()) && preSaleResult.getData() != null) {
                PreSaleTicketDO preSaleTicket = preSaleResult.getData();
                Date now = new Date();
                
                // 检查当前时间是否在预售时间范围内
                if (now.after(preSaleTicket.getSaleStartTime()) && now.before(preSaleTicket.getSaleEndTime())) {
                    log.info("商品[{}]使用早鸟票价格: {}", productId, preSaleTicket.getPrePrice());
                    return preSaleTicket.getPrePrice();
                }
            }
        } catch (Exception e) {
            log.error("检查商品[{}]早鸟票价格时发生异常", productId, e);
        }
        
        // 如果没有有效的早鸟票，返回正常价格
        log.info("商品[{}]使用正常价格: {}", productId, regularPrice);
        return regularPrice;
    }
}
