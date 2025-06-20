package com.company.meeting.mapper;

import com.company.meeting.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll();
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
    int countByRole(@Param("role") String role);
}