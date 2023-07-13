package com.example.base.utils;

import com.example.base.exception.GlobalRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonUtil {

    public static ObjectMapper objectMapper;

    public JsonUtil(ObjectMapper mapper) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        //避免精度丢失
        mapper.registerModule(simpleModule);
        objectMapper = mapper;
        log.info("objectMapper精度初始化成功");
    }

    public static <T> String toJson(T value) {
        try {
            return value.getClass() == String.class ? (String) value : JsonUtil.objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            String msg = "JSON 解析异常";
            log.warn("", ex);
            throw GlobalRuntimeException.of(msg);
        }
    }

    public  static <T> T toPojo(String value, Class<T> clazz) {
        try {
            return clazz == String.class ? (T) value : JsonUtil.objectMapper.readValue(value, clazz);
        } catch (Exception ex) {
            String msg = "JSON 解析异常";
            log.warn("", ex);
            throw GlobalRuntimeException.of(msg);
        }
    }
}
