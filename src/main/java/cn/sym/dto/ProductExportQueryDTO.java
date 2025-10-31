package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品信息导出查询参数
 *
 * @author user
 */
@Data
@ApiModel("商品信息导出查询参数")
public class ProductExportQueryDTO {

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称")
    private String name;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    /**
     * 上下架状态：1-上架，0-下架
     */
    @ApiModelProperty(value = "上下架状态：1-上架，0-下架")
    private Integer status;
}