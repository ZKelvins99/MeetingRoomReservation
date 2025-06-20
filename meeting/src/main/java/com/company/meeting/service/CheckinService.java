package com.company.meeting.service;

import com.company.meeting.dto.CheckinRequest;
import com.company.meeting.dto.PageResult;
import com.company.meeting.entity.MeetingBooking;
import com.company.meeting.entity.MeetingCheckin;
import com.company.meeting.mapper.BookingMapper;
import com.company.meeting.mapper.CheckinMapper;
import com.company.meeting.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckinService {

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Transactional
    public void scanCheckin(CheckinRequest request) {
        MeetingBooking booking = bookingMapper.findById(request.getBookingId());
        if (booking == null) {
            throw new RuntimeException("会议预订不存在");
        }

        if (!"BOOKED".equals(booking.getBookingStatus())) {
            throw new RuntimeException("会议已取消或已结束");
        }

        // 检查是否在签到时间范围内（会议开始前30分钟到会议结束后）
        Date now = new Date();
        Date allowCheckinTime = new Date(booking.getStartTime().getTime() - 30 * 60 * 1000);
        if (now.before(allowCheckinTime) || now.after(booking.getEndTime())) {
            throw new RuntimeException("不在签到时间范围内");
        }

        Long userId = SecurityUtil.getCurrentUserId();
        
        // 检查是否已签到
        if (checkinMapper.existsCheckin(request.getBookingId(), userId) > 0) {
            throw new RuntimeException("您已经签到过了");
        }

        MeetingCheckin checkin = new MeetingCheckin();
        checkin.setBookingId(request.getBookingId());
        checkin.setUserId(userId);
        checkin.setCheckinType(request.getCheckinType());
        checkin.setCheckinStatus("PRESENT");
        checkin.setCheckinTime(now);

        checkinMapper.insert(checkin);
    }

    @Transactional
    public void manualCheckin(CheckinRequest request) {
        // 手动签到逻辑，可以根据工号查找用户
        request.setCheckinType("MANUAL");
        this.scanCheckin(request);
    }

    public PageResult<MeetingCheckin> getCheckinList(Long bookingId, int page, int size) {
        int offset = (page - 1) * size;
        
        List<MeetingCheckin> checkins = checkinMapper.findByBookingId(bookingId, offset, size);
        int total = checkinMapper.countByBookingId(bookingId);
        
        return new PageResult<>(checkins, total, page, size);
    }

    public Map<String, Object> getCheckinStatistics(Long bookingId) {
        MeetingBooking booking = bookingMapper.findById(bookingId);
        if (booking == null) {
            throw new RuntimeException("会议预订不存在");
        }

        int totalCheckins = checkinMapper.countByBookingId(bookingId);
        int expectedAttendees = booking.getAttendeeCount();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCheckins", totalCheckins);
        stats.put("expectedAttendees", expectedAttendees);
        stats.put("checkinRate", expectedAttendees > 0 ? (double) totalCheckins / expectedAttendees * 100 : 0);
        stats.put("meetingTitle", booking.getMeetingTitle());
        stats.put("roomName", booking.getRoomName());
        
        return stats;
    }
}