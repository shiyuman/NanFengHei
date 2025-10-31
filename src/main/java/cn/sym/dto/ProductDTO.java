package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   商品信息传输对象
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "商品信息传输对象")
public class ProductDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID")
    private Long productId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @ApiModelProperty(value = "商品名称")
    private String name;

    /**
     * 商品描述
     */
    @ApiModelProperty(value = "商品描述")
    private String description;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    /**
     * 单价
     */
    @NotNull(message = "单价不能为空")
    @ApiModelProperty(value = "单价")
    private Integer price;

    /**
     * 库存数量
     */
    @ApiModelProperty(value = "库存数量")
    private Integer stock;

    /**
     * 上下架状态
     */
    @ApiModelProperty(value = "上下架状态")
    private Integer status;
}