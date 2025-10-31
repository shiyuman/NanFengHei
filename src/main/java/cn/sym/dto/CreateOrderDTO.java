package cn.sym.dto;

import cn.sym.dto.OrderProductDTO;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建订单请求参数对象
 * 
 * @author user
 */
@Data
public class CreateOrderDTO {

    /**
     * 下单用户ID
     */
    @NotNull(message = "下单用户ID不能为空")
    private Long userId;

    /**
     * 配送方式
     */
    @NotNull(message = "配送方式不能为空")
    private Integer deliveryType;

    /**
     * 商品列表
     */
    @NotNull(message = "商品列表不能为空")
    private List<OrderProductDTO> productList;
}