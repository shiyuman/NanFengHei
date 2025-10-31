package cn.sym.config;

import cn.sym.common.exception.BusinessException;
import cn.sym.common.response.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentNotValidException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * 处理业务异常
     * @param exception
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public RestResult<Object> handleBusinessException(BusinessException exception) {
        log.error(exception.getMessage(), exception);
        return new RestResult<>(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>("参数验证失败", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleException(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
        return new ResponseEntity<>("服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("请求方法不支持: ", exception);
        return new ResponseEntity<>("请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED);
    }
}