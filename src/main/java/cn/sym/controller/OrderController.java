package cn.sym.controller;

import cn.sym.dto.CreateOrderDTO;
import cn.sym.dto.OrderQueryDTO;
import cn.sym.common.response.RestResult;
import cn.sym.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.groups.Default;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.sym.entity.OrderDO;
import lombok.RequiredArgsConstructor;
import cn.sym.common.response.ResultCodeConstant;
import cn.sym.dto.OrderQuery;
import cn.sym.dto.OrderDTO;

import cn.sym.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.groups.Default;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.sym.entity.OrderDO;
import lombok.RequiredArgsConstructor;
import cn.sym.utils.ResultCodeConstant;
import cn.sym.dto.OrderQuery;
import cn.sym.dto.OrderDTO;

/**
 * 订单控制器
 *
 * @author user
 */
@Validated
@RequiredArgsConstructor
@RestController
@Api(tags = "订单管理")
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     *
     * @param createOrderDTO 创建订单参数
     * @return 响应结果
     */
    @PostMapping("/create")
    @ApiOperation("创建订单")
    public RestResult<String> createOrder(@RequestBody @Valid CreateOrderDTO createOrderDTO) {
        return orderService.createOrder(createOrderDTO);
    }

    /**
     * 查询订单详情
     *
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @GetMapping("/detail")
    @ApiOperation("查询订单详情")
    public RestResult<Object> queryOrderDetail(@Valid OrderQueryDTO orderQueryDTO) {
        return orderService.queryOrderDetail(orderQueryDTO);
    }

    /**
     * 取消订单
     *
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public RestResult<Boolean> cancelOrder(@RequestBody @Valid OrderQueryDTO orderQueryDTO) {
        return orderService.cancelOrder(orderQueryDTO);
    }

    /**
     * 支付订单
     *
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    @PutMapping("/pay")
    @ApiOperation("支付订单")
    public RestResult<Boolean> payOrder(@RequestBody @Valid OrderQueryDTO orderQueryDTO) {
        return orderService.payOrder(orderQueryDTO);
    }

    /**
     * 新增订单
     *
     * @param orderDTO 订单信息
     * @return RestResult 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增订单")
    public RestResult<Boolean> addOrder(@RequestBody @Validated({ Default.class }) OrderDTO orderDTO) {
        Boolean result = orderService.addOrder(orderDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 更新订单状态
     *
     * @param orderDTO 订单信息
     * @return RestResult 结果
     */
    @PutMapping("/update/status")
    @ApiOperation("更新订单状态")
    public RestResult<Boolean> updateOrderStatus(@RequestBody @Validated({ Default.class }) OrderDTO orderDTO) {
        Boolean result = orderService.updateOrderStatus(orderDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 查询订单详情
     *
     * @param orderQuery 查询参数
     * @return RestResult 结果
     */
    @GetMapping("/info")
    @ApiOperation("查询订单详情")
    public RestResult<OrderDO> orderInfo(@Validated({ Default.class }) OrderQuery orderQuery) {
        OrderDO result = orderService.orderInfo(orderQuery);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 分页查询订单列表
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @param status 订单状态
     * @return RestResult 结果
     */
    @GetMapping("/list")
    @ApiOperation("分页查询订单列表")
    public RestResult<Page<OrderDO>> listOrders(@RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) Long userId, @RequestParam(required = false) Integer status) {
        Page<OrderDO> result = orderService.listOrders(pageNo, pageSize, userId, status);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }
}