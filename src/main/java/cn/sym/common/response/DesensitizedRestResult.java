package cn.sym.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 脱敏响应结果封装类
 *
 * @param <T> 数据类型
 */
@ApiModel(description = "脱敏响应结果")
@Data
@EqualsAndHashCode(callSuper = true)
public class DesensitizedRestResult<T> extends RestResult<T> {

    /**
     * 是否脱敏
     */
    @ApiModelProperty(value = "是否脱敏")
    private Boolean desensitized = false;

    public DesensitizedRestResult(String code, String msg, T data) {
        super(code, msg, data);
    }

    public DesensitizedRestResult(String code, String msg) {
        super(code, msg);
    }

    public DesensitizedRestResult() {
        super();
    }

    /**
     * 创建脱敏响应结果
     * @param code 业务码
     * @param msg 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return 脱敏响应结果
     */
    public static <T> DesensitizedRestResult<T> desensitized(String code, String msg, T data) {
        DesensitizedRestResult<T> result = new DesensitizedRestResult<>(code, msg, data);
        result.setDesensitized(true);
        return result;
    }
}

