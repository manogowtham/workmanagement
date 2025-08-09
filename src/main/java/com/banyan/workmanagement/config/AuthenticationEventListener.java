package com.banyan.workmanagement.config;

import com.banyan.workmanagement.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    @Autowired
    private ActivityLogger activityLogger;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = authentication.getName();
        if (username != null && !username.equals("anonymousUser")) {
            activityLogger.logActivity("LOGIN", "User logged in successfully");
        }
    }

    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            if (username != null && !username.equals("anonymousUser")) {
                activityLogger.logActivity("LOGOUT", "User logged out");
            }
        }
    }
}