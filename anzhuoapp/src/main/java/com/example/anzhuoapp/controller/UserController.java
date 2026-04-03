package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.User;
import com.example.anzhuoapp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        User user = userMapper.login(username, password);
        if (user != null) {
            return "success";
        } else {
            return "error";
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        User existUser = userMapper.findByUsername(username);
        if (existUser != null) {
            return "exist";
        }
        userMapper.register(username, password);
        return "success";
    }
}