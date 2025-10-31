package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单状态变更通知参数对象
 *
 * @author user
 */
@ApiModel(description = "订单状态变更通知参数")
@Data
public class OrderStatusChangeDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    @ApiModelProperty(value = "订单编号", example = "ORD20230401001")
    private String orderNo;

    @NotNull(message = "消息内容不能为空")
    private String message;

    /**
     * 订单状态
     */
    @NotNull(message = "订单状态不能为空")
    @ApiModelProperty(value = "订单状态", example = "2")
    private Integer status;
}
