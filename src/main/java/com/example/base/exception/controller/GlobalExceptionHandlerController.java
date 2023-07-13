package com.example.base.exception.controller;

import com.example.base.controller.bean.vo.base.Result;
import com.example.base.exception.GlobalRuntimeException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {

    /**
     * 捕获不满足参数校验的参数异常
     * @param e 当校验方法参数时, 校验失败会报此错误
     */
    @ExceptionHandler(BindException.class)
    public Result BindException(BindException e){

        List<FieldError> ls = e.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        for (FieldError fe : ls){
            sb.append("property:").append(fe.getField())
                    .append(", exception-reason:").append(fe.getDefaultMessage()).append(".");
        }
        log.info(sb.toString());
        return Result.data(sb.toString());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result MissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        log.warn(exception.getMessage());
        return Result.data(exception.getMessage());
    }

    /**
     * 捕获不满足参数校验的参数异常
     * @param e 当校验类参数时，校验失败汇报此错误
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result ConstraintViolationException(ConstraintViolationException e){

        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        List<String> collect = constraintViolations.stream().map(v -> "property:" + v.getPropertyPath()
                + ", property-value:" + v.getInvalidValue()
                + ", reason:" + v.getMessage()
        ).collect(Collectors.toList());
        log.info(collect.toString());
        return Result.data(collect);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result MethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        String msg = errors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
        log.info("MethodArgumentNotValidException: {}", msg);
        return Result.fail(msg);
    }


    /**
     *  知道原因即可，不需要捕获
     */
    @ExceptionHandler(value = InvalidFormatException.class)
    public Result InvalidFormatException(Exception ex) {
        String msg = "JSON反序列化格式错误";
        log.warn(msg);
        return Result.fail(msg);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result HttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String msg = "JSON转换Java类型转换异常: " + ex.getMessage();
        log.warn(msg);
        return Result.fail(msg);
    }

    @ExceptionHandler(value = DateTimeParseException.class)
    public Result DateTimeParseException(DateTimeParseException ex) {
        String msg = "LocalDateTime转换错误";
        log.warn(msg);
        return Result.fail(msg);
    }

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public Result<Void> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String msg = "Content-Type类型错误";
        log.warn(msg);
        return Result.fail(msg);
    }

    @ExceptionHandler(value = MultipartException.class)
    public Result MultipartException(MultipartException ex) {
        return Result.fail(400, "文件格式错误", ex);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Result<Void> HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return Result.fail(ex.getMessage());
    }
    /**
     * 使用warn型警告，因为这是预期之中的异常
     */
    @ExceptionHandler(value = GlobalRuntimeException.class)
    public Result<Void> globalRuntimeException(GlobalRuntimeException ex) {
        log.debug("", ex);
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
