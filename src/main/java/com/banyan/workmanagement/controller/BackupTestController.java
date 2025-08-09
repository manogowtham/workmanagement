package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.service.DatabaseBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BackupTestController {

    @Autowired
    private DatabaseBackupService databaseBackupService;

    @GetMapping("/test-backup")
    public String testBackup() {
        try {
            databaseBackupService.performBackup();
            return "Backup triggered successfully.";
        } catch (Exception e) {
            return "Backup failed: " + e.getMessage();
        }
    }
}
