package cn.sym.repository;

import cn.sym.entity.LimitPurchaseDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 限购配置数据访问接口
 *
 * @author user
 */
@Mapper
public interface LimitPurchaseMapper extends BaseMapper<LimitPurchaseDO> {
}