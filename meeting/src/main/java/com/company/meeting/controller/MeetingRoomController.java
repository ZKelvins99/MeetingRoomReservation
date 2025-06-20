package com.company.meeting.controller;

import com.company.meeting.common.Result;
import com.company.meeting.entity.MeetingRoom;
import com.company.meeting.service.MeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@CrossOrigin
public class MeetingRoomController {

    @Autowired
    private MeetingRoomService meetingRoomService;

    @GetMapping("/list")
    public Result<List<MeetingRoom>> getAllRooms() {
        try {
            List<MeetingRoom> rooms = meetingRoomService.getAllRooms();
            return Result.success(rooms);
        } catch (Exception e) {
            return Result.error("获取会议室列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/available")
    public Result<List<MeetingRoom>> getAvailableRooms(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            List<MeetingRoom> rooms = meetingRoomService.getAvailableRooms(startTime, endTime);
            return Result.success(rooms);
        } catch (Exception e) {
            return Result.error("获取可用会议室失败：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> addRoom(@Valid @RequestBody MeetingRoom room) {
        try {
            meetingRoomService.addRoom(room);
            return Result.success(null, "添加会议室成功");
        } catch (Exception e) {
            return Result.error("添加会议室失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateRoom(@Valid @RequestBody MeetingRoom room) {
        try {
            meetingRoomService.updateRoom(room);
            return Result.success(null, "更新会议室成功");
        } catch (Exception e) {
            return Result.error("更新会议室失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        try {
            meetingRoomService.deleteRoom(id);
            return Result.success(null, "删除会议室成功");
        } catch (Exception e) {
            return Result.error("删除会议室失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<MeetingRoom> getRoomById(@PathVariable Long id) {
        try {
            MeetingRoom room = meetingRoomService.getRoomById(id);
            return Result.success(room);
        } catch (Exception e) {
            return Result.error("获取会议室信息失败：" + e.getMessage());
        }
    }
}