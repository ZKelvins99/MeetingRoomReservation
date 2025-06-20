package com.company.meeting.dto;

import com.company.meeting.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User user;
    private Long expiresIn;
}