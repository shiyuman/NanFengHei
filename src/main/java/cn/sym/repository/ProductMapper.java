package cn.sym.repository;

import cn.sym.entity.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品数据访问层接口
 * @author user
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductDO> {

    /**
     * 根据商品ID和状态查询商品
     * @param id 商品ID
     * @param status 状态 1-上架 0-下架
     * @return 商品对象
     */
    @Select("SELECT * FROM product_info WHERE id = #{id} AND status = #{status}")
    ProductDO findByIdAndStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 扣减商品库存（使用乐观锁）
     * @param id 商品ID
     * @param quantity 扣减数量
     * @param version 版本号
     * @return 更新记录数
     */
    @Update("UPDATE product_info SET stock = stock - #{quantity}, version = version + 1 WHERE id = #{id} AND stock >= #{quantity} AND version = #{version}")
    int deductStockWithVersion(@Param("id") Long id, @Param("quantity") Integer quantity, @Param("version") Integer version);
    
    /**
     * 增加商品库存（用于回滚）
     * @param id 商品ID
     * @param quantity 增加数量
     * @param version 版本号
     * @return 更新记录数
     */
    @Update("UPDATE product_info SET stock = stock + #{quantity}, version = version + 1 WHERE id = #{id} AND version = #{version}")
    int increaseStockWithVersion(@Param("id") Long id, @Param("quantity") Integer quantity, @Param("version") Integer version);
    
    /**
     * 根据商品名称模糊查询商品列表（防止SQL注入）
     * @param name 商品名称
     * @return 商品列表数量
     */
    @Select("SELECT COUNT(*) FROM product_info WHERE name LIKE CONCAT('%', #{name}, '%')")
    int countByNameContaining(@Param("name") String name);
}
