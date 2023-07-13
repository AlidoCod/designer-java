package com.example.base;

import com.example.base.client.redis.RedisSetClient;
import com.example.base.client.redis.RedisZSetClient;
import com.example.base.constant.RedisSetConstant;
import com.example.base.controller.bean.vo.base.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class RedisTest {

    @Autowired
    RedisZSetClient zSetClient;

    @Autowired
    RedisSetClient redisSetClient;

    @Test
    public void rangeTest() {
        List<Tuple> list = zSetClient.rangeWithScore(RedisSetConstant.USER_FAVOUR + "1675677815413624834", 0, 1);
        log.debug(list.toString());
    }

    @Test
    public void setTest() {
        System.out.println(redisSetClient.removeMember(RedisSetConstant.COLLECT_WORKS + 1675713231919423489L, 1674389720672927746L));
    }
}
