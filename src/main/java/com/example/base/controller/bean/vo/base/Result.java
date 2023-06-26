package com.example.base.controller.bean.vo.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Schema(description = "返回体")
@Data
@Getter
//忽略空值
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private int code;

    /**
     * 状态信息
     */
    @Schema(description = "状态信息")
    private String msg;


    /**
     *
     */
    @Schema(description = "处理时间")
    private Date time;


    @Schema(description = "数据信息")
    private T data;

    private Result() {
        this.time = new Date();
    }

    private Result(IResultCode resultCode) {
        this(resultCode, null, resultCode.getMsg());
    }

    private Result(IResultCode resultCode, String msg) {
        this(resultCode, null, msg);
    }

    private Result(IResultCode resultCode, T data) {
        this(resultCode, data, resultCode.getMsg());
    }

    private Result(IResultCode resultCode, T data, String msg) {
        this(resultCode.getCode(), data, msg);
    }

    private Result(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.time = new Date();
    }

    /**
     * 返回状态码
     *
     * @param resultCode 状态码
     * @param <T>        泛型标识
     * @return ApiResult
     */
    public static <T> Result<T> ok(IResultCode resultCode) {
        return new Result<>(resultCode);
    }

    public static <T> Result<T> ok(String msg) {
        return new Result<>(ResultCode.SUCCESS, msg);
    }

    public static <T> Result<T> ok(IResultCode resultCode, String msg) {
        return new Result<>(resultCode, msg);
    }

    public static <T> Result<T> ok() {
        return new Result<>(200, null, null);
    }

    public static  <T> Result<T> data(T data) {
        return new Result<>(200, data, null);
    }

    public static <T> Result<T> data(T data, String msg) {
        return new Result<>(200, data, msg);
    }

    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.FAILURE, ResultCode.FAILURE.getMsg());
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.FAILURE, msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, null, msg);
    }

    public static <T> Result<T> fail(IResultCode resultCode) {
        return new Result<>(resultCode);
    }

    public static <T> Result<T> fail(IResultCode resultCode, String msg) {
        return new Result<>(resultCode, msg);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, null, "服务器内部异常，请联系管理员");
    }

    public static <T> Result<T> condition(boolean flag) {
        return flag ? ok("处理成功") : fail("处理失败");
    }
}