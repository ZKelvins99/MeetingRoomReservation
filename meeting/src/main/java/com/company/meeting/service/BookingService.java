package com.company.meeting.service;

import com.company.meeting.dto.BookingRequest;
import com.company.meeting.dto.PageResult;
import com.company.meeting.entity.MeetingBooking;
import com.company.meeting.mapper.BookingMapper;
import com.company.meeting.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void bookRoom(BookingRequest request) {
        // 验证时间
        if (request.getEndTime().before(request.getStartTime())) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }
        
        if (request.getStartTime().before(new Date())) {
            throw new RuntimeException("不能预订过去的时间");
        }

        // 检查时间冲突
        if (bookingMapper.checkTimeConflict(request.getRoomId(), 
                request.getStartTime(), request.getEndTime(), null) > 0) {
            throw new RuntimeException("该时间段已被预订");
        }

        MeetingBooking booking = new MeetingBooking();
        booking.setRoomId(request.getRoomId());
        booking.setUserId(SecurityUtil.getCurrentUserId());
        booking.setMeetingTitle(request.getMeetingTitle());
        booking.setMeetingDesc(request.getMeetingDesc());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setAttendeeCount(request.getAttendeeCount());
        booking.setAttendees(request.getAttendees());
        booking.setRemindTime(request.getRemindTime());
        booking.setBookingStatus("BOOKED");
        booking.setIsReminded(0);

        bookingMapper.insert(booking);

        // 发送预订确认通知
        notificationService.sendBookingConfirmation(booking);
    }

    public PageResult<MeetingBooking> getMyBookings(int page, int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        int offset = (page - 1) * size;
        
        List<MeetingBooking> bookings = bookingMapper.findByUserId(userId, offset, size);
        int total = bookingMapper.countByUserId(userId);
        
        return new PageResult<>(bookings, total, page, size);
    }

    public PageResult<MeetingBooking> getAllBookings(int page, int size, String roomName, String status) {
        int offset = (page - 1) * size;
        
        List<MeetingBooking> bookings = bookingMapper.findAll(offset, size, roomName, status);
        int total = bookingMapper.countAll(roomName, status);
        
        return new PageResult<>(bookings, total, page, size);
    }

    @Transactional
    public void cancelBooking(Long id) {
        MeetingBooking booking = bookingMapper.findById(id);
        if (booking == null) {
            throw new RuntimeException("预订不存在");
        }

        Long currentUserId = SecurityUtil.getCurrentUserId();
        String currentUserRole = SecurityUtil.getCurrentUserRole();
        
        // 检查权限：只有预订人或管理员可以取消
        if (!booking.getUserId().equals(currentUserId) && !"ADMIN".equals(currentUserRole)) {
            throw new RuntimeException("无权取消此预订");
        }

        // 检查是否可以取消（会议开始前才能取消）
        if (booking.getStartTime().before(new Date())) {
            throw new RuntimeException("会议已开始，无法取消");
        }

        booking.setBookingStatus("CANCELLED");
        booking.setUpdateTime(new Date());
        bookingMapper.update(booking);

        // 发送取消通知
        notificationService.sendCancellationNotification(booking);
    }

    public String generateQRCode(Long bookingId) {
        MeetingBooking booking = bookingMapper.findById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预订不存在");
        }
        
        // 生成签到二维码链接
        return "http://localhost:8081/#/checkin?bookingId=" + bookingId + "&token=" + System.currentTimeMillis();
    }

    public MeetingBooking getBookingById(Long id) {
        return bookingMapper.findById(id);
    }
}