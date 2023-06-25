package com.example.base.client.bean;

@FunctionalInterface
public interface Consumer {

    void consume(Object... objects);
}