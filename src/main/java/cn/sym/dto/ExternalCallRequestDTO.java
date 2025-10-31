package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   外部接口调用请求参数封装类
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "外部接口调用请求参数")
public class ExternalCallRequestDTO {

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true)
    private Long orderId;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @ApiModelProperty(value = "支付金额", required = true)
    private Double amount;

    /**
     * 异步通知地址
     */
    @NotBlank(message = "异步通知地址不能为空")
    @ApiModelProperty(value = "异步通知地址", required = true)
    private String notifyUrl;

    /**
     * 运单号
     */
    @NotBlank(message = "运单号不能为空")
    @ApiModelProperty(value = "运单号", required = true)
    private String waybillNo;

    /**
     * 物流公司编码
     */
    @NotBlank(message = "物流公司编码不能为空")
    @ApiModelProperty(value = "物流公司编码", required = true)
    private String logisticsCode;
}