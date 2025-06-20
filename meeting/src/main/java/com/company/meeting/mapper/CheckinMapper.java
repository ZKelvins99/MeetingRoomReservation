package com.company.meeting.mapper;

import com.company.meeting.entity.MeetingCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CheckinMapper {
    MeetingCheckin findById(Long id);
    List<MeetingCheckin> findByBookingId(@Param("bookingId") Long bookingId, @Param("offset") int offset, @Param("size") int size);
    int countByBookingId(@Param("bookingId") Long bookingId);
    List<MeetingCheckin> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);
    int countByUserId(@Param("userId") Long userId);
    int insert(MeetingCheckin checkin);
    int update(MeetingCheckin checkin);
    int deleteById(Long id);
    int existsCheckin(@Param("bookingId") Long bookingId, @Param("userId") Long userId);
}