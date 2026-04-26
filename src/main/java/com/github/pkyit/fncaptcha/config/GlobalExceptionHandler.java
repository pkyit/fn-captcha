package com.github.pkyit.fncaptcha.config;

import com.github.pkyit.fncaptcha.domain.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>统一处理参数校验异常和系统运行时异常，保证返回一致的 {@link Result} 响应格式。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验失败异常（@Valid 校验不通过时触发）
     *
     * @param ex 参数校验异常
     * @return 包含所有校验失败信息的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.fail(message);
    }

    /**
     * 兜底异常处理，防止未捕获的异常暴露给客户端
     *
     * @param ex 异常
     * @return 通用错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.fail("系统异常，请稍后重试");
    }
}
