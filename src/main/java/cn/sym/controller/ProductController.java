package cn.sym.controller;

import cn.sym.dto.*;
import cn.sym.dto.ProductAddDTO;
import cn.sym.dto.ProductDeleteDTO;
import cn.sym.dto.ProductPageQueryDTO;
import cn.sym.dto.ProductQueryDTO;
import cn.sym.dto.ProductStatusUpdateDTO;
import cn.sym.dto.ProductUpdateDTO;
import cn.sym.entity.ProductDO;
import cn.sym.common.exception.BusinessException;
import cn.sym.common.response.RestResult;
import cn.sym.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 商品管理控制器
 * @author user
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(tags = "商品管理")
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    /**
     * 新增商品
     *
     * @param productAddDTO 商品信息
     * @return 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增商品")
    public RestResult<Boolean> addProduct(@RequestBody @Valid ProductAddDTO productAddDTO) {
        try {
            Boolean result = productService.addProduct(productAddDTO);
            return new RestResult<>("000000", "调用成功", result);
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
            return new RestResult<>(e.getCode(), e.getMsg(), false);
        } catch (Exception e) {
            log.error("新增商品异常", e);
            return new RestResult<>("999999", "服务器内部错误", false);
        }
    }

    /**
     * 修改商品
     *
     * @param productUpdateDTO 商品信息
     * @return 结果
     */
    @PutMapping("/update")
    @ApiOperation("修改商品")
    public RestResult<Boolean> updateProduct(@RequestBody @Valid ProductUpdateDTO productUpdateDTO) {
        try {
            Boolean result = productService.updateProduct(productUpdateDTO);
            return new RestResult<>("000000", "调用成功", result);
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
            return new RestResult<>(e.getCode(), e.getMsg(), false);
        } catch (Exception e) {
            log.error("修改商品异常", e);
            return new RestResult<>("999999", "服务器内部错误", false);
        }
    }

    /**
     * 删除商品
     *
     * @param productDeleteDTO 商品删除参数
     * @return 结果
     */
    @DeleteMapping("/delete")
    @ApiOperation("删除商品")
    public RestResult<Boolean> deleteProduct(@RequestBody @Valid ProductDeleteDTO productDeleteDTO) {
        try {
            Boolean result = productService.deleteProduct(productDeleteDTO);
            return new RestResult<>("000000", "调用成功", result);
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
            return new RestResult<>(e.getCode(), e.getMsg(), false);
        } catch (Exception e) {
            log.error("删除商品异常", e);
            return new RestResult<>("999999", "服务器内部错误", false);
        }
    }

    /**
     * 查询商品详情
     *
     * @param productQueryDTO 商品查询参数
     * @return 商品详情
     */
    @GetMapping("/detail")
    @ApiOperation("查询商品详情")
    public RestResult<ProductDO> getProductDetail(@Valid ProductQueryDTO productQueryDTO) {
        try {
            ProductDO result = productService. getProductDetail(productQueryDTO);
            return new RestResult<>("000000", "调用成功", result);
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
            return new RestResult<>(e.getCode(), e.getMsg(), null);
        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            return new RestResult<>("999999", "服务器内部错误", null);
        }
    }

    /**
     * 商品上下架
     *
     * @param productStatusUpdateDTO 商品上下架参数
     * @return 结果
     */
    @PutMapping("/status/update")
    @ApiOperation("商品上下架")
    public RestResult<Boolean> updateProductStatus(@RequestBody @Valid ProductStatusUpdateDTO productStatusUpdateDTO) {
        try {
            Boolean result = productService.updateProductStatus(productStatusUpdateDTO);
            return new RestResult<>("000000", "调用成功", result);
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
            return new RestResult<>(e.getCode(), e.getMsg(), false);
        } catch (Exception e) {
            log.error("商品上下架异常", e);
            return new RestResult<>("999999", "服务器内部错误", false);
        }
    }

    /**
     * 分页查询商品列表
     *
     * @param productPageQueryDTO 分页查询参数
     * @return 商品分页结果
     */
    @GetMapping("/list/page")
    @ApiOperation("分页查询商品列表")
    public RestResult<Page<ProductDO>> getProductList(@Valid ProductPageQueryDTO productPageQueryDTO) {
        try {
            Page<ProductDO> result = productService.getProductList(productPageQueryDTO);
            return new RestResult<>("000000", "调用成功", result);
        } catch (Exception e) {
            log.error("分页查询商品列表异常", e);
            return new RestResult<>("999999", "服务器内部错误", null);
        }
    }
}
