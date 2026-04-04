package com.example.anzhuoapp.controller;

import com.example.anzhuoapp.entity.User;
import com.example.anzhuoapp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> params) {
        User user = userMapper.login(params.get("username"), params.get("password"));
        return user != null ? "success:" + user.getRole() : "error";
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> params) {
        if (userMapper.findByUsername(params.get("username")) != null) return "exist";
        userMapper.register(params.get("username"), params.get("password"));
        return "success";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody Map<String, String> params) {
        try {
            User user = userMapper.login(params.get("username"), params.get("oldPassword"));
            if (user == null) return "wrong_password";
            userMapper.updatePassword(params.get("username"), params.get("newPassword"));
            return "success";
        } catch (Exception e) { return "error"; }
    }

    @GetMapping("/allList")
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }
}