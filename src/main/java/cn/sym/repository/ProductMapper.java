package cn.sym.repository;

import cn.sym.entity.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品数据访问层接口
 * @author user
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {
}
