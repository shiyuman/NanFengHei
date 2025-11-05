package cn.sym.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 订单详情实体类
 *
 * @author user
 */
@TableName("order_item")
@Data
@ApiModel(description = "订单详情实体类")
public class OrderItemDO {

    /**
     * 订单详情唯一标识
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "订单详情唯一标识")
    private Long id;

    /**
     * 订单ID
     */
    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID")
    private Long productId;

    /**
     * 商品名称（快照）
     */
    @ApiModelProperty(value = "商品名称（快照）")
    private String productName;

    /**
     * 商品单价（快照）
     */
    @ApiModelProperty(value = "商品单价（快照）")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @ApiModelProperty(value = "购买数量")
    private Integer quantity;

    /**
     * 小计金额
     */
    @ApiModelProperty(value = "小计金额")
    private BigDecimal subtotalAmount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
}

