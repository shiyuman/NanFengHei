package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增限购配置参数对象
 *
 * @author user
 */
@Data
@ApiModel("新增限购配置参数对象")
public class LimitPurchaseAddDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true)
    private Long productId;

    /**
     * 最大购买数量
     */
    @NotNull(message = "最大购买数量不能为空")
    @ApiModelProperty(value = "最大购买数量", required = true)
    private Integer maxQuantity;
}