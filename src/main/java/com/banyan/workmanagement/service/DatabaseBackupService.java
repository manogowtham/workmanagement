package com.banyan.workmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;

@Service
public class DatabaseBackupService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${backup.directory}")
    private String backupDirectory;

    private static final String PROJECT_DIR = "D:\\Website\\workmanagement";

    // Scheduled to run daily at 12 PM (noon) as per user request
    @Scheduled(cron = "0 0 12 * * ?")
    public void backupDatabaseAndProject() {
        performBackup();
    }

    // Public method to manually trigger backup for testing
    public void performBackup() {
        try {
            System.out.println("Starting backup process...");

            // Ensure backup directory exists
            File backupDir = new File(backupDirectory);
            if (!backupDir.exists()) {
                boolean created = backupDir.mkdirs();
                System.out.println("Backup directory created: " + created + " at " + backupDirectory);
            }

            // Backup database
            backupDatabase();

            // Upload backup files to Google Drive
            uploadBackupFilesToGoogleDrive();

            // Backup project directory
            // backupProjectDirectory(); // Disabled as per user request to keep only
            // database backup

            System.out.println("Database backup completed successfully.");
        } catch (Exception e) {
            System.err.println("Error during backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void backupDatabase() throws IOException, InterruptedException {
        String dbName = extractDatabaseName(dbUrl);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFile = backupDirectory + "db_backup_" + timestamp + ".sql";

        // Use full path to mysqldump
        String mysqldumpPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";

        // Build command as list of strings for ProcessBuilder
        ProcessBuilder pb = new ProcessBuilder(
                mysqldumpPath,
                "-u" + dbUsername,
                "-p" + dbPassword,
                dbName,
                "-r",
                backupFile);

        System.out.println("Executing backup command: " + String.join(" ", pb.command()).replace(dbPassword, "****"));

        Process process = pb.start();
        int processComplete = process.waitFor();

        if (processComplete == 0) {
            System.out.println("Database backup created successfully at " + backupFile);
        } else {
            throw new IOException("Failed to create database backup. mysqldump exited with code " + processComplete);
        }
    }

    private String extractDatabaseName(String url) {
        // Example URL: jdbc:mysql://localhost:3306/workmanagement
        int lastSlash = url.lastIndexOf("/");
        if (lastSlash == -1) {
            return "";
        }
        int questionMark = url.indexOf("?", lastSlash);
        if (questionMark == -1) {
            return url.substring(lastSlash + 1);
        } else {
            return url.substring(lastSlash + 1, questionMark);
        }
    }

    private void backupProjectDirectory() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String zipFileName = backupDirectory + "project_backup_" + timestamp + ".zip";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            Path sourcePath = Paths.get(PROJECT_DIR);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println(
                                    "Error while adding file to zip: " + path.toString() + " - " + e.getMessage());
                        }
                    });
        }
        System.out.println("Project directory backup created successfully at " + zipFileName);
    }

    private void uploadBackupFilesToGoogleDrive() {
        try {
            GoogleDriveService googleDriveService = new GoogleDriveService();

            File backupDir = new File(backupDirectory);
            File[] files = backupDir
                    .listFiles((dir, name) -> name != null && (name.endsWith(".sql") || name.endsWith(".zip")));

            if (files != null) {
                for (File file : files) {
                    try {
                        String mimeType = Files.probeContentType(file.toPath());
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }
                        String fileId = googleDriveService.uploadFile(file, mimeType);
                        System.out.println("Uploaded file to Google Drive: " + file.getName() + " with ID: " + fileId);
                    } catch (Exception e) {
                        System.err.println(
                                "Failed to upload file to Google Drive: " + file.getName() + " - " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during Google Drive upload: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
