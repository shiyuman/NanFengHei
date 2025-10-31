package cn.sym.repository;

import cn.sym.entity.PromotionActivityDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *   促销活动数据访问层接口
 * </p>
 * @author user
 */
@Mapper
public interface PromotionActivityMapper extends BaseMapper<PromotionActivityDO> {
}