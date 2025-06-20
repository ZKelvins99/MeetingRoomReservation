package com.company.meeting.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BookingRequest {
    @NotNull(message = "会议室ID不能为空")
    private Long roomId;
    
    @NotBlank(message = "会议主题不能为空")
    private String meetingTitle;
    
    private String meetingDesc;
    
    @NotNull(message = "开始时间不能为空")
    private Date startTime;
    
    @NotNull(message = "结束时间不能为空")
    private Date endTime;
    
    @NotNull(message = "参会人数不能为空")
    private Integer attendeeCount;
    
    private String attendees;
    
    private Integer remindTime = 15; // 默认提前15分钟提醒
}