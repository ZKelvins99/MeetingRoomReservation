package com.company.meeting.controller;

import com.company.meeting.common.Result;
import com.company.meeting.dto.BookingRequest;
import com.company.meeting.dto.PageResult;
import com.company.meeting.entity.MeetingBooking;
import com.company.meeting.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/bookings")
@CrossOrigin
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book")
    public Result<Void> bookRoom(@Valid @RequestBody BookingRequest request) {
        try {
            bookingService.bookRoom(request);
            return Result.success(null, "预订成功");
        } catch (Exception e) {
            return Result.error("预订失败：" + e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<PageResult<MeetingBooking>> getMyBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageResult<MeetingBooking> result = bookingService.getMyBookings(page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取我的预订失败：" + e.getMessage());
        }
    }

    @GetMapping("/all")
    public Result<PageResult<MeetingBooking>> getAllBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) String status) {
        try {
            PageResult<MeetingBooking> result = bookingService.getAllBookings(page, size, roomName, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取预订列表失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);
            return Result.success(null, "取消预订成功");
        } catch (Exception e) {
            return Result.error("取消预订失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/qrcode")
    public Result<String> getBookingQRCode(@PathVariable Long id) {
        try {
            String qrCode = bookingService.generateQRCode(id);
            return Result.success(qrCode);
        } catch (Exception e) {
            return Result.error("生成二维码失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<MeetingBooking> getBookingById(@PathVariable Long id) {
        try {
            MeetingBooking booking = bookingService.getBookingById(id);
            return Result.success(booking);
        } catch (Exception e) {
            return Result.error("获取预订信息失败：" + e.getMessage());
        }
    }
}