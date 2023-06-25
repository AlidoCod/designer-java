package com.example.base.service;

import com.example.base.exception.GlobalRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonService {

    private final ObjectMapper objectMapper;

    public <T> String toJson(T value) {
        try {
            return value.getClass() == String.class ? (String) value : objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            String msg = "JSON 解析异常";
            log.warn("", ex);
            throw GlobalRuntimeException.of(msg);
        }
    }

    private <T> T parseJson(String value, Class<T> clazz) {
        try {
            return clazz == String.class ? (T) value : objectMapper.readValue(value, clazz);
        } catch (Exception ex) {
            String msg = "JSON 解析异常";
            log.warn("", ex);
            throw GlobalRuntimeException.of(msg);
        }
    }
}
