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
import java.util.Date;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.List;
import org.springframework.util.CollectionUtils;
import cn.sym.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import cn.sym.dto.OrderQuery;
import cn.sym.dto.OrderDTO;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 订单服务实现类
 *
 * @author user
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    /**
     * 创建订单
     * @param createOrderDTO 创建订单参数
     * @return 响应结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RestResult<String> createOrder(CreateOrderDTO createOrderDTO) {
        try {
            // 校验用户是否存在
            UserDO user = userMapper.selectById(createOrderDTO.getUserId());
            if (user == null) {
                return new RestResult<>(ResultCodeConstant.CODE_000001, "用户不存在");
            }
            // 校验商品是否上架且库存充足
            //创建一个线程安全的引用变量totalAmount，用于累计订单总金额，初始值为0。使用AtomicReference是因为在lambda表达式中需要修改这个变量
            AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
            for (OrderProductDTO item : createOrderDTO.getProductList()) {
                // 1表示上架
                ProductDO product = productMapper.findByIdAndStatus(item.getProductId(), 1);
                if (product == null || product.getStock() < item.getQuantity()) {
                    return new RestResult<>(ResultCodeConstant.CODE_000001, "商品库存不足或未上架");
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
                totalAmount.set(totalAmount.get()
                        .add(
                                BigDecimal.valueOf(product.getPrice())
                                        .multiply(
                                                BigDecimal.valueOf(item.getQuantity())
                                        )
                        )
                );

            }
            
            // 生成订单编号：时间戳+随机数
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNo = "ORD" + sdf.format(new Date()) + String.format("%04d", new Random().nextInt(10000));
            
            // 构建订单对象
            OrderDO orderDO = new OrderDO();
            orderDO.setUserId(createOrderDTO.getUserId());
            orderDO.setOrderNo(orderNo);
            orderDO.setTotalAmount(totalAmount.get());
            orderDO.setDeliveryType(createOrderDTO.getDeliveryType());
            // 待支付状态
            orderDO.setStatus(1);
            
            // 保存订单到数据库
            orderMapper.insert(orderDO);

            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG);
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return new RestResult<>(ResultCodeConstant.CODE_999999, ResultCodeConstant.CODE_999999_MSG);
        }
    }

    /**
     * 通过订单号查询
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
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
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RestResult<Boolean> cancelOrder(OrderQueryDTO orderQueryDTO) {
        OrderDO orderDO = orderMapper.selectById(orderQueryDTO.getOrderNo());
        if (orderDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000003, ResultCodeConstant.CODE_000003_MSG);
        }
        // 判断订单状态是否允许取消（这里简单设定只有待支付状态可以取消）
        if (orderDO.getStatus() != 1) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单状态不允许取消");
        }
        // 已取消
        orderDO.setStatus(4);
        orderDO.setUpdateTime(new Date());
        orderMapper.updateById(orderDO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    /**
     * 支付订单
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public RestResult<Boolean> payOrder(OrderQueryDTO orderQueryDTO) {
        OrderDO orderDO = orderMapper.selectById(orderQueryDTO.getOrderNo());
        if (orderDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单不存在");
        }
        // 判断订单是否处于待支付状态
        if (orderDO.getStatus() != 1) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单不可支付");
        }
        // 已支付
        orderDO.setStatus(2);
        orderDO.setUpdateTime(new Date());
        orderMapper.updateById(orderDO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    @Override
    public void exportOrders(OrderExportQueryDTO query, HttpServletResponse response) throws IOException {
        // 构建查询条件
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        if (query.getOrderNo() != null && !query.getOrderNo().isEmpty()) {
            wrapper.like(OrderDO::getOrderNo, query.getOrderNo());
        }
        if (query.getUserId() != null) {
            wrapper.eq(OrderDO::getUserId, query.getUserId());
        }
        if (query.getDeliveryType() != null) {
            wrapper.eq(OrderDO::getDeliveryType, query.getDeliveryType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(OrderDO::getStatus, query.getStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(OrderDO::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OrderDO::getUpdateTime, query.getEndTime());
        }
        
        List<OrderDO> orderList = orderMapper.selectList(wrapper);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("订单数据");
        // 设置列标题行
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("订单ID");
        headerRow.createCell(1).setCellValue("用户ID");
        headerRow.createCell(2).setCellValue("订单编号");
        headerRow.createCell(3).setCellValue("订单总金额");
        headerRow.createCell(4).setCellValue("配送方式");
        headerRow.createCell(5).setCellValue("订单状态");
        headerRow.createCell(6).setCellValue("创建时间");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 填充数据
        int rowNum = 1;
        for (OrderDO order : orderList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(order.getId());
            row.createCell(1).setCellValue(order.getUserId());
            row.createCell(2).setCellValue(order.getOrderNo());
            row.createCell(3).setCellValue(order.getTotalAmount().toString());
            row.createCell(4).setCellValue(order.getDeliveryType() == 1 ? "自取" : "快递");
            row.createCell(5).setCellValue(getStatusText(order.getStatus()));
            row.createCell(6).setCellValue(sdf.format(order.getCreateTime()));
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
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Boolean importOrders(List<OrderDO> orders) {
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "导入的数据为空");
        }
        // 进行数据校验和插入操作
        try {
            for (OrderDO order : orders) {
                // 校验必要字段
                if (order.getOrderNo() == null || order.getOrderNo().trim().isEmpty()) {
                    throw new BusinessException(ResultCodeConstant.CODE_000001, "订单编号不能为空");
                }
                if (order.getUserId() == null) {
                    throw new BusinessException(ResultCodeConstant.CODE_000001, "用户ID不能为空");
                }
                if (order.getTotalAmount() == null) {
                    throw new BusinessException(ResultCodeConstant.CODE_000001, "订单总金额不能为空");
                }
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
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Boolean addOrder(OrderDTO orderDTO) {
        // 校验订单号是否重复
        if (orderMapper.selectById(orderDTO.getOrderNo()) != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000005, ResultCodeConstant.CODE_000005_MSG);
        }
        // 插入新订单
        OrderDO orderDO = new OrderDO();
        orderDO.setUserId(orderDTO.getUserId());
        orderDO.setOrderNo(orderDTO.getOrderNo());
        orderDO.setTotalAmount(orderDTO.getTotalAmount());
        orderDO.setDeliveryType(orderDTO.getDeliveryType());
        // 默认为待支付状态
        orderDO.setStatus(1);
        return orderMapper.insert(orderDO) >0;
    }

    @Override
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
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public OrderDO orderInfo(OrderQuery orderQuery) {
        OrderDO orderDO = orderMapper.selectById(orderQuery.getOrderId());
        if (orderDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000003, ResultCodeConstant.CODE_000003_MSG);
        }
        return orderDO;
    }

    @Override
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
}
