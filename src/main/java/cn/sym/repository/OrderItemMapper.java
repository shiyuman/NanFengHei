package cn.sym.repository;

import cn.sym.entity.OrderItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单详情数据访问层接口
 * @author user
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemDO> {

    /**
     * 根据订单ID查询订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItemDO> selectByOrderId(Long orderId);
    
    /**
     * 查询用户已购买的特定商品数量
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 已购买数量
     */
    @Select("SELECT COALESCE(SUM(oi.quantity), 0) FROM order_item oi " +
           "JOIN order_info o ON oi.order_id = o.id " +
           "WHERE o.user_id = #{userId} AND oi.product_id = #{productId} " +
           "AND o.status IN (2, 3)") // 只统计已支付和已完成的订单
    Integer selectPurchasedQuantityByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}

