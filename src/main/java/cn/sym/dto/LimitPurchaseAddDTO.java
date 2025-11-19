package cn.sym.dto;


import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增限购配置参数对象
 *
 * @author user
 */
@Data
public class LimitPurchaseAddDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 最大购买数量
     */
    @NotNull(message = "最大购买数量不能为空")
    private Integer maxQuantity;
}