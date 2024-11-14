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
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

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

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //验证手机号是否有效
        if(!PhoneValidator.isValidPhone(phone)){
            throw new BaseException( "手机号格式不正确");
        }
        String code= CodeGenerator.generateCode(4);
        session.setAttribute("code",code);
        log.info("手机号：{}，验证码：{}",phone,code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        if(!PhoneValidator.isValidPhone(loginForm.getPhone())){
            throw new BaseException( "手机号格式不正确");
        }
        String code= (String) session.getAttribute("code");
        if(code==null || !code.equals(loginForm.getCode())){
            throw new BaseException("验证码错误");
        }
        User user = getOne(new QueryWrapper<User>().eq("phone", loginForm.getPhone()));
        if(user==null){
            //用户不存在就直接注册用户
            user=new User();
            user.setPhone(loginForm.getPhone());
            user.setNickName("用户"+loginForm.getPhone());
            save(user);
        }
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }
}
