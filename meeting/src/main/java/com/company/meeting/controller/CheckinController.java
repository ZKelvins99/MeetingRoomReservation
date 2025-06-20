package com.company.meeting.controller;

import com.company.meeting.common.Result;
import com.company.meeting.dto.CheckinRequest;
import com.company.meeting.dto.PageResult;
import com.company.meeting.entity.MeetingCheckin;
import com.company.meeting.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/checkin")
@CrossOrigin
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @PostMapping("/scan")
    public Result<Void> scanCheckin(@Valid @RequestBody CheckinRequest request) {
        try {
            checkinService.scanCheckin(request);
            return Result.success(null, "签到成功");
        } catch (Exception e) {
            return Result.error("签到失败：" + e.getMessage());
        }
    }

    @PostMapping("/manual")
    public Result<Void> manualCheckin(@Valid @RequestBody CheckinRequest request) {
        try {
            checkinService.manualCheckin(request);
            return Result.success(null, "手动签到成功");
        } catch (Exception e) {
            return Result.error("手动签到失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<PageResult<MeetingCheckin>> getCheckinList(
            @RequestParam Long bookingId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageResult<MeetingCheckin> result = checkinService.getCheckinList(bookingId, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取签到列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/statistics/{bookingId}")
    public Result<Map<String, Object>> getCheckinStatistics(@PathVariable Long bookingId) {
        try {
            Map<String, Object> stats = checkinService.getCheckinStatistics(bookingId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取签到统计失败：" + e.getMessage());
        }
    }
}