package com.company.meeting.service;

import com.company.meeting.dto.LoginRequest;
import com.company.meeting.dto.LoginResponse;
import com.company.meeting.entity.User;
import com.company.meeting.mapper.UserMapper;
import com.company.meeting.util.JwtUtil;
import com.company.meeting.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            System.out.println("用户不存在");
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateToken(user.getUsername());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        response.setExpiresIn(86400000L); // 24小时
        
        return response;
    }

    public LoginResponse getCurrentUserInfo() {
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("用户未登录");
        }
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        LoginResponse response = new LoginResponse();
        response.setUser(user);
        
        return response;
    }
}