package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增早鸟票预售配置参数对象
 *
 * @author user
 */
@Data
@ApiModel("新增早鸟票预售配置参数对象")
public class PreSaleTicketAddDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true)
    private Long productId;

    /**
     * 预售开始时间
     */
    @NotNull(message = "预售开始时间不能为空")
    @ApiModelProperty(value = "预售开始时间", required = true)
    private Date saleStartTime;

    /**
     * 预售结束时间
     */
    @NotNull(message = "预售结束时间不能为空")
    @ApiModelProperty(value = "预售结束时间", required = true)
    private Date saleEndTime;

    /**
     * 预售价格
     */
    @NotNull(message = "预售价格不能为空")
    @ApiModelProperty(value = "预售价格", required = true)
    private BigDecimal prePrice;
}