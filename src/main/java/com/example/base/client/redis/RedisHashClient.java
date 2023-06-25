package com.example.base.client.redis;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.example.base.service.JsonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
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
    final JsonService jsonService;

    public <T> void putAll(String key, T obj) {
        try {
            Map<String, String> map = new HashMap<>();
            for (Field field : obj.getClass().getDeclaredFields()) {
                //禁用安全检查
                field.setAccessible(true);
                map.put(field.getName(), jsonService.toJson(field.get(obj)));
                //开启安全检查
                field.setAccessible(false);
            }
            stringRedisTemplate.opsForHash().putAll(key, map);
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    public <T> void put(String firstKey, String secondKey, T obj) {
        stringRedisTemplate.opsForHash().put(firstKey, secondKey, jsonService.toJson(obj));
    }

    public Map<String, String> getAll(String key) {
        return stringRedisTemplate.execute((RedisCallback<Map<String, String>>) con -> {
            Map<byte[], byte[]> result = con.hGetAll(key.getBytes());
            if (CollectionUtils.isEmpty(result)) {
                return new HashMap<>(0);
            }

            Map<String, String> ans = new HashMap<>(result.size());
            for (Map.Entry<byte[], byte[]> entry : result.entrySet()) {
                ans.put(new String(entry.getKey()), new String(entry.getValue()));
            }
            return ans;
        });
    }


}
