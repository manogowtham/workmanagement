package com.banyan.workmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private LocalDateTime timestamp;
    private String action;
    private String message;
    private String ipAddress;

    // Constructors
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLog(String username, String action, String message) {
        this.username = username;
        this.action = action;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLog(String username, String action, String message, String ipAddress) {
        this.username = username;
        this.action = action;
        this.message = message;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}