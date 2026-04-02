package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.User;
import com.example.anzhuoapp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        User user = userMapper.login(username, password);
        if (user != null) {
            return "登录成功，欢迎 " + user.getRole();
        } else {
            return "用户名或密码错误";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        // 先检查是否被注册过
        User existUser = userMapper.findByUsername(username);
        if (existUser != null) {
            return "用户名已存在";
        }
        // 执行注册
        userMapper.register(username, password);
        return "注册成功";
    }
}