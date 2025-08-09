package com.banyan.workmanagement.service;

import com.banyan.workmanagement.model.Attendance;
import com.banyan.workmanagement.model.Engineer;
import com.banyan.workmanagement.model.WorkDetails;
import com.banyan.workmanagement.repository.AttendanceRepository;
import com.banyan.workmanagement.repository.EngineerRepository;
import com.banyan.workmanagement.repository.WorkDetailsRepository;
import com.banyan.workmanagement.util.ActivityLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EngineerRepository engineerRepository;

    @Autowired
    private WorkDetailsRepository workDetailsRepository;

    @Autowired
    private ActivityLogger activityLogger;

    // Create or update attendance record
    public Attendance saveAttendance(Attendance attendance) {
        if (attendance.getId() == null) {
            // New attendance record
            activityLogger.logActivity("CREATE",
                    "Created attendance record for " + attendance.getEngineer().getName() +
                            " on " + attendance.getAttendanceDate());
        } else {
            // Update existing record
            activityLogger.logActivity("UPDATE",
                    "Updated attendance record for " + attendance.getEngineer().getName() +
                            " on " + attendance.getAttendanceDate());
        }

        // Calculate work hours before saving
        attendance.calculateWorkHours();
        return attendanceRepository.save(attendance);
    }

    // Mark check-in for engineer
    public Attendance checkIn(Long engineerId, LocalTime checkInTime, String location) {
        Engineer engineer = engineerRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        LocalDate today = LocalDate.now();

        // Check if attendance already exists for today
        Optional<Attendance> existingAttendance = attendanceRepository
                .findByEngineerAndAttendanceDate(engineer, today);

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            attendance = existingAttendance.get();
            if (attendance.getCheckInTime() != null) {
                throw new RuntimeException("Already checked in for today");
            }
        } else {
            attendance = new Attendance(engineer, today);
        }

        attendance.setCheckInTime(checkInTime);
        attendance.setCheckInLocation(location);
        attendance.setStatus("PRESENT");

        activityLogger.logActivity("CHECK_IN",
                "Engineer " + engineer.getName() + " checked in at " + checkInTime);

        return saveAttendance(attendance);
    }

    // Mark check-out for engineer
    public Attendance checkOut(Long engineerId, LocalTime checkOutTime, String location) {
        Engineer engineer = engineerRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findByEngineerAndAttendanceDate(engineer, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out for today");
        }

        attendance.setCheckOutTime(checkOutTime);
        attendance.setCheckOutLocation(location);

        activityLogger.logActivity("CHECK_OUT",
                "Engineer " + engineer.getName() + " checked out at " + checkOutTime);

        return saveAttendance(attendance);
    }

    // Mark break times
    public Attendance markBreak(Long attendanceId, LocalTime breakStart, LocalTime breakEnd) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        attendance.setBreakStartTime(breakStart);
        attendance.setBreakEndTime(breakEnd);

        activityLogger.logActivity("BREAK_UPDATE",
                "Updated break times for " + attendance.getEngineer().getName() +
                        " on " + attendance.getAttendanceDate());

        return saveAttendance(attendance);
    }

    // Link attendance to work details
    public Attendance linkToWorkDetails(Long attendanceId, Long workDetailsId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        WorkDetails workDetails = workDetailsRepository.findById(workDetailsId)
                .orElseThrow(() -> new RuntimeException("Work details not found"));

        attendance.setWorkDetails(workDetails);

        activityLogger.logActivity("LINK_WORK",
                "Linked attendance to work details for " + attendance.getEngineer().getName());

        return saveAttendance(attendance);
    }

    // Get attendance by ID
    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    // Get all attendance records
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    // Get attendance for specific engineer
    public List<Attendance> getAttendanceByEngineer(Long engineerId) {
        Engineer engineer = engineerRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));
        return attendanceRepository.findByEngineerOrderByAttendanceDateDesc(engineer);
    }

    // Get attendance for date range
    public List<Attendance> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository
                .findByAttendanceDateBetweenOrderByAttendanceDateDescEngineerNameAsc(startDate, endDate);
    }

    // Get attendance for engineer in date range
    public List<Attendance> getAttendanceByEngineerAndDateRange(Long engineerId,
            LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findAttendanceByEngineerAndDateRange(engineerId, startDate, endDate);
    }

    // Get today's attendance for engineer
    public Optional<Attendance> getTodayAttendance(Long engineerId) {
        Engineer engineer = engineerRepository.findById(engineerId)
                .orElseThrow(() -> new RuntimeException("Engineer not found"));
        return attendanceRepository.findByEngineerAndAttendanceDate(engineer, LocalDate.now());
    }

    // Check if engineer can check in today
    public boolean canCheckInToday(Long engineerId) {
        Optional<Attendance> todayAttendance = getTodayAttendance(engineerId);
        return !todayAttendance.isPresent() || todayAttendance.get().getCheckInTime() == null;
    }

    // Get overtime records
    public List<Attendance> getOvertimeRecords(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findOvertimeRecords(startDate, endDate);
    }

    // Get overtime records for engineer
    public List<Attendance> getOvertimeRecordsByEngineer(Long engineerId,
            LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findOvertimeRecordsByEngineer(engineerId, startDate, endDate);
    }

    // Generate attendance summary for engineer
    public Map<String, Object> getAttendanceSummary(Long engineerId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        // Get total work hours and overtime
        Integer totalWorkMinutes = attendanceRepository
                .getTotalWorkMinutesByEngineerAndDateRange(engineerId, startDate, endDate);
        Integer totalOvertimeMinutes = attendanceRepository
                .getTotalOvertimeMinutesByEngineerAndDateRange(engineerId, startDate, endDate);

        // Count attendance by status
        Long presentDays = attendanceRepository
                .countAttendanceByEngineerStatusAndDateRange(engineerId, "PRESENT", startDate, endDate);
        Long halfDays = attendanceRepository
                .countAttendanceByEngineerStatusAndDateRange(engineerId, "HALF_DAY", startDate, endDate);
        Long lateDays = attendanceRepository
                .countAttendanceByEngineerStatusAndDateRange(engineerId, "LATE", startDate, endDate);
        Long absentDays = attendanceRepository
                .countAttendanceByEngineerStatusAndDateRange(engineerId, "ABSENT", startDate, endDate);

        // Convert minutes to formatted hours
        summary.put("totalWorkHours", formatMinutesToHours(totalWorkMinutes));
        summary.put("totalOvertimeHours", formatMinutesToHours(totalOvertimeMinutes));
        summary.put("presentDays", presentDays);
        summary.put("halfDays", halfDays);
        summary.put("lateDays", lateDays);
        summary.put("absentDays", absentDays);
        summary.put("totalDays", presentDays + halfDays + lateDays + absentDays);

        return summary;
    }

    // Generate attendance statistics for all engineers
    public List<Map<String, Object>> getAttendanceStatistics(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = attendanceRepository.getAttendanceStatistics(startDate, endDate);

        return results.stream().map(result -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("engineerName", result[0]);
            stat.put("totalDays", result[1]);
            Number totalWorkMinutesNum = (Number) result[2];
            int totalWorkMinutes = totalWorkMinutesNum != null ? totalWorkMinutesNum.intValue() : 0;
            Number totalOvertimeMinutesNum = (Number) result[3];
            int totalOvertimeMinutes = totalOvertimeMinutesNum != null ? totalOvertimeMinutesNum.intValue() : 0;
            stat.put("totalWorkHours", formatMinutesToHours(totalWorkMinutes));
            stat.put("totalOvertimeHours", formatMinutesToHours(totalOvertimeMinutes));
            return stat;
        }).collect(Collectors.toList());
    }

    // Get recent attendance (last 30 days)
    public List<Attendance> getRecentAttendance() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return attendanceRepository.findRecentAttendance(thirtyDaysAgo);
    }

    // Delete attendance record
    public void deleteAttendance(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        activityLogger.logActivity("DELETE",
                "Deleted attendance record for " + attendance.getEngineer().getName() +
                        " on " + attendance.getAttendanceDate());

        attendanceRepository.deleteById(id);
    }

    // Get distinct engineer names from attendance
    public List<String> getEngineerNamesFromAttendance() {
        return attendanceRepository.findDistinctEngineerNames();
    }

    // Helper method to format minutes to HH:MM format
    private String formatMinutesToHours(Integer minutes) {
        if (minutes == null || minutes == 0)
            return "0:00";
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%d:%02d", hours, mins);
    }

    // Create bulk attendance records for all engineers for a specific date
    public void createBulkAttendanceForDate(LocalDate date) {
        List<Engineer> allEngineers = engineerRepository.findAll();

        for (Engineer engineer : allEngineers) {
            Optional<Attendance> existing = attendanceRepository
                    .findByEngineerAndAttendanceDate(engineer, date);

            if (!existing.isPresent()) {
                Attendance attendance = new Attendance(engineer, date);
                attendanceRepository.save(attendance);
            }
        }

        activityLogger.logActivity("BULK_CREATE",
                "Created bulk attendance records for " + date);
    }

    // Get attendance records with work details
    public List<Attendance> getAttendanceWithWorkDetails() {
        return attendanceRepository.findAttendanceWithWorkDetails();
    }

    // Get attendance records without work details
    public List<Attendance> getAttendanceWithoutWorkDetails() {
        return attendanceRepository.findAttendanceWithoutWorkDetails();
    }
}