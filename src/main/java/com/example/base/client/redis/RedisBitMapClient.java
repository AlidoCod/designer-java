package com.example.base.client.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisBitMapClient {

    final StringRedisTemplate redisTemplate;

    public void set(String key, Long offset) {
        redisTemplate.opsForValue().setBit(key, offset, true);
    }

    public Boolean get(String key, Long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }
}
