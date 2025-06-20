package com.company.meeting.task;

import com.company.meeting.entity.MeetingBooking;
import com.company.meeting.mapper.BookingMapper;
import com.company.meeting.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderTask {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * 每分钟检查一次需要发送提醒的会议
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    public void sendMeetingReminders() {
        try {
            List<MeetingBooking> pendingReminders = bookingMapper.findPendingReminders();
            
            for (MeetingBooking booking : pendingReminders) {
                // 发送提醒邮件
                notificationService.sendMeetingReminder(booking);
                
                // 更新提醒状态
                bookingMapper.updateReminderStatus(booking.getId());
            }
            
            if (!pendingReminders.isEmpty()) {
                System.out.println("发送了 " + pendingReminders.size() + " 个会议提醒");
            }
        } catch (Exception e) {
            System.err.println("发送会议提醒失败: " + e.getMessage());
        }
    }

    /**
     * 每天凌晨2点清理过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        try {
            // 这里可以添加清理过期签到记录、通知等逻辑
            System.out.println("执行数据清理任务...");
        } catch (Exception e) {
            System.err.println("数据清理失败: " + e.getMessage());
        }
    }
}