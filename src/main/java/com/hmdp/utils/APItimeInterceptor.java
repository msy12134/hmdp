package com.hmdp.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class APItimeInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 获取请求开始时间
        long startTime = (Long) request.getAttribute("startTime");
        // 记录请求结束时间
        long endTime = System.currentTimeMillis();
        // 计算处理时间
        long executeTime = endTime - startTime;
        // 打印处理时间
        System.out.println("API [" + request.getRequestURI() + "] executed in " + executeTime + " ms");
    }
}