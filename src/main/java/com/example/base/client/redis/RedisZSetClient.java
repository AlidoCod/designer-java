package com.example.base.client.redis;

import com.example.base.controller.bean.vo.base.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisZSetClient {

    final StringRedisTemplate stringRedisTemplate;

    /**
     * 初始化排名，初始化为0
     */
    public void put(String key, String value, int score) {
        stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    public void put(String key, String value) {
         put(key, value, 0);
    }

    /**
     *  批量插入
     */
    public void put(String key, List<Tuple> list) {
        Set<ZSetOperations.TypedTuple<String>> tuples = list.parallelStream().map(o ->
                ZSetOperations.TypedTuple.of(o.getKey(), Double.valueOf(o.getValue()))
        ).collect(Collectors.toSet());
        stringRedisTemplate.opsForZSet().add(key, tuples);
    }

    /**
     *  初始化插入
     */
    public void initPut(String key, List<String> list, int initScore) {
        Set<ZSetOperations.TypedTuple<String>> tuples = list.parallelStream().map(o ->
                ZSetOperations.TypedTuple.of(o, (double) initScore)
        ).collect(Collectors.toSet());
        stringRedisTemplate.opsForZSet().add(key, tuples);
    }

    /**
     *  若value不存在，则会创建score值的value
     */
    public void increase(String key, String value, int score) {
        stringRedisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    public void increase(String key, String value) {
        increase(key, value, 1);
    }

    public void decrease(String key, String value) {
        increase(key, value, -1);
    }

    /**
     * 查询对应的score
     */
    public Double score(String key, String value) {
        return stringRedisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 查询当前排名
     */
    public Long rank(String key, String value) {
        return stringRedisTemplate.opsForZSet().rank(key, value);
    }

    /**
     *  倒排，因为正常情况下，分越大，排名越小，倒排就正常了。
     */
    public List<Tuple> rangeWithScore(String key, int start, int end) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, end).stream()
                .map(o -> Tuple.builder().key(o.getValue()).value(String.valueOf(o.getScore())).build()).toList();
    }

    public Set<String> range(String key, int start, int end) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
    }

}
