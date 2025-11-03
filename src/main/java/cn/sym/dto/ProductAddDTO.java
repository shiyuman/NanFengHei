package cn.sym.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增商品传输对象
 * @author user
 */
@Data
public class ProductAddDTO {

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 单价
     */
    @NotNull(message = "单价不能为空")
    @Min(value = 0, message = "单价必须大于等于0")
    private Integer price;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 上下架状态：1-上架，0-下架
     */
    private Integer status;
}