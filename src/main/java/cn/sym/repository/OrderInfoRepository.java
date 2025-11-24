package cn.sym.repository;

import cn.sym.entity.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *   订单信息数据访问层接口
 * </p>
 * @author user
 */
@Mapper
@Repository
public interface OrderInfoRepository extends BaseMapper<OrderInfo> {
}