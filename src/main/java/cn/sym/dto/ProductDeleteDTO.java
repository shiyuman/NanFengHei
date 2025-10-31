package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除商品传输对象
 * @author user
 */
@Data
public class ProductDeleteDTO {

    /**
     * 商品唯一标识
     */
    @NotNull(message = "商品ID不能为空")
    private Long id;
}