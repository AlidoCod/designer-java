package com.example.base.exception;

import java.util.Optional;

public class GlobalException extends RuntimeException{

    /*输出代码*/
    int code;

    private GlobalException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static GlobalException of(int code, String message) {
        return new GlobalException(code, message);
    }

    public static GlobalException fail(String message) {
        return GlobalException.of(404, "服务器内部异常: " + message);
    }
}
