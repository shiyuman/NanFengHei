package cn.sym.service.impl;

import cn.sym.dto.*;
import cn.sym.dto.ProductAddDTO;
import cn.sym.dto.ProductDeleteDTO;
import cn.sym.dto.ProductPageQueryDTO;
import cn.sym.dto.ProductQueryDTO;
import cn.sym.dto.ProductStatusUpdateDTO;
import cn.sym.dto.ProductUpdateDTO;
import cn.sym.entity.ProductDO;
import cn.sym.common.exception.BusinessException;
import cn.sym.repository.ProductMapper;
import cn.sym.service.ProductService;
import cn.sym.common.constant.ResultCodeConstant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.text.SimpleDateFormat;
import java.util.List;
import org.springframework.util.CollectionUtils;

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
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    @Override
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
    public Boolean updateProduct(ProductUpdateDTO productUpdateDTO) {
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

        return productMapper.updateById(productDO) > 0;
    }

    @Override
    public Boolean deleteProduct(ProductDeleteDTO productDeleteDTO) {
        // 确认商品是否存在
        ProductDO productDO = productMapper.selectById(productDeleteDTO.getId());
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }

        // 逻辑删除商品
        return productMapper.deleteById(productDeleteDTO.getId()) > 0;
    }

    @Override
    public ProductDO getProductDetail(ProductQueryDTO productQueryDTO) {
        ProductDO productDO = productMapper.selectById(productQueryDTO.getId());
        if (productDO == null) {
            throw new BusinessException(ResultCodeConstant.CODE_000001, ResultCodeConstant.CODE_000001_MSG);
        }
        return productDO;
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
        Page<ProductDO> productPage = new Page<>(productPageQueryDTO.getPage(), productPageQueryDTO.getSize());
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();

        if (productPageQueryDTO.getName() != null && !productPageQueryDTO.getName().isEmpty()) {
            wrapper.like(ProductDO::getName, productPageQueryDTO.getName());
        }

        if (productPageQueryDTO.getStatus() != null) {
            wrapper.eq(ProductDO::getStatus, productPageQueryDTO.getStatus());
        }

        return productMapper.selectPage(productPage, wrapper);
    }

    @Override
    public void exportProducts(ProductExportQueryDTO query,HttpServletResponse response)throws IOException {
    LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
    if(query.getName() != null && !query.getName().isEmpty()){
        wrapper.like(ProductDO::getName, query.getName());
    }
    if(query.getStatus() != null){
        wrapper.eq(ProductDO::getStatus, query.getStatus());
    }
    List<ProductDO> productList = productMapper.selectList(wrapper);

    //调用POI库创建Excel文件
    Workbook workbook = new XSSFWorkbook();
    //创建Sheet页
    Sheet sheet = workbook.createSheet("商品信息");
    //创建表头行
        Row Header = sheet.createRow(0);
        Header.createCell(0).setCellValue("商品ID");
        Header.createCell(1).setCellValue("商品名称");
        Header.createCell(2).setCellValue("商品描述");
        Header.createCell(3).setCellValue("分类ID");
        Header.createCell(4).setCellValue("单价");
        Header.createCell(5).setCellValue("库存数量");
        Header.createCell(6).setCellValue("上下架状态");
        Header.createCell(7).setCellValue("创建时间");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //写入数据行
        int rowNum = 1;
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
        //自动调整列宽
       for(int i=0;i<8;i++){
            sheet.autoSizeColumn(i);
       }
       //写入响应流
        // 1. 设置响应头
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
            //记录日志
            log.error("导出商品数据，关闭Excel工作簿时发生异常", e);
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