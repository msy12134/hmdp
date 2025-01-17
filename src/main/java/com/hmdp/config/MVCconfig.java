package com.hmdp.config;

import com.hmdp.utils.APItimeInterceptor;
import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MVCconfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //token刷新拦截器
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .order(1)
                .addPathPatterns("/**");
        //登录校验拦截器
        registry.addInterceptor(new RefreshInterceptor())
                .order(2)
                .excludePathPatterns("/shop/**",
                        "/voucher/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                );
        registry.addInterceptor(new APItimeInterceptor())
                .order(0)
                .addPathPatterns("/**");
    }
}