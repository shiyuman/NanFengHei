package cn.sym.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册传输对象
 * 
 * @author user
 */
@Data
@ApiModel(description = "用户注册信息")
public class UserRegisterDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true)
    private String password;

    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String phone;
}