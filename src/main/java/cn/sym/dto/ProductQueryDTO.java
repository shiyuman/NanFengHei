package cn.sym.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询商品详情传输对象
 * @author user
 */
@Data
public class ProductQueryDTO {

    /**
     * 商品唯一标识
     */
    @NotNull(message = "商品ID不能为空")
    private Long id;
}