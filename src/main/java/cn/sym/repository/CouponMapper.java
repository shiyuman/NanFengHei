package cn.sym.repository;

import cn.sym.entity.CouponDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *   优惠券数据访问接口
 * </p>
 * @author user
 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {
}