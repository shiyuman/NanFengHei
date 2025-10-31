package cn.sym.controller;

import cn.sym.dto.FileImportDTO;
import cn.sym.dto.OrderExportQueryDTO;
import cn.sym.dto.ProductExportQueryDTO;
import cn.sym.dto.UserExportQueryDTO;
import cn.sym.entity.OrderDO;
import cn.sym.entity.ProductDO;
import cn.sym.entity.UserDO;
import cn.sym.response.RestResult;
import cn.sym.response.ResultCodeConstant;
import cn.sym.service.OrderService;
import cn.sym.service.ProductService;
import cn.sym.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据导入导出控制器
 *
 * @author user
 */
@Slf4j
@RestController
@RequestMapping("/data")
@Api(tags = "数据导入导出管理")
public class DataImportExportController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    /**
     * 导出用户信息
     *
     * @param query 查询条件
     * @param response HTTP响应对象
     * @return RestResult结果
     */
    @PostMapping("/export/users")
    @ApiOperation("导出用户信息")
    public RestResult<Boolean> exportUsers(@RequestBody UserExportQueryDTO query,
                                           HttpServletResponse response) {
        try {
            userService.exportUsers(query, response);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
        } catch (IOException e) {
            log.error("导出用户信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_000001, "导出失败", false);
        }
    }

    /**
     * 导入用户信息
     *
     * @param dto 包含上传文件的DTO对象
     * @return RestResult结果
     */
    @PostMapping("/import/users")
    @ApiOperation("导入用户信息")
    public RestResult<Boolean> importUsers(@Validated @ModelAttribute FileImportDTO dto) {
        MultipartFile file = dto.getFile();
        if (file == null || file.isEmpty()) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "请选择要上传的文件", false);
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "文件格式不正确，请上传Excel文件", false);
        }

        try {
            List<UserDO> users = readUsersFromExcel(file.getInputStream());
            Boolean result = userService.importUsers(users);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
        } catch (Exception e) {
            log.error("导入用户信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_000001, "导入失败：" + e.getMessage(), false);
        }
    }

    /**
     * 从Excel中读取用户数据
     *
     * @param inputStream Excel输入流
     * @return 用户列表
     * @throws Exception 异常
     */
    private List<UserDO> readUsersFromExcel(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0); // 第一个工作表

        List<UserDO> users = new ArrayList<>();

        // 从第二行开始遍历（第一行为标题）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            UserDO user = new UserDO();
            Cell cell = row.getCell(0);
            if (cell != null) {
                user.setId((long) cell.getNumericCellValue()); // 用户ID
            }

            cell = row.getCell(1);
            if (cell != null) {
                user.setUsername(cell.getStringCellValue()); // 用户名
            }

            cell = row.getCell(2);
            if (cell != null) {
                user.setPhone(cell.getStringCellValue()); // 手机号
            }

            cell = row.getCell(3);
            if (cell != null) {
                String statusStr = cell.getStringCellValue();
                user.setStatus(statusStr.equals("正常") ? 1 : 0); // 账户状态
            }

            cell = row.getCell(4);
            if (cell != null) {
                Date date = cell.getDateCellValue(); // 创建时间
                user.setCreateTime(date);
            }

            users.add(user);
        }

        workbook.close();
        return users;
    }

    /**
     * 导出商品信息
     *
     * @param query 查询条件
     * @param response HTTP响应对象
     * @return RestResult结果
     */
    @PostMapping("/export/products")
    @ApiOperation("导出商品信息")
    public RestResult<Boolean> exportProducts(@RequestBody ProductExportQueryDTO query,
                                              HttpServletResponse response) {
        try {
            productService.exportProducts(query, response);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
        } catch (IOException e) {
            log.error("导出商品信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_000001, "导出失败", false);
        }
    }

    /**
     * 导入商品信息
     *
     * @param dto 包含上传文件的DTO对象
     * @return RestResult结果
     */
    @PostMapping("/import/products")
    @ApiOperation("导入商品信息")
    public RestResult<Boolean> importProducts(@Validated @ModelAttribute FileImportDTO dto) {
        MultipartFile file = dto.getFile();
        if (file == null || file.isEmpty()) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "请选择要上传的文件", false);
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "文件格式不正确，请上传Excel文件", false);
        }

        try {
            List<ProductDO> products = readProductsFromExcel(file.getInputStream());
            Boolean result = productService.importProducts(products);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
        } catch (Exception e) {
            log.error("导入商品信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_000001, "导入失败：" + e.getMessage(), false);
        }
    }

    /**
     * 从Excel中读取商品数据
     *
     * @param inputStream Excel输入流
     * @return 商品列表
     * @throws Exception 异常
     */
    private List<ProductDO> readProductsFromExcel(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0); // 第一个工作表

        List<ProductDO> products = new ArrayList<>();

        // 从第二行开始遍历（第一行为标题）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            ProductDO product = new ProductDO();
            Cell cell = row.getCell(0);
            if (cell != null) {
                product.setId((long) cell.getNumericCellValue()); // 商品ID
            }

            cell = row.getCell(1);
            if (cell != null) {
                product.setName(cell.getStringCellValue()); // 商品名称
            }

            cell = row.getCell(2);
            if (cell != null) {
                product.setDescription(cell.getStringCellValue()); // 商品描述
            }

            cell = row.getCell(3);
            if (cell != null) {
                product.setCategoryId((long) cell.getNumericCellValue()); // 分类ID
            }

            cell = row.getCell(4);
            if (cell != null) {
                product.setPrice((int) cell.getNumericCellValue()); // 单价
            }

            cell = row.getCell(5);
            if (cell != null) {
                product.setStock((int) cell.getNumericCellValue()); // 库存数量
            }

            cell = row.getCell(6);
            if (cell != null) {
                String statusStr = cell.getStringCellValue();
                product.setStatus(statusStr.equals("上架") ? 1 : 0); // 上下架状态
            }

            cell = row.getCell(7);
            if (cell != null) {
                Date date = cell.getDateCellValue(); // 创建时间
                product.setCreateTime(date);
            }

            products.add(product);
        }

        workbook.close();
        return products;
    }

    /**
     * 导出订单信息
     *
     * @param query 查询条件
     * @param response HTTP响应对象
     * @return RestResult结果
     */
    @PostMapping("/export/orders")
    @ApiOperation("导出订单信息")
    public RestResult<Boolean> exportOrders(@RequestBody OrderExportQueryDTO query,
                                            HttpServletResponse response) {
        try {
            orderService.exportOrders(query, response);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, true);
        } catch (IOException e) {
            log.error("导出订单信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_000001, "导出失败", false);
        }
    }

    /**
     * 导入订单信息
     *
     * @param dto 包含上传文件的DTO对象
     * @return RestResult结果
     */
    @PostMapping("/import/orders")
    @ApiOperation("导入订单信息")
    public RestResult<Boolean> importOrders(@Validated @ModelAttribute FileImportDTO dto) {
        MultipartFile file = dto.getFile();
        if (file == null || file.isEmpty()) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "请选择要上传的文件", false);
        }

        // 检查文件类型
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return new RestResult<>(ResultCodeConstant.CODE_000001, "文件格式不正确，请上传Excel文件", false);
        }

        try {
            List<OrderDO> orders = readOrdersFromExcel(file.getInputStream());
            Boolean result = orderService.importOrders(orders);
            return new RestResult<>(ResultCodeConstant.CODE_000000, ResultCodeConstant.CODE_000000_MSG, result);
        } catch (Exception e) {
            log.error("导入订单信息失败", e);
            return new RestResult<>(ResultCodeConstant.CODE_000001, "导入失败：" + e.getMessage(), false);
        }
    }

    /**
     * 从Excel中读取订单数据
     *
     * @param inputStream Excel输入流
     * @return 订单列表
     * @throws Exception 异常
     */
    private List<OrderDO> readOrdersFromExcel(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0); // 第一个工作表

        List<OrderDO> orders = new ArrayList<>();

        // 从第二行开始遍历（第一行为标题）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            OrderDO order = new OrderDO();
            Cell cell = row.getCell(0);
            if (cell != null) {
                order.setId((long) cell.getNumericCellValue()); // 订单ID
            }

            cell = row.getCell(1);
            if (cell != null) {
                order.setUserId((long) cell.getNumericCellValue()); // 用户ID
            }

            cell = row.getCell(2);
            if (cell != null) {
                order.setOrderNo(cell.getStringCellValue()); // 订单编号
            }

            cell = row.getCell(3);
            if (cell != null) {
                order.setTotalAmount(new BigDecimal(cell.getStringCellValue())); // 订单总金额
            }

            cell = row.getCell(4);
            if (cell != null) {
                String typeStr = cell.getStringCellValue();
                order.setDeliveryType(typeStr.equals("自取") ? 1 : 2); // 配送方式
            }

            cell = row.getCell(5);
            if (cell != null) {
                String statusStr = cell.getStringCellValue();
                switch (statusStr) {
                    case "待支付":
                        order.setStatus(1);
                        break;
                    case "已支付":
                        order.setStatus(2);
                        break;
                    case "已完成":
                        order.setStatus(3);
                        break;
                    case "已取消":
                        order.setStatus(4);
                        break;
                    default:
                        order.setStatus(0);
                }
            }

            cell = row.getCell(6);
            if (cell != null) {
                Date date = cell.getDateCellValue(); // 创建时间
                order.setCreateTime(date);
            }

            orders.add(order);
        }

        workbook.close();
        return orders;
    }
}