package cn.sym.repository;

import cn.sym.entity.ProductDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品数据访问接口
 * 
 * @author user
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductDO, Long> {
    
    /**
     * 根据商品ID和状态查询商品
     * @param id 商品ID
     * @param status 状态
     * @return 商品对象
     */
    ProductDO findByIdAndStatus(Long id, Integer status);
}