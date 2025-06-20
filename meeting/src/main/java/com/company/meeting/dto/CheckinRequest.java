package com.company.meeting.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class CheckinRequest {
    @NotNull(message = "预订ID不能为空")
    private Long bookingId;
    
    private String checkinType = "QR_CODE"; // QR_CODE, NFC, MANUAL
    
    private String userCode; // 用于手动输入工号签到
}