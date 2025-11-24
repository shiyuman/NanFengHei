package cn.sym.repository;

import cn.sym.entity.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 商品数据访问接口
 * 
 * @author user
 */
@Mapper
@Repository
public interface ProductRepository extends BaseMapper<ProductDO> {
    
    /**
     * 根据商品ID和状态查询商品
     * @param id 商品ID
     * @param status 状态
     * @return 商品对象
     */
    @Select("SELECT * FROM product_info WHERE id = #{id} AND status = #{status}")
    ProductDO findByIdAndStatus(@Param("id") Long id, @Param("status") Integer status);
}