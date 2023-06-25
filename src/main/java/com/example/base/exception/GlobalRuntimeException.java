package com.example.base.exception;

/**
 * 可以预见的异常
 */
public class GlobalRuntimeException extends RuntimeException{

    private GlobalRuntimeException(String message) {
        super(message);
    }

    public static GlobalRuntimeException of(String message) {
        return new GlobalRuntimeException(message);
    }

    public static GlobalRuntimeException fail() {
        return GlobalRuntimeException.of("预期之内的错误");
    }
}
