package com.banyan.workmanagement.util;

import com.banyan.workmanagement.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ActivityLogger {

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Log a user activity
     * @param action The action being performed (LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW)
     * @param message Description of the activity
     */
    public void logActivity(String action, String message) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = "anonymous";
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            username = auth.getName();
        }
        
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String ipAddress = getClientIp(request);
            activityLogService.logActivity(username, action, message, ipAddress);
        } else {
            activityLogService.logActivity(username, action, message);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}