package cn.sym.controller;

import cn.sym.common.annotation.Idempotent;
import cn.sym.common.annotation.IdempotentKeyStrategy;
import cn.sym.dto.*;
import cn.sym.dto.ProductAddDTO;
import cn.sym.dto.ProductDeleteDTO;
import cn.sym.dto.ProductPageQueryDTO;
import cn.sym.dto.ProductQueryDTO;
import cn.sym.dto.ProductStatusUpdateDTO;
import cn.sym.dto.ProductUpdateDTO;
import cn.sym.entity.ProductDO;
import cn.sym.utils.SqlInjectionUtil;
import cn.sym.common.exception.BusinessException;
import cn.sym.common.response.DesensitizedRestResult;
import cn.sym.utils.DataDesensitizeUtil;
import cn.sym.common.response.RestResult;
import cn.sym.entity.ProductExportTask;
import cn.sym.service.ProductService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

/**
 * 商品管理控制器
 * @author user
 */
@Slf4j
@RequiredArgsConstructor
@RestController

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
    @Idempotent(keyStrategy = IdempotentKeyStrategy.REQUEST_ID)
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
     * 查询商品详情
     *
     * @param productQueryDTO 商品查询参数
     * @return 商品详情
     */
    @GetMapping("/detail")
    public RestResult<ProductDO> getProductDetail(@Valid ProductQueryDTO productQueryDTO) {
        try {
            ProductDO result = productService.getProductDetailWithCache(productQueryDTO);
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
     * 查询商品详情（脱敏版本）
     *
     * @param productQueryDTO 商品查询参数
     * @return 商品详情
     */
    @GetMapping("/detail/desensitized")
    public DesensitizedRestResult<ProductDO> getProductDetailDesensitized(@Valid ProductQueryDTO productQueryDTO) {
        try {
            ProductDO result = productService.getProductDetailWithCache(productQueryDTO);
            // 对结果进行脱敏处理
            ProductDO desensitizedResult = DataDesensitizeUtil.desensitize(result);
            return DesensitizedRestResult.desensitized("000000", "调用成功", desensitizedResult);
        } catch (BusinessException e) {
            log.error(e.getMessage(), e);
            return new DesensitizedRestResult<>(e.getCode(), e.getMsg(), null);
        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            return new DesensitizedRestResult<>("999999", "服务器内部错误", null);
        }
    }

    /**
     * 修改商品
     *
     * @param productUpdateDTO 商品信息
     * @return 结果
     */
    @PutMapping("/update")
    public RestResult<Boolean> updateProduct(@RequestBody @Valid ProductUpdateDTO productUpdateDTO) {
        try {
            Boolean result = productService.updateProductWithCache(productUpdateDTO);
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
    public RestResult<Boolean> deleteProduct(@RequestBody @Valid ProductDeleteDTO productDeleteDTO) {
        try {
            Boolean result = productService.deleteProductWithCache(productDeleteDTO);
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
     * 预热商品缓存
     *
     * @param productId 商品ID
     * @return 结果
     */
    @PostMapping("/cache/warmup")
    public RestResult<Boolean> warmUpProductCache(@RequestParam Long productId) {
        try {
            productService.warmUpProductCache(productId);
            return new RestResult<>("000000", "调用成功", true);
        } catch (Exception e) {
            log.error("预热商品缓存异常", e);
            return new RestResult<>("999999", "服务器内部错误", false);
        }
    }

    /**
     * 商品上下架
     *
     * @param productStatusUpdateDTO 商品上下架参数
     * @return 结果
     */
    @PutMapping("/status/update")
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
    public RestResult<Page<ProductDO>> getProductList(@Valid ProductPageQueryDTO productPageQueryDTO) {
        try {
            // 检查参数是否包含SQL注入风险
            if (productPageQueryDTO.getName() != null && !SqlInjectionUtil.isSqlSafe(productPageQueryDTO.getName())) {
                return new RestResult<>("000001", "参数包含非法字符", null);
            }
            
            Page<ProductDO> result = productService.getProductList(productPageQueryDTO);
            return new RestResult<>("000000", "调用成功", result);
        } catch (Exception e) {
            log.error("分页查询商品列表异常", e);
            return new RestResult<>("999999", "服务器内部错误", null);
        }
    }
    
    /**
     * 异步导出商品信息
     *
     * @param productExportQueryDTO 查询条件
     * @return 任务ID
     */
    @PostMapping("/export/async")
    public RestResult<String> exportProductsAsync(@RequestBody ProductExportQueryDTO productExportQueryDTO) {
        try {
            String taskId = productService.exportProductsAsync(productExportQueryDTO);
            return new RestResult<>("000000", "调用成功", taskId);
        } catch (Exception e) {
            log.error("异步导出商品信息异常", e);
            return new RestResult<>("999999", "服务器内部错误", null);
        }
    }
    
    /**
     * 查询导出任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/export/status")
    public RestResult<ProductExportTaskDTO> getExportTaskStatus(@RequestParam String taskId) {
        try {
            ProductExportTaskDTO result = productService.getExportTaskStatus(taskId);
            if (result == null) {
                return new RestResult<>("000001", "任务不存在", null);
            }
            return new RestResult<>("000000", "调用成功", result);
        } catch (Exception e) {
            log.error("查询导出任务状态异常", e);
            return new RestResult<>("999999", "服务器内部错误", null);
        }
    }
    
    /**
     * 下载导出文件
     *
     * @param taskId 任务ID
     * @param response HTTP响应对象
     */
    @GetMapping("/export/download")
    public void downloadExportFile(@RequestParam String taskId, HttpServletResponse response) {
        try {
            ProductExportTaskDTO taskDTO = productService.getExportTaskStatus(taskId);
            if (taskDTO == null || taskDTO.getStatus() != 1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            ProductExportTask task = null; // 这里需要从实际存储中获取任务信息
            String filePath = null; // 从任务信息中获取文件路径
            
            // 读取文件并写入响应
            File file = new File(filePath);
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename("商品数据.xlsx", StandardCharsets.UTF_8)
                    .build();
            response.setHeader("Content-Disposition", contentDisposition.toString());
            
            // 写入文件内容
            try (FileInputStream fis = new FileInputStream(file);
                 ServletOutputStream sos = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    sos.write(buffer, 0, len);
                }
                sos.flush();
            }
            
            // 删除临时文件（可选）
            // file.delete();
        } catch (Exception e) {
            log.error("下载导出文件异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
