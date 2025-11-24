package cn.sym.config;

import cn.sym.common.exception.BusinessException;
import cn.sym.common.response.RestResult;
import cn.sym.utils.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * 处理业务异常
     * @param exception 业务异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public RestResult<Object> handleBusinessException(BusinessException exception) {
        log.error("业务异常: code={}, message={}", exception.getCode(), exception.getMsg(), exception);
        return new RestResult<>(exception.getCode(), exception.getMsg());
    }

    /**
     * 处理请求体参数校验异常（@Validated配合@RequestParam或@RequestBody使用）
     * @param exception 参数校验异常
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.warn("请求参数校验失败: {}", exception.getMessage());
        BindingResult bindingResult = exception.getBindingResult();
        return ValidationUtil.handleValidationErrors(bindingResult);
    }

    /**
     * 处理请求参数校验异常（@Validated配合普通参数使用）
     * @param exception 参数校验异常
     * @return 统一响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public RestResult<String> handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn("请求参数校验失败: {}", exception.getMessage());
        String errorMessage = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        return new RestResult<>("000001", "参数校验失败: " + errorMessage);
    }

    /**
     * 处理绑定异常
     * @param exception 绑定异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BindException.class)
    public RestResult<String> handleBindException(BindException exception) {
        log.warn("数据绑定失败: {}", exception.getMessage());
        return ValidationUtil.handleValidationErrors(exception.getBindingResult());
    }

    /**
     * 处理HTTP请求方法不支持异常
     * @param exception 请求方法不支持异常
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RestResult<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("请求方法不支持: ", exception);
        return new RestResult<>("000001", "请求方法不支持: " + exception.getMethod());
    }

    /**
     * 处理通用异常
     * @param throwable 异常
     * @return 统一响应结果
     */
    @ExceptionHandler(Throwable.class)
    public RestResult<String> handleException(Throwable throwable) {
        log.error("系统异常: ", throwable);
        return new RestResult<>("999999", "系统异常: " + throwable.getMessage());
    }
}