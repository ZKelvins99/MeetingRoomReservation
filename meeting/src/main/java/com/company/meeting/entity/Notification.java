package com.company.meeting.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Notification {
    private Long id;
    private Long userId;
    private Long bookingId;
    private String title;
    private String content;
    private String type; // SYSTEM, MEETING
    private Integer isRead; // 0:未读 1:已读
    private Date createTime;
    
    // 关联属性
    private String userName;
    private String realName;
}