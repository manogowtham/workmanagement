package com.banyan.workmanagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.Set;
import java.util.stream.Collectors;

import com.banyan.workmanagement.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ActivityLogger activityLogger;

    @GetMapping({ "/" })
    public String homePage(Model model, Authentication authentication) {
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }

        // Log the home page visit activity
        activityLogger.logActivity("VIEW", "Visited home page");

        // Collect all granted authorities (permissions)
        Set<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Add permissions to the model for frontend access
        model.addAttribute("permissions", permissions);

        return "home";
    }
}
