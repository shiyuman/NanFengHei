package cn.sym.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 库存扣减消息实体
 */
@Data
public class StockDeductionMessage {
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品列表
     */
    private List<OrderProductDTO> productList;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
}
