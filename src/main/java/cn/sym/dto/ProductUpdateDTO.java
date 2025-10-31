package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新商品传输对象
 * @author user
 */
@Data
public class ProductUpdateDTO {

    /**
     * 商品唯一标识
     */
    @NotNull(message = "商品ID不能为空")
    private Long id;

    /**
     * 商品名称
     */
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