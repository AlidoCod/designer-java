package com.example.base.client.redis;

import com.example.base.client.bean.Consumer;
import com.example.base.service.plain.JsonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisSetClient {

    private final StringRedisTemplate stringRedisTemplate;
    final JsonService jsonService;

    public <T> List<T> getMembers(String key, Class<T> clazz) {
        return Objects.requireNonNull(stringRedisTemplate.opsForSet().members(key))
                .stream().map(o -> jsonService.toPojo(o, clazz)).toList();
    }
    // key userId username userId username

    public <T> Boolean isMember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, jsonService.toJson(value));
    }

    @SuppressWarnings("ConstantConditions")
    public <T> void addMember(String key, T value) {
        stringRedisTemplate.opsForSet().add(key, jsonService.toJson(value));
    }

    @SuppressWarnings("ConstantConditions")
    public <T> Boolean removeMember(String key, T value) {
        return stringRedisTemplate.opsForSet().remove(key, jsonService.toJson(value)) == 1;
    }

    public Set<String> getCollectiveMembers(String key1, String key2) {
        return stringRedisTemplate.opsForSet().intersect(key1, key2);
    }

    /**
     * 获取差集，获取key1存在但key2不存在的元素
     * 所以如果想获取感兴趣的人，第一个key就得是别人，第二个key是自己
     * 别人关注了，但自己没关注；别人存在的元素，但自己不存在
     */
    public Set<String> getDifferentMembers(String key1, String key2) {
        return stringRedisTemplate.opsForSet().difference(key1, key2);
    }

    public Long getSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    public Set<String> getKeys(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

    public void clear(String key, Consumer consumer) {
        while (getSize(key) != 0) {
            String value = stringRedisTemplate.opsForSet().pop(key);
            consumer.consume(value);
        }
    }
}