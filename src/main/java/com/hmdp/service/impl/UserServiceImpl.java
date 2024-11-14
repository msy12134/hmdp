package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.config.BaseException;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.CodeGenerator;
import com.hmdp.utils.PhoneValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //验证手机号是否有效
        if (!PhoneValidator.isValidPhone(phone)) {
            throw new BaseException("手机号格式不正确");
        }
        String code = CodeGenerator.generateCode(4);
        log.info("手机号：{}，验证码：{}", phone, code);
        stringRedisTemplate.opsForValue().set("phone:" + phone, code, 2, TimeUnit.MINUTES);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        if (!PhoneValidator.isValidPhone(loginForm.getPhone())) {
            throw new BaseException("手机号格式不正确");
        }
        String code = stringRedisTemplate.opsForValue().get("phone:" + loginForm.getPhone());
        if (code == null || !code.equals(loginForm.getCode())) {
            throw new BaseException("验证码错误");
        }
        User user = getOne(new QueryWrapper<User>().eq("phone", loginForm.getPhone()));
        if (user == null) {
            //用户不存在就直接注册用户
            user = new User();
            user.setPhone(loginForm.getPhone());
            user.setNickName("用户" + loginForm.getPhone());
            save(user);
        }
        //生成UUID作为token，然后把UUID和用户信息存入redis，用hash结构存储
        String token = java.util.UUID.randomUUID().toString();
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        Map<String, String> stringUserMap = userMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toString()
                ));
        stringRedisTemplate.opsForHash().putAll("login:token:" + token, stringUserMap);
        stringRedisTemplate.expire("login:token:" + token, 30, TimeUnit.MINUTES);
        //把redis中存储的验证码相关的键值对也删除
        stringRedisTemplate.delete("phone:" + loginForm.getPhone());
        return Result.ok(token);
    }
}