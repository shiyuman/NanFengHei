package cn.sym.service.impl;

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
import cn.sym.service.MultiLevelCacheService;
import cn.sym.utils.SqlInjectionUtil;
import cn.sym.common.exception.BusinessException;
import cn.sym.repository.ProductMapper;
import cn.sym.service.ProductService;
import cn.sym.common.constant.ResultCodeConstant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ContentDisposition;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.text.SimpleDateFormat;
import java.util.List;
import org.springframework.util.CollectionUtils;
import cn.sym.entity.ProductExportTask;
import cn.sym.dto.ProductExportTaskDTO;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 *   商品业务逻辑实现类
 * </p>
 * @author user
 */
@Service
@RequiredArgsConstructor
@EnableAsync
public class ProductServiceImpl implements ProductService {
    
    private final StringRedisTemplate redisTemplate;
    private final ProductMapper productMapper;
    private final MultiLevelCacheService multiLevelCacheService;
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    
    @Override
    @Idempotent(keyStrategy = IdempotentKeyStrategy.REQUEST_ID)
    public Boolean addProduct(ProductAddDTO productAddDTO) {
        // 校验商品名称是否重复
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDO::getName, productAddDTO.getName());
        ProductDO existingProduct = productMapper.selectOne(wrapper);
        if (existingProduct != null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 插入新商品
        ProductDO productDO = new ProductDO();
        productDO.setName(productAddDTO.getName());
        productDO.setDescription(productAddDTO.getDescription());
        productDO.setCategoryId(productAddDTO.getCategoryId());
        productDO.setPrice(productAddDTO.getPrice());
        productDO.setStock(productAddDTO.getStock());
        productDO.setStatus(productAddDTO.getStatus());

        return productMapper.insert(productDO) > 0;
    }
    
    @Override
    public ProductDO getProductDetailWithCache(ProductQueryDTO productQueryDTO) {
        return multiLevelCacheService.getProductDetailWithCache(productQueryDTO.getId());
    }
    
    @Override
    public Boolean updateProductWithCache(ProductUpdateDTO productUpdateDTO) {
        // 判断商品是否存在
        ProductDO productDO = productMapper.selectById(productUpdateDTO.getId());
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 更新商品信息
        productDO.setName(productUpdateDTO.getName());
        productDO.setDescription(productUpdateDTO.getDescription());
        productDO.setCategoryId(productUpdateDTO.getCategoryId());
        productDO.setPrice(productUpdateDTO.getPrice());
        productDO.setStock(productUpdateDTO.getStock());
        productDO.setStatus(productUpdateDTO.getStatus());
        
        // 更新商品并清除缓存
        multiLevelCacheService.updateProductAndClearCache(productDO);
        return true;
    }
    @Override
    public Boolean deleteProductWithCache(ProductDeleteDTO productDeleteDTO) {
        // 确认商品是否存在
        ProductDO productDO = productMapper.selectById(productDeleteDTO.getId());
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 删除商品并清除缓存
        multiLevelCacheService.deleteProductAndClearCache(productDeleteDTO.getId());
        return true;
    }
    
    @Override
    public void warmUpProductCache(Long productId) {
        multiLevelCacheService.warmUpProductCache(productId);
    }
    
    @Override
    public Boolean updateProductStatus(ProductStatusUpdateDTO productStatusUpdateDTO) {
        // 判断商品是否存在
        ProductDO productDO = productMapper.selectById(productStatusUpdateDTO.getId());
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 更新商品状态
        productDO.setStatus(productStatusUpdateDTO.getStatus());

        return productMapper.updateById(productDO) > 0;
    }

    @Override
    public Page<ProductDO> getProductList(ProductPageQueryDTO productPageQueryDTO) {
        // 检查参数是否包含SQL注入风险
        if (productPageQueryDTO.getName() != null && !SqlInjectionUtil.isSqlSafe(productPageQueryDTO.getName())) {
            throw new BusinessException("000001", "参数包含非法字符");
        }
        
        Page<ProductDO> productPage = new Page<>(productPageQueryDTO.getPage(), productPageQueryDTO.getSize());
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();

        // 处理游标分页，避免深度分页性能问题
        if (productPageQueryDTO.getCursorId() != null) {
            // 使用游标分页，根据排序方式处理
            if ("desc".equalsIgnoreCase(productPageQueryDTO.getOrderType())) {
                wrapper.lt(ProductDO::getId, productPageQueryDTO.getCursorId());
            } else {
                wrapper.gt(ProductDO::getId, productPageQueryDTO.getCursorId());
            }
        }

        if (productPageQueryDTO.getName() != null && !productPageQueryDTO.getName().isEmpty()) {
            wrapper.like(ProductDO::getName, productPageQueryDTO.getName());
        }

        if (productPageQueryDTO.getStatus() != null) {
            wrapper.eq(ProductDO::getStatus, productPageQueryDTO.getStatus());
        }
        
        // 根据参数设置排序规则
        if ("create_time".equalsIgnoreCase(productPageQueryDTO.getOrderBy())) {
            if ("desc".equalsIgnoreCase(productPageQueryDTO.getOrderType())) {
                wrapper.orderByDesc(ProductDO::getCreateTime);
            } else {
                wrapper.orderByAsc(ProductDO::getCreateTime);
            }
        } else if ("id".equalsIgnoreCase(productPageQueryDTO.getOrderBy())) {
            if ("desc".equalsIgnoreCase(productPageQueryDTO.getOrderType())) {
                wrapper.orderByDesc(ProductDO::getId);
            } else {
                wrapper.orderByAsc(ProductDO::getId);
            }
        } else {
            // 默认按照创建时间倒序排列
            wrapper.orderByDesc(ProductDO::getCreateTime);
        }

        return productMapper.selectPage(productPage, wrapper);
    }

    @Override
    public void exportProducts(ProductExportQueryDTO query,HttpServletResponse response)throws IOException {
        // 使用分页查询避免一次性加载大量数据到内存
        final int pageSize = 1000; // 每页查询1000条记录
        int current = 1;
        boolean hasMore = true;
        
        // 创建Excel工作簿和表单
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品信息");
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("商品ID");
        headerRow.createCell(1).setCellValue("商品名称");
        headerRow.createCell(2).setCellValue("商品描述");
        headerRow.createCell(3).setCellValue("分类ID");
        headerRow.createCell(4).setCellValue("单价");
        headerRow.createCell(5).setCellValue("库存数量");
        headerRow.createCell(6).setCellValue("上下架状态");
        headerRow.createCell(7).setCellValue("创建时间");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        
        // 分批查询并写入Excel
        while (hasMore) {
            Page<ProductDO> page = new Page<>(current, pageSize);
            LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
            
            if(query.getName() != null && !query.getName().isEmpty()){
                wrapper.like(ProductDO::getName, query.getName());
            }
            if(query.getStatus() != null){
                wrapper.eq(ProductDO::getStatus, query.getStatus());
            }
            if (query.getCategoryId() != null) {
                wrapper.eq(ProductDO::getCategoryId, query.getCategoryId());
            }
            
            // 按创建时间倒序排列
            wrapper.orderByDesc(ProductDO::getCreateTime);
            
            Page<ProductDO> productPage = productMapper.selectPage(page, wrapper);
            List<ProductDO> productList = productPage.getRecords();
            
            // 写入数据行
            for(ProductDO product:productList){
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getDescription());
                row.createCell(3).setCellValue(product.getCategoryId());
                row.createCell(4).setCellValue(product.getPrice());
                row.createCell(5).setCellValue(product.getStock());
                row.createCell(6).setCellValue(product.getStatus() == 1 ? "上架" : "下架");
                if(product.getCreateTime() != null) {
                    row.createCell(7).setCellValue(sdf.format(product.getCreateTime()));
                }
            }
            
            // 判断是否还有更多数据
            hasMore = productPage.getCurrent() < productPage.getPages();
            current++;
        }
        
        // 自动调整列宽
        for(int i=0;i<8;i++){
            sheet.autoSizeColumn(i);
        }
        
        // 写入响应流
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 2. 使用 Spring 框架的 ContentDisposition 工具类：会自动生成符合 RFC 5987 标准的 filename* 参数
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("商品数据.xlsx", StandardCharsets.UTF_8)
                .build();
        // 将 ContentDisposition 对象转换为标准 HTTP 头字符串
        response.setHeader("Content-Disposition", contentDisposition.toString());
        // 将 Excel 工作簿写入响应输出流
        workbook.write(response.getOutputStream());
        try{
            workbook.close();
        }catch(IOException e){
            log.error("导出商品数据，关闭Excel工作簿时发生异常", e);
        }
    }
    
    @Override
    public String exportProductsAsync(ProductExportQueryDTO query) {
        String taskId = UUID.randomUUID().toString();
        ProductExportTask task = new ProductExportTask();
        task.setTaskId(taskId);
        task.setStatus(0); // 处理中
        task.setCreateTime(new Date());
        
        // 将任务信息存储到Redis中
        try {
            String taskJson = objectMapper.writeValueAsString(task);
            redisTemplate.opsForValue().set("export_task:" + taskId, taskJson);
        } catch (Exception e) {
            log.error("存储导出任务到Redis失败", e);
        }
        
        // 异步执行导出任务
        doExportProductsAsync(query, taskId);
        
        return taskId;
    }
    
    @Async
    public void doExportProductsAsync(ProductExportQueryDTO query, String taskId) {
        FileOutputStream fos = null;
        try {
            // 使用分页查询避免一次性加载大量数据到内存
            final int pageSize = 1000; // 每页查询1000条记录
            int current = 1;
            boolean hasMore = true;
            
            // 生成临时文件路径
            String filePath = System.getProperty("java.io.tmpdir") + "/product_export_" + taskId + ".xlsx";
            
            // 创建Excel工作簿和表单
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("商品信息");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("商品ID");
            headerRow.createCell(1).setCellValue("商品名称");
            headerRow.createCell(2).setCellValue("商品描述");
            headerRow.createCell(3).setCellValue("分类ID");
            headerRow.createCell(4).setCellValue("单价");
            headerRow.createCell(5).setCellValue("库存数量");
            headerRow.createCell(6).setCellValue("上下架状态");
            headerRow.createCell(7).setCellValue("创建时间");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            
            // 分批查询并写入Excel
            while (hasMore) {
                Page<ProductDO> page = new Page<>(current, pageSize);
                LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
                
                if(query.getName() != null && !query.getName().isEmpty()){
                    wrapper.like(ProductDO::getName, query.getName());
                }
                if(query.getStatus() != null){
                    wrapper.eq(ProductDO::getStatus, query.getStatus());
                }
                if (query.getCategoryId() != null) {
                    wrapper.eq(ProductDO::getCategoryId, query.getCategoryId());
                }
                
                // 按创建时间倒序排列
                wrapper.orderByDesc(ProductDO::getCreateTime);
                
                Page<ProductDO> productPage = productMapper.selectPage(page, wrapper);
                List<ProductDO> productList = productPage.getRecords();
                
                // 写入数据行
                for(ProductDO product:productList){
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(product.getId());
                    row.createCell(1).setCellValue(product.getName());
                    row.createCell(2).setCellValue(product.getDescription());
                    row.createCell(3).setCellValue(product.getCategoryId());
                    row.createCell(4).setCellValue(product.getPrice());
                    row.createCell(5).setCellValue(product.getStock());
                    row.createCell(6).setCellValue(product.getStatus() == 1 ? "上架" : "下架");
                    if(product.getCreateTime() != null) {
                        row.createCell(7).setCellValue(sdf.format(product.getCreateTime()));
                    }
                }
                
                // 判断是否还有更多数据
                hasMore = productPage.getCurrent() < productPage.getPages();
                current++;
                
                // 释放内存中的部分数据
                productList.clear();
            }
            
            // 自动调整列宽
            for(int i=0;i<8;i++){
                sheet.autoSizeColumn(i);
            }
            
            // 写入临时文件
            fos = new FileOutputStream(filePath);
            workbook.write(fos);
            workbook.close();
            
            // 更新任务状态
            updateTaskStatus(taskId, 1, filePath, null); // 1表示已完成
        } catch (Exception e) {
            log.error("导出商品数据异常", e);
            // 更新任务状态为失败
            updateTaskStatus(taskId, 2, null, e.getMessage()); // 2表示失败
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("关闭文件输出流异常", e);
                }
            }
        }
    }
    
    private void updateTaskStatus(String taskId, Integer status, String filePath, String errorMsg) {
        try {
            String taskJson = redisTemplate.opsForValue().get("export_task:" + taskId);
            if (taskJson != null) {
                ProductExportTask task = objectMapper.readValue(taskJson, ProductExportTask.class);
                task.setStatus(status);
                if (filePath != null) {
                    task.setFilePath(filePath);
                }
                if (errorMsg != null) {
                    task.setErrorMsg(errorMsg);
                }
                task.setFinishTime(new Date());
                
                // 更新Redis中的任务信息
                String updatedTaskJson = objectMapper.writeValueAsString(task);
                redisTemplate.opsForValue().set("export_task:" + taskId, updatedTaskJson);
            }
        } catch (Exception e) {
            log.error("更新任务状态到Redis失败", e);
        }
    }
    
    @Override
    public ProductExportTaskDTO getExportTaskStatus(String taskId) {
        try {
            String taskJson = redisTemplate.opsForValue().get("export_task:" + taskId);
            if (taskJson == null) {
                return null;
            }
            
            ProductExportTask task = objectMapper.readValue(taskJson, ProductExportTask.class);
            
            ProductExportTaskDTO dto = new ProductExportTaskDTO();
            dto.setTaskId(task.getTaskId());
            dto.setStatus(task.getStatus());
            dto.setErrorMsg(task.getErrorMsg());
            
            if (task.getStatus() == 1) {
                // 任务已完成，提供下载地址
                dto.setDownloadUrl("/product/export/download?taskId=" + taskId);
            }
            
            return dto;
        } catch (Exception e) {
            log.error("获取任务状态失败", e);
            return null;
        }
    }
    
    @Override
       public Boolean importProducts(List<ProductDO> products) {
        if (CollectionUtils.isEmpty(products)) {
            log.warn("导入商品数据为空");
            return false;
        }
        try {
            for (ProductDO product : products) {
                // 数据校验
                if (product.getName() == null || product.getName().trim().isEmpty()) {
                    log.warn("商品名称不能为空");
                    continue;
                }

                if (product.getPrice() == null || product.getPrice() < 0) {
                    log.warn("商品价格不能低于0: {}", product.getPrice());
                    continue;
                }

                if (product.getStock() == null || product.getStock() < 0) {
                    log.warn("商品库存不能低于0: {}", product.getStock());
                    continue;
                }

                // 设置默认值
                if (product.getStatus() == null) {
                    product.setStatus(0); // 默认下架状态
                }

                product.setCreateBy("system");
                product.setCreateTime(new Date());
                product.setUpdateBy("system");
                product.setUpdateTime(new Date());

                productMapper.insert(product);
            }
            return true;
        } catch (Exception e) {
            log.error("导入商品数据时发生异常", e);
            return false;
        }
    }

}