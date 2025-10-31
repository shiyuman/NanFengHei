package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   订单查询条件传输对象
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "订单查询条件传输对象")
public class OrderQuery {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID")
    private Long orderId;
}