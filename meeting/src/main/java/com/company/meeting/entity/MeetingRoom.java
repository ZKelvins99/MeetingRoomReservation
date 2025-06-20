package com.company.meeting.entity;

import lombok.Data;
import java.util.Date;

@Data
public class MeetingRoom {
    private Long id;
    private String roomName;
    private String roomLocation;
    private Integer capacity;
    private String equipment; // JSON格式设备信息
    private Integer status; // 1:可用 0:维护中
    private Date createTime;
    private Date updateTime;
}