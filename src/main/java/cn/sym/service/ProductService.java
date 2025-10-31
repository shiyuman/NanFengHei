package cn.sym.service;

import cn.sym.dto.*;
import cn.sym.dto.ProductAddDTO;
import cn.sym.dto.ProductDeleteDTO;
import cn.sym.dto.ProductPageQueryDTO;
import cn.sym.dto.ProductQueryDTO;
import cn.sym.dto.ProductStatusUpdateDTO;
import cn.sym.dto.ProductUpdateDTO;
import cn.sym.entity.ProductDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 商品服务接口
 * @author user
 */
public interface ProductService extends IService<ProductDO> {

    /**
     * 添加商品
     * @param productAddDTO 商品新增参数
     * @return 是否添加成功
     */
    Boolean addProduct(ProductAddDTO productAddDTO);

    /**
     * 更新商品
     * @param productUpdateDTO 商品更新参数
     * @return 是否更新成功
     */
    Boolean updateProduct(ProductUpdateDTO productUpdateDTO);

    /**
     * 删除商品
     * @param productDeleteDTO 商品删除参数
     * @return 是否删除成功
     */
    Boolean deleteProduct(ProductDeleteDTO productDeleteDTO);

    /**
     * 查询商品详情
     * @param productQueryDTO 商品查询参数
     * @return 商品信息
     */
    ProductDO getProductDetail(ProductQueryDTO productQueryDTO);

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
     * 导入商品信息
     *
     * @param products 商品列表
     * @return 是否成功
     */
    Boolean importProducts(List<ProductDO> products);

    /**
     * 添加商品
     *
     * @param productDTO 商品信息
     * @return 是否成功
     */
    Boolean addProduct(ProductDTO productDTO);

    /**
     * 删除商品
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    Boolean deleteProduct(Long productId);

    /**
     * 更新商品信息
     *
     * @param productDTO 商品信息
     * @return 是否成功
     */
    Boolean updateProduct(ProductDTO productDTO);

    /**
     * 查询商品详情
     *
     * @param productQuery 查询参数
     * @return 商品信息
     */
    ProductDO productInfo(ProductQuery productQuery);

    /**
     * 分页查询商品列表
     *
     * @param page 当前页数
     * @param size 每页条数
     * @param name 商品名称关键字
     * @param status 上下架状态
     * @return 商品分页结果
     */
    Page<ProductDO> listProducts(int page, int size, String name, Integer status);
}
