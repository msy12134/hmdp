package com.hmdp.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class RedisUniqueIdCreaterTest {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void test1() {
        RedisUniqueIdCreater redisUniqueIdCreater = new RedisUniqueIdCreater(stringRedisTemplate);
        for (int i = 0; i < 100000; i++) {
            long result = redisUniqueIdCreater.nextId("test");
            System.out.println(result);
        }
}
}