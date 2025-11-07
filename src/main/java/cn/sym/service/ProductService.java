package cn.sym.service;

import cn.sym.dto.*;
import cn.sym.entity.ProductDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 商品服务接口
 * @author user
 */
public interface ProductService {

    /**
     * 添加商品
     * @param productAddDTO 商品新增参数
     * @return 是否添加成功
     */
    Boolean addProduct(ProductAddDTO productAddDTO);

    /**
     * 更新商品上下架状态
     * @param productStatusUpdateDTO 商品上下架参数
     * @return 是否更新成功
     */
    Boolean updateProductStatus(ProductStatusUpdateDTO productStatusUpdateDTO);

    /**
     * 分页查询商品列表
     * @param productPageQueryDTO 分页查询参数
     * @return 商品分页结果
     */
    Page<ProductDO> getProductList(ProductPageQueryDTO productPageQueryDTO);

    /**
     * 导出商品信息
     *
     * @param query 查询条件
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    void exportProducts(ProductExportQueryDTO query, HttpServletResponse response) throws IOException;

    /**
     * 导出商品信息（异步）
     *
     * @param query 查询条件
     * @return 任务ID
     */
    String exportProductsAsync(ProductExportQueryDTO query);

    /**
     * 查询导出任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    ProductExportTaskDTO getExportTaskStatus(String taskId);

    /**
     * 导入商品信息
     *
     * @param products 商品列表
     * @return 是否成功
     */
    Boolean importProducts(List<ProductDO> products);

    /**
     * 查询商品详情（带缓存）
     * @param productQueryDTO 商品查询参数
     * @return 商品信息
     */
    ProductDO getProductDetailWithCache(ProductQueryDTO productQueryDTO);

    /**
     * 更新商品（清除缓存）
     * @param productUpdateDTO 商品更新参数
     * @return 是否更新成功
     */
    Boolean updateProductWithCache(ProductUpdateDTO productUpdateDTO);

    /**
     * 删除商品（清除缓存）
     * @param productDeleteDTO 商品删除参数
     * @return 是否删除成功
     */
    Boolean deleteProductWithCache(ProductDeleteDTO productDeleteDTO);

    /**
     * 预热商品缓存
     * @param productId 商品ID
     */
    void warmUpProductCache(Long productId);
}
