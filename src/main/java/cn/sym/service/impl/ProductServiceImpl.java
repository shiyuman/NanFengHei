package cn.sym.service.impl;

import cn.sym.dto.ProductDTO;
import cn.sym.dto.ProductQuery;
import cn.sym.entity.ProductDO;
import cn.sym.exception.BusinessException;
import cn.sym.repository.ProductMapper;
import cn.sym.service.ProductService;
import cn.sym.utils.ResultCodeConstant;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 *   商品业务逻辑实现类
 * </p>
 * @author user
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Boolean addProduct(ProductDTO productDTO) {
        // 校验商品名称是否重复
        QueryWrapper<ProductDO> wrapper = new QueryWrapper<>();
        wrapper.eq("name", productDTO.getName());
        ProductDO existingProduct = productMapper.selectOne(wrapper);
        if (existingProduct != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 插入新商品
        ProductDO productDO = new ProductDO();
        productDO.setName(productDTO.getName());
        productDO.setDescription(productDTO.getDescription());
        productDO.setCategoryId(productDTO.getCategoryId());
        productDO.setPrice(productDTO.getPrice());
        productDO.setStock(productDTO.getStock());
        productDO.setStatus(productDTO.getStatus());
        productDO.setCreateBy("system");
        productDO.setCreateTime(new Date());
        productDO.setUpdateBy("system");
        productDO.setUpdateTime(new Date());

        return productMapper.insert(productDO) > 0;
    }

    @Override
    public Boolean deleteProduct(Long productId) {
        // 确认商品是否存在
        ProductDO productDO = productMapper.selectById(productId);
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 删除商品
        return productMapper.deleteById(productId) > 0;
    }

    @Override
    public Boolean updateProduct(ProductDTO productDTO) {
        // 判断商品是否存在
        ProductDO productDO = productMapper.selectById(productDTO.getProductId());
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 更新商品信息
        productDO.setName(productDTO.getName());
        productDO.setDescription(productDTO.getDescription());
        productDO.setCategoryId(productDTO.getCategoryId());
        productDO.setPrice(productDTO.getPrice());
        productDO.setStock(productDTO.getStock());
        productDO.setStatus(productDTO.getStatus());
        productDO.setUpdateBy("system");
        productDO.setUpdateTime(new Date());

        return productMapper.updateById(productDO) > 0;
    }

    @Override
    public ProductDO productInfo(ProductQuery productQuery) {
        ProductDO productDO = productMapper.selectById(productQuery.getProductId());
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        return productDO;
    }

    @Override
    public Page<ProductDO> listProducts(int page, int size, String name, Integer status) {
        Page<ProductDO> productPage = new Page<>(page, size);
        QueryWrapper<ProductDO> wrapper = new QueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }

        if (status != null) {
            wrapper.eq("status", status);
        }

        return productMapper.selectPage(productPage, wrapper);
    }
}