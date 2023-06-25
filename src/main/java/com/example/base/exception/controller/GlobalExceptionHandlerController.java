package com.example.base.exception.controller;

import com.example.base.bean.vo.result.Result;
import com.example.base.exception.GlobalRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Result<Void> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String msg = "Content-Type类型错误";
        log.warn(msg, ex);
        return Result.fail(msg);
    }

    /**
     * 使用warn型警告，因为这是预期之中的异常
     */
    @ExceptionHandler(value = GlobalRuntimeException.class)
    public Result<Void> globalRuntimeException(GlobalRuntimeException ex) {
        return Result.fail(ex.getMessage());
    }

    /**
     * 使用error型警告，这是意料之外的异常
     */
    @ExceptionHandler(value = Exception.class)
    public Result<Void> exception(Exception ex) {
        log.error(ex.getMessage(), ex);
        return Result.error();
    }
}
