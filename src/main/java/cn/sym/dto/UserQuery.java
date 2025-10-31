package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *   用户查询条件传输对象
 * </p>
 * @author user
 */
@Data
@ApiModel(description = "用户查询条件传输对象")
public class UserQuery {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID")
    private Long userId;
}