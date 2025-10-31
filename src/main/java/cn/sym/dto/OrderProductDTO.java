package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单商品信息传输对象
 * 
 * @author user
 */
@Data
public class OrderProductDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    private Integer quantity;
}