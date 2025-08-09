package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.ActivityLog;
import com.banyan.workmanagement.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ActivityLogBackupService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private static final String BACKUP_DIR = "D:/Website/";

    // Scheduled to run daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackupActivityLogsToCSV() {
        backupActivityLogsToCSV();
    }

    // Public method to manually trigger activity log backup
    public void backupActivityLogsToCSV() {
        List<ActivityLog> logs = activityLogRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String filename = BACKUP_DIR + "activity_logs_backup_" + timestamp + ".csv";

        try (FileWriter writer = new FileWriter(filename)) {
            // Write CSV header
            writer.append("ID,Username,Timestamp,Action,Message,IPAddress\n");

            for (ActivityLog log : logs) {
                writer.append(String.valueOf(log.getId())).append(",");
                writer.append(escapeCsv(log.getUsername())).append(",");
                writer.append(log.getTimestamp().toString()).append(",");
                writer.append(escapeCsv(log.getAction())).append(",");
                writer.append(escapeCsv(log.getMessage())).append(",");
                writer.append(escapeCsv(log.getIpAddress())).append("\n");
            }

            writer.flush();
            System.out.println("Activity logs backed up successfully to " + filename);
        } catch (IOException e) {
            System.err.println("Error backing up activity logs: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
