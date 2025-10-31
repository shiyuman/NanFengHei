package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import lombok.Data;

/**
 * 订单信息导出查询参数
 *
 * @author user
 */
@Data
@ApiModel("订单信息导出查询参数")
public class OrderExportQueryDTO {

    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private Long userId;

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
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private Date endTime;
}