package com.company.meeting.mapper;

import com.company.meeting.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface MeetingRoomMapper {
    MeetingRoom findById(Long id);
    List<MeetingRoom> findAll();
    List<MeetingRoom> findAvailable(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    int insert(MeetingRoom room);
    int update(MeetingRoom room);
    int deleteById(Long id);
    int countByStatus(@Param("status") Integer status);
}