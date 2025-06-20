package com.company.meeting.controller;

import com.company.meeting.common.Result;
import com.company.meeting.dto.LoginRequest;
import com.company.meeting.dto.LoginResponse;
import com.company.meeting.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return Result.success(response, "登录成功");
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null, "退出成功");
    }

    @GetMapping("/info")
    public Result<LoginResponse> getUserInfo() {
        try {
            LoginResponse response = authService.getCurrentUserInfo();
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }
}