package com.company.meeting.entity;

import lombok.Data;
import java.util.Date;

@Data
public class MeetingCheckin {
    private Long id;
    private Long bookingId;
    private Long userId;
    private String checkinType; // QR_CODE, NFC, MANUAL
    private Date checkinTime;
    private String checkinStatus; // PRESENT, ABSENT
    
    // 关联属性
    private String realName;
    private String meetingTitle;
}