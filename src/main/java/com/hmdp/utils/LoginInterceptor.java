package com.hmdp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的token
        String token = request.getHeader("authorization");
        if (token == null) {
            return true;
        }
        // 根据token从redis中获取用户信息
        // 从 Redis 中获取哈希值
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries("login:token:" + token);
        if (userMap.isEmpty()) {
            return true;
        }
        // 将哈希值转换为 UserDTO 对象
        ObjectMapper objectMapper = new ObjectMapper();
        UserDTO userDTO = objectMapper.convertValue(userMap, UserDTO.class);
        // 如果用户存在，就把用户信息保存到UserHolder中
        UserHolder.saveUser(userDTO);

        // 刷新token的过期时间
        stringRedisTemplate.expire("login:token:" + token, 30, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后，清除UserHolder中的用户信息
        UserHolder.removeUser();
    }
}