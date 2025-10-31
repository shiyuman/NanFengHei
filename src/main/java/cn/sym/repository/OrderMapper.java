package cn.sym.repository;

import cn.sym.entity.OrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单信息Mapper接口
 *
 * @author user
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {
}
