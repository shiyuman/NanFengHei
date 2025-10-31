package cn.sym.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import cn.sym.common.constant.ResultCodeConstant;
import lombok.*;
import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @author user
 */
@ApiModel(description = "统一响应结果")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RestResult<T> {

    /**
     * 业务返回码
     */
    private String code;

    /**
     * 业务提示信息
     */
    private String msg;

    /**
     * 业务数据
     */
    private T data;

    public RestResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
