package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 操作日志记录参数对象
 *
 * @author user
 */
@Data
@ApiModel(description = "操作日志记录参数")
public class OperationLogDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", example = "1001")
    private Long userId;

    /**
     * 操作类型
     */
    @NotBlank(message = "操作类型不能为空")
    @ApiModelProperty(value = "操作类型", example = "登录")
    private String operationType;

    /**
     * 操作内容
     */
    @NotBlank(message = "操作内容不能为空")
    @ApiModelProperty(value = "操作内容", example = "用户登录系统")
    private String content;
}