package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.ActivityLog;
import com.banyan.workmanagement.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    // Log an activity without IP address
    public void logActivity(String username, String action, String message) {
        ActivityLog log = new ActivityLog(username, action, message);
        activityLogRepository.save(log);
    }

    // Log an activity with IP address
    public void logActivity(String username, String action, String message, String ipAddress) {
        ActivityLog log = new ActivityLog(username, action, message, ipAddress);
        activityLogRepository.save(log);
    }

    // Get all logs with pagination
    public Page<ActivityLog> getAllLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    // Get logs by username
    public Page<ActivityLog> getLogsByUsername(String username, Pageable pageable) {
        return activityLogRepository.findByUsernameContainingIgnoreCaseOrderByTimestampDesc(username, pageable);
    }

    // Get logs by action
    public Page<ActivityLog> getLogsByAction(String action, Pageable pageable) {
        return activityLogRepository.findByActionContainingIgnoreCaseOrderByTimestampDesc(action, pageable);
    }

    // Get logs by date range
    public Page<ActivityLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return activityLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate, pageable);
    }

    // Get log by id
    public ActivityLog getLogById(Long id) {
        return activityLogRepository.findById(id).orElse(null);
    }

    // Delete logs by ids
    public void deleteLogsByIds(java.util.List<Long> ids) {
        java.util.List<ActivityLog> logsToDelete = activityLogRepository.findAllById(ids);
        activityLogRepository.deleteAll(logsToDelete);
    }

    // Archive logs by ids (for now, just delete as placeholder)
    public void archiveLogsByIds(java.util.List<Long> ids) {
        // Implement archiving logic here, for now just delete
        deleteLogsByIds(ids);
    }

    // Delete logs older than specified months
    public void deleteLogsOlderThanMonths(int months) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(months);
        activityLogRepository.deleteByTimestampBefore(cutoffDate);
    }

    // Scheduled task to delete logs older than 3 months daily at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledDeleteOldLogs() {
        deleteLogsOlderThanMonths(3);
    }
}
