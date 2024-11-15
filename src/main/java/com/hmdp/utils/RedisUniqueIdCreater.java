package com.hmdp.utils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisUniqueIdCreater {
    private static final long BEGIN = 1000000000L;
    private StringRedisTemplate stringRedisTemplate;
    public RedisUniqueIdCreater(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public long nextId(String keyprefix) {
        LocalDateTime now = LocalDateTime.now();
        long epochSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp=epochSecond-BEGIN;
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long increment = stringRedisTemplate.opsForValue().increment("icr:" + keyprefix + ":" + date);
        return (0L << 63) | (timestamp << 32) | (increment & 0xFFFFFFFFL);
    }
}
