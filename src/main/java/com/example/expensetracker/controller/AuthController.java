package com.example.expensetracker.controller;

import com.example.expensetracker.model.User;
import com.example.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController   // ✅ VERY IMPORTANT
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        userService.register(user);
        return "Signup successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return userService.login(user.getEmail(), user.getPassword())
                ? "Login successful"
                : "Invalid credentials";
    }

    @PostMapping("/forgot")
    public String forgot(@RequestBody User user) {
        return userService.resetPassword(user.getEmail(), user.getPassword())
                ? "Password reset successful"
                : "User not found";
    }
}

