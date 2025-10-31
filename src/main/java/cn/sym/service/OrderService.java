package cn.sym.service;

import cn.sym.dto.CreateOrderDTO;
import cn.sym.dto.OrderQueryDTO;
import cn.sym.response.RestResult;
import java.util.List;
import cn.sym.entity.OrderDO;
import cn.sym.dto.OrderExportQueryDTO;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.sym.dto.OrderQuery;
import cn.sym.dto.OrderDTO;

/**
 * 订单服务接口
 *
 * @author user
 */
public interface OrderService {

    /**
     * 创建订单
     * @param createOrderDTO 创建订单参数
     * @return 响应结果
     */
    RestResult<String> createOrder(CreateOrderDTO createOrderDTO);

    /**
     * 查询订单详情
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    RestResult<Object> queryOrderDetail(OrderQueryDTO orderQueryDTO);

    /**
     * 取消订单
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    RestResult<Boolean> cancelOrder(OrderQueryDTO orderQueryDTO);

    /**
     * 支付订单
     * @param orderQueryDTO 查询订单参数
     * @return 响应结果
     */
    RestResult<Boolean> payOrder(OrderQueryDTO orderQueryDTO);

    /**
     * 导出订单信息
     *
     * @param query 查询条件
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    void exportOrders(OrderExportQueryDTO query, HttpServletResponse response) throws IOException;

    /**
     * 导入订单信息
     *
     * @param orders 订单列表
     * @return 是否成功
     */
    Boolean importOrders(List<OrderDO> orders);

    /**
     * 添加订单
     *
     * @param orderDTO 订单信息
     * @return 是否成功
     */
    Boolean addOrder(OrderDTO orderDTO);

    /**
     * 更新订单状态
     *
     * @param orderDTO 订单信息
     * @return 是否成功
     */
    Boolean updateOrderStatus(OrderDTO orderDTO);

    /**
     * 查询订单详情
     *
     * @param orderQuery 查询参数
     * @return 订单信息
     */
    OrderDO orderInfo(OrderQuery orderQuery);

    /**
     * 分页查询订单列表
     *
     * @param page 当前页数
     * @param size 每页条数
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单分页结果
     */
    Page<OrderDO> listOrders(int page, int size, Long userId, Integer status);
}
