package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询限购配置详情参数对象
 *
 * @author user
 */
@Data
@ApiModel("查询限购配置详情参数对象")
public class LimitPurchaseQueryDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true)
    private Long productId;
}