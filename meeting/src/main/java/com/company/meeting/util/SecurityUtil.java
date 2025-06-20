package com.company.meeting.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        // 简化实现，实际项目中应该从JWT token中解析或从数据库查询
        String username = getCurrentUsername();
        if ("admin".equals(username)) {
            return 1L;
        } else if ("user001".equals(username)) {
            return 2L;
        }
        return 1L; // 默认返回1L，实际应该从数据库查询
    }

    public static String getCurrentUserRole() {
        String username = getCurrentUsername();
        if ("admin".equals(username)) {
            return "ADMIN";
        }
        return "USER";
    }
}