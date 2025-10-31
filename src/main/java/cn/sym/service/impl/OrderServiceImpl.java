package cn.sym.service.impl;

import cn.sym.dto.CreateOrderDTO;
import cn.sym.dto.OrderProductDTO;
import cn.sym.dto.OrderQueryDTO;
import cn.sym.entity.OrderDO;
import cn.sym.entity.ProductDO;
import cn.sym.repository.OrderRepository;
import cn.sym.repository.ProductRepository;
import cn.sym.response.RestResult;
import cn.sym.response.ResultCodeConstant;
import cn.sym.service.OrderService;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.sym.dto.OrderExportQueryDTO;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import cn.sym.exception.BusinessException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.util.CollectionUtils;
import cn.sym.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import cn.sym.utils.ResultCodeConstant;
import cn.sym.dto.OrderQuery;
import cn.sym.dto.OrderDTO;

/**
 * 订单服务实现类
 *
 * @author user
 */
@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 创建订单
     * @param createOrderDTO 创建订单参数
     * @return 响应结果
     */
    @Override
    public RestResult<String> createOrder(CreateOrderDTO createOrderDTO) {
        try {
            // 校验用户是否存在
            // 这里假设有一个UserService来检查用户存在性，实际项目中需要具体实现
            // boolean isUserExist = userService.checkUserExists(createOrderDTO.getUserId());
            // if (!isUserExist) {
            // return new RestResult<>(ResultCodeConstant.CODE_000001, "用户不存在");
            // }
            // 校验商品是否上架且库存充足
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderProductDTO item : createOrderDTO.getProductList()) {
                // 1表示上架
                ProductDO product = productRepository.findByIdAndStatus(item.getProductId(), 1);
                if (product == null || product.getStock() < item.getQuantity()) {
                    return new RestResult<>(ResultCodeConstant.CODE_000001, "商品库存不足或未上架");
                }
                totalAmount = totalAmount.add(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            // 生成订单编号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String orderNo = "ORD" + sdf.format(new Date()) + new Random().nextInt(1000);
            // 构建订单对象
            OrderDO orderDO = new OrderDO();
            orderDO.setUserId(createOrderDTO.getUserId());
            orderDO.setOrderNo(orderNo);
            orderDO.setTotalAmount(totalAmount);
            orderDO.setDeliveryType(createOrderDTO.getDeliveryType());
            // 待支付状态
            orderDO.setStatus(1);
            orderDO.setCreateTime(new Date());
            orderDO.setUpdateTime(new Date());
            // 保存订单到数据库
            orderRepository.save(orderDO);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, orderNo);
        } catch (Exception e) {
            return new RestResult<>(ResultCodeConstant.CODE_999999, "系统异常");
        }
    }

    /**
     * 查询订单详情
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
    public RestResult<Object> queryOrderDetail(OrderQueryDTO orderQueryDTO) {
        OrderDO orderDO = orderRepository.findByOrderNo(orderQueryDTO.getOrderNo());
        if (orderDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单不存在");
        }
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, orderDO);
    }

    /**
     * 取消订单
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
    public RestResult<Boolean> cancelOrder(OrderQueryDTO orderQueryDTO) {
        OrderDO orderDO = orderRepository.findByOrderNo(orderQueryDTO.getOrderNo());
        if (orderDO == null) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单不存在");
        }
        // 判断订单状态是否允许取消（这里简单设定只有待支付状态可以取消）
        if (orderDO.getStatus() != 1) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "订单状态不允许取消");
        }
        // 已取消
        orderDO.setStatus(4);
        orderDO.setUpdateTime(new Date());
        orderRepository.save(orderDO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    /**
     * 支付订单
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @Override
    public RestResult<Boolean> payOrder(OrderQueryDTO orderQueryDTO) {
        OrderDO orderDO = orderRepository.findByOrderNo(orderQueryDTO.getOrderNo());
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
        orderRepository.save(orderDO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
    }

    @Override
    public void exportOrders(OrderExportQueryDTO query, HttpServletResponse response) throws IOException {
        // 构建查询条件
        QueryWrapper<OrderDO> wrapper = new QueryWrapper<>();
        if (query.getOrderNo() != null && !query.getOrderNo().isEmpty()) {
            wrapper.like("order_no", query.getOrderNo());
        }
        if (query.getUserId() != null) {
            wrapper.eq("user_id", query.getUserId());
        }
        if (query.getDeliveryType() != null) {
            wrapper.eq("delivery_type", query.getDeliveryType());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge("create_time", query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le("create_time", query.getEndTime());
        }
        Page<OrderDO> page = new Page<>(1, Integer.MAX_VALUE);
        this.page(page, wrapper);
        List<OrderDO> orderList = page.getRecords();
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
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String("订单数据.xlsx".getBytes("utf-8"), "iso-8859-1"));
        workbook.write(response.getOutputStream());
        try {
            workbook.close();
        } catch (IOException e) {
            log.error("关闭Workbook失败", e);
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

    @Transactional
    @Override
    public Boolean importOrders(List<OrderDO> orders) {
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, "导入的数据为空");
        }
        // 进行数据校验和插入操作
        boolean success = true;
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
                // 可以在这里添加更多校验逻辑
                // 插入数据库
                this.save(order);
            }
        } catch (Exception e) {
            log.error("批量导入订单数据失败", e);
            success = false;
        }
        return success;
    }

    @Override
    public Boolean addOrder(OrderDTO orderDTO) {
        // 校验订单号是否重复
        QueryWrapper<OrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderDTO.getOrderNo());
        OrderDO existingOrder = orderMapper.selectOne(wrapper);
        if (existingOrder != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 插入新订单
        OrderDO orderDO = new OrderDO();
        orderDO.setUserId(orderDTO.getUserId());
        orderDO.setOrderNo(orderDTO.getOrderNo());
        orderDO.setTotalAmount(orderDTO.getTotalAmount());
        orderDO.setDeliveryType(orderDTO.getDeliveryType());
        // 默认为待支付状态
        orderDO.setStatus(1);
        orderDO.setCreateBy("system");
        orderDO.setCreateTime(new Date());
        orderDO.setUpdateBy("system");
        orderDO.setUpdateTime(new Date());
        return orderMapper.insert(orderDO) > 0;
    }

    @Override
    public Boolean updateOrderStatus(OrderDTO orderDTO) {
        // 验证订单是否存在
        OrderDO orderDO = orderMapper.selectById(orderDTO.getOrderId());
        if (orderDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        // 更新订单状态
        orderDO.setStatus(orderDTO.getStatus());
        orderDO.setUpdateBy("system");
        orderDO.setUpdateTime(new Date());
        return orderMapper.updateById(orderDO) > 0;
    }

    @Override
    public OrderDO orderInfo(OrderQuery orderQuery) {
        OrderDO orderDO = orderMapper.selectById(orderQuery.getOrderId());
        if (orderDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        return orderDO;
    }

    @Override
    public Page<OrderDO> listOrders(int page, int size, Long userId, Integer status) {
        Page<OrderDO> orderPage = new Page<>(page, size);
        QueryWrapper<OrderDO> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        return orderMapper.selectPage(orderPage, wrapper);
    }
}
