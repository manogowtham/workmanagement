package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.model.ActivityLog;
import com.banyan.workmanagement.service.ActivityLogService;
import com.banyan.workmanagement.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ActivityLogger activityLogger;

    @GetMapping("/activity-logs")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public String viewLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        // Log the view action
        activityLogger.logActivity("VIEW", "Viewed activity logs");

        Page<ActivityLog> logs;
        PageRequest pageRequest = PageRequest.of(page, size);

        if (username != null && !username.isEmpty()) {
            logs = activityLogService.getLogsByUsername(username, pageRequest);
            model.addAttribute("filterType", "username");
            model.addAttribute("filterValue", username);
        } else if (action != null && !action.isEmpty()) {
            logs = activityLogService.getLogsByAction(action, pageRequest);
            model.addAttribute("filterType", "action");
            model.addAttribute("filterValue", action);
        } else if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime startDateTime = LocalDate.parse(startDate, formatter).atStartOfDay();
                LocalDateTime endDateTime = LocalDate.parse(endDate, formatter).atTime(LocalTime.MAX);
                logs = activityLogService.getLogsByDateRange(startDateTime, endDateTime, pageRequest);
                model.addAttribute("filterType", "dateRange");
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
            } catch (Exception e) {
                logs = activityLogService.getAllLogs(pageRequest);
                model.addAttribute("errorMessage", "Invalid date format. Please use YYYY-MM-DD format.");
            }
        } else {
            logs = activityLogService.getAllLogs(pageRequest);
        }

        model.addAttribute("logs", logs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logs.getTotalPages());
        model.addAttribute("totalItems", logs.getTotalElements());

        return "activity-logs";
    }

    @GetMapping("/activity-logs/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public String viewLogDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        ActivityLog log = activityLogService.getLogById(id);
        if (log == null) {
            model.addAttribute("errorMessage", "Activity log not found.");
            return "activity-log-detail";
        }
        model.addAttribute("log", log);
        return "activity-log-detail";
    }

    @GetMapping("/api/activity-logs/{id}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public org.springframework.http.ResponseEntity<ActivityLog> getLogDetailsApi(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        ActivityLog log = activityLogService.getLogById(id);
        if (log == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        return org.springframework.http.ResponseEntity.ok(log);
    }

    @PostMapping("/activity-logs/bulk-delete")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public String bulkDelete(@org.springframework.web.bind.annotation.RequestParam("ids") java.util.List<Long> ids,
            Model model) {
        activityLogService.deleteLogsByIds(ids);
        activityLogger.logActivity("DELETE", "Bulk deleted activity logs: " + ids);
        return "redirect:/activity-logs";
    }

    @PostMapping("/activity-logs/bulk-archive")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public String bulkArchive(@org.springframework.web.bind.annotation.RequestParam("ids") java.util.List<Long> ids,
            Model model) {
        activityLogService.archiveLogsByIds(ids);
        activityLogger.logActivity("ARCHIVE", "Bulk archived activity logs: " + ids);
        return "redirect:/activity-logs";
    }
}