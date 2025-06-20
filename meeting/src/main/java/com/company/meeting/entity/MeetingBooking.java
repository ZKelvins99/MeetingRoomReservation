package com.company.meeting.entity;

import lombok.Data;
import java.util.Date;

@Data
public class MeetingBooking {
    private Long id;
    private Long roomId;
    private Long userId;
    private String meetingTitle;
    private String meetingDesc;
    private Date startTime;
    private Date endTime;
    private Integer attendeeCount;
    private String attendees; // JSON格式参会人员
    private String bookingStatus; // BOOKED, CANCELLED, COMPLETED
    private Integer remindTime; // 提前提醒时间（分钟）
    private Integer isReminded; // 是否已提醒
    private Date createTime;
    private Date updateTime;
    
    // 关联属性
    private String roomName;
    private String userName;
    private String realName;
}