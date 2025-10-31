package cn.sym.common.exception;

import lombok.Getter;
import cn.sym.common.constant.ResultCodeConstant;

/**
 * 自定义业务异常类
 *
 * @author user
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    private final String msg;

    public BusinessException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(String code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
