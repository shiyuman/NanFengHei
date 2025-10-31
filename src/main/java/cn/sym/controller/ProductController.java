package cn.sym.controller;

import cn.sym.dto.*;
import cn.sym.dto.ProductAddDTO;
import cn.sym.dto.ProductDeleteDTO;
import cn.sym.dto.ProductPageQueryDTO;
import cn.sym.dto.ProductQueryDTO;
import cn.sym.dto.ProductStatusUpdateDTO;
import cn.sym.dto.ProductUpdateDTO;
import cn.sym.entity.ProductDO;
import cn.sym.exception.BusinessException;
import cn.sym.response.RestResult;
import cn.sym.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.groups.Default;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import cn.sym.utils.ResultCodeConstant;

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
            ProductDO result = productService.getProductDetail(productQueryDTO);
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

    /**
     * 新增商品
     *
     * @param productDTO 商品信息
     * @return RestResult 结果
     */
    @PostMapping("/add")
    @ApiOperation("新增商品")
    public RestResult<Boolean> addProduct(@RequestBody @Validated({ Default.class }) ProductDTO productDTO) {
        Boolean result = productService.addProduct(productDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 删除商品
     *
     * @param productId 商品ID
     * @return RestResult 结果
     */
    @DeleteMapping("/delete/{productId}")
    @ApiOperation("删除商品")
    public RestResult<Boolean> deleteProduct(@PathVariable Long productId) {
        Boolean result = productService.deleteProduct(productId);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 更新商品
     *
     * @param productDTO 商品信息
     * @return RestResult 结果
     */
    @PutMapping("/update")
    @ApiOperation("更新商品")
    public RestResult<Boolean> updateProduct(@RequestBody @Validated({ Default.class }) ProductDTO productDTO) {
        Boolean result = productService.updateProduct(productDTO);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 查询商品详情
     *
     * @param productQuery 查询参数
     * @return RestResult 结果
     */
    @GetMapping("/info")
    @ApiOperation("查询商品详情")
    public RestResult<ProductDO> productInfo(@Validated({ Default.class }) ProductQuery productQuery) {
        ProductDO result = productService.productInfo(productQuery);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }

    /**
     * 分页查询商品列表
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param name 商品名称关键词
     * @param status 上下架状态
     * @return RestResult 结果
     */
    @GetMapping("/list")
    @ApiOperation("分页查询商品列表")
    public RestResult<Page<ProductDO>> listProducts(@RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(required = false) String name, @RequestParam(required = false) Integer status) {
        Page<ProductDO> result = productService.listProducts(pageNo, pageSize, name, status);
        return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
    }
}
