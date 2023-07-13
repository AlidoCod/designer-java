package com.example.base.exception;

public class TooManyRequestsException extends RuntimeException {

    private TooManyRequestsException() {
        super("系统繁忙,请稍后再试");
    }

    public static TooManyRequestsException of() {
        return new TooManyRequestsException();
    }
}
