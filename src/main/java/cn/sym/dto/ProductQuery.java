package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   商品查询条件传输对象
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "商品查询条件传输对象")
public class ProductQuery {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID")
    private Long productId;
}