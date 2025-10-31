package cn.sym.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品上下架传输对象
 * @author user
 */
@Data
public class ProductStatusUpdateDTO {

    /**
     * 商品唯一标识
     */
    @NotNull(message = "商品ID不能为空")
    private Long id;

    /**
     * 上下架状态：1-上架，0-下架
     */
    @NotNull(message = "上下架状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;
}