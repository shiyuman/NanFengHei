package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *   优惠券查询条件
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "优惠券查询条件")
public class CouponQuery {

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    private Integer pageNo;

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    private Integer pageSize;
    
    /**
     * 优惠券名称
     */
    @ApiModelProperty(value = "优惠券名称")
    private String name;
    
    /**
     * 优惠券状态
     */
    @ApiModelProperty(value = "优惠券状态")
    private Integer status;
    
    /**
     * 优惠券类型
     */
    @ApiModelProperty(value = "优惠券类型")
    private Integer type;
}