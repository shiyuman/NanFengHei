package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑限购配置参数对象
 *
 * @author user
 */
@Data
@ApiModel("编辑限购配置参数对象")
public class LimitPurchaseEditDTO {

    /**
     * 限购记录ID
     */
    @NotNull(message = "限购记录ID不能为空")
    @ApiModelProperty(value = "限购记录ID", required = true)
    private Long id;

    /**
     * 最大购买数量
     */
    @NotNull(message = "最大购买数量不能为空")
    @ApiModelProperty(value = "最大购买数量", required = true)
    private Integer maxQuantity;
}