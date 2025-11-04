package cn.sym.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 订单实体类
 *
 * @author user
 */
@TableName("order_info")
@Data
@ApiModel(description = "订单信息实体类")
public class OrderDO {

    /**
     * 订单唯一标识
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "订单唯一标识")
    private Long id;

    /**
     * 下单用户ID
     */
    @ApiModelProperty(value = "下单用户ID")
    private Long userId;

    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    /**
     * 订单总金额
     */
    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalAmount;

    /**
     * 配送方式：1-自取，2-快递
     */
    @ApiModelProperty(value = "配送方式：1-自取，2-快递")
    private Integer deliveryType;

    /**
     * 订单状态：1-待支付，2-已支付，3-已完成，4-已取消
     */
    @ApiModelProperty(value = "订单状态：1-待支付，2-已支付，3-已完成，4-已取消")
    private Integer status;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改人")
    private String updateBy;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
}
