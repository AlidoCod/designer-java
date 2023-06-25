package com.example.base.client;

import com.example.base.exception.GlobalRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author 雷佳宝
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RedisStringClient {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private static final String LOCK_KEY_PREFIX = "LOCK:";

    /**
     * 设置永久Key-Value
     */
    private <T> void set(String key, T value) {
        stringRedisTemplate.opsForValue().set(key, toJson(value));
    }

    /**
     * 设置会过期的key-value
     */
    public <T> void set(String key, T value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, toJson(value), time, unit);
    }

    /**
     * 设置仅能设置一次的缓存
     */
    public <T> boolean setIfAbsent(String key, T value, Long time, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, toJson(value), time, unit));
    }

    public Long increase(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    public Long decrease(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 避免String对象转化Json时发生异常
     */
    private <T> String toJson(T value) {
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

    /**
     * 普通的get方法，不会自动重建缓存。
     */
    public <T> T get(String key, Class<T> clazz){
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return parseJson(value, clazz);
    }

    public <T> T get(String prefix, String suffix, Class<T> clazz) {
        return get(prefix + suffix, clazz);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    public boolean tryLock(String suffix) {
        return setIfAbsent(LOCK_KEY_PREFIX + suffix, "1", 10L, TimeUnit.SECONDS);
    }

    public void unlock(String suffix) {
        delete(LOCK_KEY_PREFIX + suffix);
    }

}
