package com.company.meeting.util;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetingRoomSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomSystemApplication.class, args);
    }
}