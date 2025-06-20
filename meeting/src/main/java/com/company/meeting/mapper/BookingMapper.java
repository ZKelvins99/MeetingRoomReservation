package com.company.meeting.mapper;

import com.company.meeting.entity.MeetingBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface BookingMapper {
    MeetingBooking findById(Long id);
    List<MeetingBooking> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);
    int countByUserId(@Param("userId") Long userId);
    List<MeetingBooking> findAll(@Param("offset") int offset, @Param("size") int size, 
                                @Param("roomName") String roomName, @Param("status") String status);
    int countAll(@Param("roomName") String roomName, @Param("status") String status);
    int insert(MeetingBooking booking);
    int update(MeetingBooking booking);
    int deleteById(Long id);
    int checkTimeConflict(@Param("roomId") Long roomId, @Param("startTime") Date startTime, 
                         @Param("endTime") Date endTime, @Param("excludeId") Long excludeId);
    List<MeetingBooking> findPendingReminders();
    int updateReminderStatus(@Param("id") Long id);
}