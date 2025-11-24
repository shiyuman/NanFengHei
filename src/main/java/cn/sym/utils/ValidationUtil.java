package cn.sym.utils;

import cn.sym.common.response.RestResult;

import java.util.stream.Collectors;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * 参数校验工具类
 */
public class ValidationUtil {

    /**
     * 处理参数校验错误
     * @param bindingResult 校验结果
     * @return 包含错误信息的响应结果
     */
    public static RestResult<String> handleValidationErrors(BindingResult bindingResult) {
        String errorMessage = bindingResult.getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return new RestResult<>("000001", "参数校验失败: " + errorMessage);
    }
}
