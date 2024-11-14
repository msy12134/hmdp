package com.hmdp.utils;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO)session.getAttribute("user");
        if (user == null) {
            //用户不存在，重定向到登录页面
            response.setStatus(401);
            return false;
        }
        //如果用户存在，就把用户信息保存到UserHolder中
        UserHolder.saveUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //请求结束后，清除UserHolder中的用户信息
        if (UserHolder.getUser() != null) {
            UserHolder.removeUser();
        }
    }
}
