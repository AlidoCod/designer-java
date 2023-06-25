package com.example.base.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisHashClient {

    final StringRedisTemplate stringRedisTemplate;
    final ObjectMapper objectMapper;

    public <T> void putAll(String key, T obj) {
        try {
            Map<String, String> map = new HashMap<>();
            for (Field field : obj.getClass().getDeclaredFields()) {
                //禁用安全检查
                field.setAccessible(true);
                map.put(field.getName(), toJson(field.get(obj)));
                //开启安全检查
                field.setAccessible(false);
            }
            stringRedisTemplate.opsForHash().putAll(key, map);
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    /**
     * 避免String对象转化Json时发生异常
     */
    private <T> String toJson(T value) throws JsonProcessingException {
        return value.getClass() == String.class ? (String) value : objectMapper.writeValueAsString(value);
    }

    private <T> T parseJson(String value, Class<T> clazz) throws JsonProcessingException {
        return clazz == String.class ? (T) value : objectMapper.readValue(value, clazz);
    }
}
