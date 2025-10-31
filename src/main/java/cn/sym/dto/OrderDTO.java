package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   订单信息传输对象
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "订单信息传输对象")
public class OrderDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    /**
     * 下单用户ID
     */
    @NotNull(message = "下单用户ID不能为空")
    @ApiModelProperty(value = "下单用户ID")
    private Long userId;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    /**
     * 订单总金额
     */
    @NotNull(message = "订单总金额不能为空")
    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    /**
     * 配送方式
     */
    @NotNull(message = "配送方式不能为空")
    @ApiModelProperty(value = "配送方式")
    private Integer deliveryType;

    /**
     * 订单状态
     */
    @ApiModelProperty(value = "订单状态")
    private Integer status;
}