package cn.sym.repository;

import cn.sym.entity.OrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单数据访问层接口
 * @author user
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    /**
     * 根据请求ID查询订单
     * @param requestId 请求ID
     * @return 订单对象
     */
    @Select("SELECT * FROM order_info WHERE request_id = #{requestId}")
    OrderDO selectByRequestId(@Param("requestId") String requestId);
    
    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单对象
     */
    @Select("SELECT * FROM order_info WHERE order_no = #{orderNo}")
    OrderDO selectByOrderNo(@Param("orderNo") String orderNo);
}
