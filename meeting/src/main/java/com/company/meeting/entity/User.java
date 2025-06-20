package com.company.meeting.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String role; // USER, ADMIN
    private Integer status; // 1:启用 0:禁用
    private Date createTime;
    private Date updateTime;
}