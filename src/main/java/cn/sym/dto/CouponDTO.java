package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   优惠券传输对象
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "优惠券信息")
public class CouponDTO {

    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    @ApiModelProperty(value = "优惠券名称")
    private String name;

    /**
     * 类型：1-满减券，2-折扣券
     */
    @NotNull(message = "类型不能为空")
    @ApiModelProperty(value = "类型：1-满减券，2-折扣券")
    private Integer type;

    /**
     * 减免金额或折扣比例
     */
    @NotNull(message = "减免金额或折扣比例不能为空")
    @ApiModelProperty(value = "减免金额或折扣比例")
    private BigDecimal discountAmount;

    /**
     * 最低消费金额
     */
    @NotNull(message = "最低消费金额不能为空")
    @ApiModelProperty(value = "最低消费金额")
    private BigDecimal minAmount;

    /**
     * 生效开始时间
     */
    @NotNull(message = "生效开始时间不能为空")
    @ApiModelProperty(value = "生效开始时间")
    private Date startTime;

    /**
     * 生效结束时间
     */
    @NotNull(message = "生效结束时间不能为空")
    @ApiModelProperty(value = "生效结束时间")
    private Date endTime;
}