package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单传输对象
 * 
 * @author user
 */
@Data
@ApiModel(description = "创建订单传输对象")
public class CreateOrderDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    /**
     * 配送方式：1-自取，2-快递
     */
    @NotNull(message = "配送方式不能为空")
    @ApiModelProperty(value = "配送方式：1-自取，2-快递")
    private Integer deliveryType;

    /**
     * 商品列表
     */
    @NotNull(message = "商品列表不能为空")
    @ApiModelProperty(value = "商品列表")
    private List<OrderProductDTO> productList;
    
    /**
     * 唯一请求ID，用于幂等性保障
     */
    @NotBlank(message = "请求ID不能为空")
    @ApiModelProperty(value = "唯一请求ID，用于幂等性保障")
    private String requestId;
}