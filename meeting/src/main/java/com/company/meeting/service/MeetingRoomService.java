package com.company.meeting.service;

import com.company.meeting.entity.MeetingRoom;
import com.company.meeting.mapper.MeetingRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class MeetingRoomService {

    @Autowired
    private MeetingRoomMapper meetingRoomMapper;

    public List<MeetingRoom> getAllRooms() {
        return meetingRoomMapper.findAll();
    }

    public List<MeetingRoom> getAvailableRooms(String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            return meetingRoomMapper.findAvailable(start, end);
        } catch (ParseException e) {
            throw new RuntimeException("时间格式错误");
        }
    }

    @Transactional
    public void addRoom(MeetingRoom room) {
        if (room.getRoomName() == null || room.getRoomName().trim().isEmpty()) {
            throw new RuntimeException("会议室名称不能为空");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new RuntimeException("会议室容量必须大于0");
        }
        
        room.setStatus(1); // 默认可用状态
        meetingRoomMapper.insert(room);
    }

    @Transactional
    public void updateRoom(MeetingRoom room) {
        if (room.getId() == null) {
            throw new RuntimeException("会议室ID不能为空");
        }
        
        MeetingRoom existingRoom = meetingRoomMapper.findById(room.getId());
        if (existingRoom == null) {
            throw new RuntimeException("会议室不存在");
        }
        
        meetingRoomMapper.update(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        MeetingRoom room = meetingRoomMapper.findById(id);
        if (room == null) {
            throw new RuntimeException("会议室不存在");
        }
        
        meetingRoomMapper.deleteById(id);
    }

    public MeetingRoom getRoomById(Long id) {
        return meetingRoomMapper.findById(id);
    }
}