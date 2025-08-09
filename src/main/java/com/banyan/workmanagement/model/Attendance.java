package com.banyan.workmanagement.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "engineer_id", nullable = false)
    private Engineer engineer;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate attendanceDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime checkInTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime breakStartTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime breakEndTime;

    @Column(length = 20)
    private String status; // PRESENT, ABSENT, HALF_DAY, LATE

    @Column(length = 500)
    private String remarks;

    // Standard working hours (8 hours = 480 minutes)
    private static final int STANDARD_WORK_MINUTES = 480;

    // Optional link to work details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_details_id")
    private WorkDetails workDetails;

    // Location tracking
    private String checkInLocation;
    private String checkOutLocation;

    // Calculated fields
    @Column(name = "total_work_minutes")
    private Integer totalWorkMinutes;

    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes;

    @Column(name = "break_minutes")
    private Integer breakMinutes;

    // Constructors
    public Attendance() {
    }

    public Attendance(Engineer engineer, LocalDate attendanceDate) {
        this.engineer = engineer;
        this.attendanceDate = attendanceDate;
        this.status = "ABSENT";
    }

    // Methods to calculate work hours
    public void calculateWorkHours() {
        if (checkInTime != null && checkOutTime != null) {
            Duration totalDuration = Duration.between(checkInTime, checkOutTime);
            int totalMinutes = (int) totalDuration.toMinutes();

            // Subtract break time if available
            int breakTime = 0;
            if (breakStartTime != null && breakEndTime != null) {
                Duration breakDuration = Duration.between(breakStartTime, breakEndTime);
                breakTime = (int) breakDuration.toMinutes();
            }

            this.totalWorkMinutes = totalMinutes - breakTime;
            this.breakMinutes = breakTime;

            // Calculate overtime (anything above 8 hours)
            if (this.totalWorkMinutes > STANDARD_WORK_MINUTES) {
                this.overtimeMinutes = this.totalWorkMinutes - STANDARD_WORK_MINUTES;
            } else {
                this.overtimeMinutes = 0;
            }

            // Set status based on work hours
            if (this.totalWorkMinutes >= STANDARD_WORK_MINUTES) {
                this.status = "PRESENT";
            } else if (this.totalWorkMinutes >= STANDARD_WORK_MINUTES / 2) {
                this.status = "HALF_DAY";
            } else {
                this.status = "LATE";
            }
        }
    }

    // Helper methods to get formatted durations
    public String getFormattedTotalWorkHours() {
        if (totalWorkMinutes == null)
            return "0:00";
        int hours = totalWorkMinutes / 60;
        int minutes = totalWorkMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    public String getFormattedOvertimeHours() {
        if (overtimeMinutes == null || overtimeMinutes == 0)
            return "0:00";
        int hours = overtimeMinutes / 60;
        int minutes = overtimeMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    public String getFormattedBreakHours() {
        if (breakMinutes == null || breakMinutes == 0)
            return "0:00";
        int hours = breakMinutes / 60;
        int minutes = breakMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Engineer getEngineer() {
        return engineer;
    }

    public void setEngineer(Engineer engineer) {
        this.engineer = engineer;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
        // Auto-calculate when times are set
        if (checkOutTime != null) {
            calculateWorkHours();
        }
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalTime checkOutTime) {
        this.checkOutTime = checkOutTime;
        // Auto-calculate when times are set
        if (checkInTime != null) {
            calculateWorkHours();
        }
    }

    public LocalTime getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
        if (checkInTime != null && checkOutTime != null) {
            calculateWorkHours();
        }
    }

    public LocalTime getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(LocalTime breakEndTime) {
        this.breakEndTime = breakEndTime;
        if (checkInTime != null && checkOutTime != null) {
            calculateWorkHours();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public WorkDetails getWorkDetails() {
        return workDetails;
    }

    public void setWorkDetails(WorkDetails workDetails) {
        this.workDetails = workDetails;
    }

    public String getCheckInLocation() {
        return checkInLocation;
    }

    public void setCheckInLocation(String checkInLocation) {
        this.checkInLocation = checkInLocation;
    }

    public String getCheckOutLocation() {
        return checkOutLocation;
    }

    public void setCheckOutLocation(String checkOutLocation) {
        this.checkOutLocation = checkOutLocation;
    }

    public Integer getTotalWorkMinutes() {
        return totalWorkMinutes;
    }

    public void setTotalWorkMinutes(Integer totalWorkMinutes) {
        this.totalWorkMinutes = totalWorkMinutes;
    }

    public Integer getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(Integer overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public Integer getBreakMinutes() {
        return breakMinutes;
    }

    public void setBreakMinutes(Integer breakMinutes) {
        this.breakMinutes = breakMinutes;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", engineer=" + (engineer != null ? engineer.getName() : null) +
                ", attendanceDate=" + attendanceDate +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", status='" + status + '\'' +
                ", totalWorkHours='" + getFormattedTotalWorkHours() + '\'' +
                ", overtimeHours='" + getFormattedOvertimeHours() + '\'' +
                '}';
    }
}