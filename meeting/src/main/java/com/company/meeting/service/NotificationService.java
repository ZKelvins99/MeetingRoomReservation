package com.company.meeting.service;

import com.company.meeting.entity.MeetingBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendBookingConfirmation(MeetingBooking booking) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(booking.getUserName() + "@company.com"); // 假设邮箱格式
                message.setSubject("会议室预订确认 - " + booking.getMeetingTitle());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String content = String.format(
                    "您好，\n\n您的会议室预订已确认：\n\n" +
                    "会议主题：%s\n" +
                    "会议室：%s\n" +
                    "时间：%s - %s\n" +
                    "参会人数：%d人\n\n" +
                    "请准时参加会议。\n\n" +
                    "此邮件由系统自动发送，请勿回复。",
                    booking.getMeetingTitle(),
                    booking.getRoomName(),
                    sdf.format(booking.getStartTime()),
                    sdf.format(booking.getEndTime()),
                    booking.getAttendeeCount()
                );
                
                message.setText(content);
                mailSender.send(message);
            }
        } catch (Exception e) {
            // 邮件发送失败不影响主流程
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }

    public void sendCancellationNotification(MeetingBooking booking) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(booking.getUserName() + "@company.com");
                message.setSubject("会议室预订取消通知 - " + booking.getMeetingTitle());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String content = String.format(
                    "您好，\n\n您的会议室预订已取消：\n\n" +
                    "会议主题：%s\n" +
                    "会议室：%s\n" +
                    "原定时间：%s - %s\n\n" +
                    "如有疑问，请联系管理员。\n\n" +
                    "此邮件由系统自动发送，请勿回复。",
                    booking.getMeetingTitle(),
                    booking.getRoomName(),
                    sdf.format(booking.getStartTime()),
                    sdf.format(booking.getEndTime())
                );
                
                message.setText(content);
                mailSender.send(message);
            }
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }

    public void sendMeetingReminder(MeetingBooking booking) {
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(booking.getUserName() + "@company.com");
                message.setSubject("会议提醒 - " + booking.getMeetingTitle());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String content = String.format(
                    "您好，\n\n您有一个会议即将开始：\n\n" +
                    "会议主题：%s\n" +
                    "会议室：%s\n" +
                    "开始时间：%s\n" +
                    "参会人数：%d人\n\n" +
                    "请提前到达会议室准备。\n\n" +
                    "此邮件由系统自动发送，请勿回复。",
                    booking.getMeetingTitle(),
                    booking.getRoomName(),
                    sdf.format(booking.getStartTime()),
                    booking.getAttendeeCount()
                );
                
                message.setText(content);
                mailSender.send(message);
            }
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
        }
    }
}