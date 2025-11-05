package cn.sym.repository;

import cn.sym.entity.OrderItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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
}

