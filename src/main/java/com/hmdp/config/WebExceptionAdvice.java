package com.hmdp.config;

import com.hmdp.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return Result.fail(e.getMessage());
    }
    @ExceptionHandler(BaseException.class)
    public Result handleRuntimeException(BaseException e) {
        log.error(e.getMessage());
        return Result.fail(e.getMessage());
    }
}
