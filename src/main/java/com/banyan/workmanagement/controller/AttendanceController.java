package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.model.Attendance;
import com.banyan.workmanagement.model.Engineer;
import com.banyan.workmanagement.model.User;
import com.banyan.workmanagement.model.WorkDetails;
import com.banyan.workmanagement.repository.EngineerRepository;
import com.banyan.workmanagement.repository.UserRepository;
import com.banyan.workmanagement.repository.WorkDetailsRepository;
import com.banyan.workmanagement.service.AttendanceService;
import com.banyan.workmanagement.util.ActivityLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private EngineerRepository engineerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkDetailsRepository workDetailsRepository;

    @Autowired
    private ActivityLogger activityLogger;

    // Handle GET request to /attendance/delete without id to avoid 404 static
    // resource error
    @GetMapping("/delete")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String handleDeleteWithoutId() {
        // Redirect to attendance list page
        return "redirect:/attendance/list";
    }

    // Handle POST request to /attendance/delete without id to avoid 404 static
    // resource error
    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String handlePostDeleteWithoutId() {
        // Redirect to attendance list page
        return "redirect:/attendance/list";
    }

    // Quick attendance page for engineers
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    @GetMapping("/quick")
    public String quickAttendancePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("currentUser", currentUser);

        // If user is an engineer, get their today's attendance
        if (currentUser.getEngineer() != null) {
            Optional<Attendance> todayAttendance = attendanceService
                    .getTodayAttendance(currentUser.getEngineer().getId());
            model.addAttribute("todayAttendance", todayAttendance.orElse(null));
            model.addAttribute("canCheckIn", !todayAttendance.isPresent() ||
                    todayAttendance.get().getCheckInTime() == null);
            model.addAttribute("canCheckOut", todayAttendance.isPresent() &&
                    todayAttendance.get().getCheckInTime() != null &&
                    todayAttendance.get().getCheckOutTime() == null);
        }

        activityLogger.logActivity("VIEW", "Visited quick attendance page");
        return "attendance/quick-attendance";
    }

    // Main attendance page
    @GetMapping
    public String attendancePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get recent attendance records
        List<Attendance> recentAttendance = attendanceService.getRecentAttendance();
        if (recentAttendance == null) {
            recentAttendance = java.util.Collections.emptyList();
        }

        // Check if user is engineer or has supervisory role
        boolean isEngineer = currentUser.getEngineer() != null;
        boolean isSupervisor = currentUser.getRole().getName().equals("SUPERADMIN") ||
                currentUser.getRole().getName().equals("SUPERVISOR");

        model.addAttribute("recentAttendance", recentAttendance);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isEngineer", isEngineer);
        model.addAttribute("isSupervisor", isSupervisor);
        model.addAttribute("engineers", engineerRepository.findAll());

        // If user is an engineer, get their today's attendance
        if (isEngineer) {
            Optional<Attendance> todayAttendance = attendanceService
                    .getTodayAttendance(currentUser.getEngineer().getId());
            model.addAttribute("todayAttendance", todayAttendance.orElse(null));
            model.addAttribute("canCheckIn", !todayAttendance.isPresent() ||
                    todayAttendance.get().getCheckInTime() == null);
            model.addAttribute("canCheckOut", todayAttendance.isPresent() &&
                    todayAttendance.get().getCheckInTime() != null &&
                    todayAttendance.get().getCheckOutTime() == null);
        }

        activityLogger.logActivity("VIEW", "Visited attendance page");
        return "attendance/attendance-main";
    }

    // Engineer check-in
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    @PostMapping("/check-in")
    public String checkIn(@RequestParam("checkInTime") @DateTimeFormat(pattern = "HH:mm") LocalTime checkInTime,
            @RequestParam(value = "checkInLocation", required = false) String location,
            @RequestParam(value = "redirect", required = false) String redirectTo,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (currentUser.getEngineer() == null) {
                throw new RuntimeException("Only engineers can check in");
            }

            attendanceService.checkIn(currentUser.getEngineer().getId(), checkInTime, location);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Checked in successfully at " + checkInTime.toString() + "!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        // Redirect to specified page or default to main attendance page
        if (redirectTo != null && !redirectTo.isEmpty()) {
            return "redirect:" + redirectTo;
        }
        return "redirect:/attendance";
    }

    // Engineer check-out
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String checkOut(@RequestParam("checkOutTime") @DateTimeFormat(pattern = "HH:mm") LocalTime checkOutTime,
            @RequestParam(value = "checkOutLocation", required = false) String location,
            @RequestParam(value = "redirect", required = false) String redirectTo,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (currentUser.getEngineer() == null) {
                throw new RuntimeException("Only engineers can check out");
            }

            Attendance attendance = attendanceService.checkOut(currentUser.getEngineer().getId(), checkOutTime,
                    location);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Checked out successfully at " + checkOutTime.toString() +
                            "! Total work time: " + attendance.getFormattedTotalWorkHours());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        // Redirect to specified page or default to main attendance page
        if (redirectTo != null && !redirectTo.isEmpty()) {
            return "redirect:" + redirectTo;
        }
        return "redirect:/attendance";
    }

    // View all attendance records (for supervisors)
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String attendanceList(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "engineerId", required = false) Long engineerId,
            Model model) {

        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Attendance> attendanceList;

        if (engineerId != null && engineerId > 0) {
            attendanceList = attendanceService.getAttendanceByEngineerAndDateRange(engineerId, startDate, endDate);
        } else {
            attendanceList = attendanceService.getAttendanceByDateRange(startDate, endDate);
        }

        if (attendanceList == null) {
            attendanceList = java.util.Collections.emptyList();
        }

        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("engineers", engineerRepository.findAll());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedEngineerId", engineerId);

        activityLogger.logActivity("VIEW", "Viewed attendance list");
        return "attendance/attendance-list";
    }

    // Add/Edit attendance form
    @GetMapping("/form")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String attendanceForm(@RequestParam(value = "id", required = false) Long id, Model model) {
        Attendance attendance;

        if (id != null) {
            attendance = attendanceService.getAttendanceById(id)
                    .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        } else {
            attendance = new Attendance();
            attendance.setAttendanceDate(LocalDate.now());
        }

        model.addAttribute("attendance", attendance);
        model.addAttribute("engineers", engineerRepository.findAll());
        model.addAttribute("workDetailsList", workDetailsRepository.findAll());
        model.addAttribute("isEdit", id != null);

        return "attendance/attendance-form";
    }

    // Save attendance
    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String saveAttendance(@ModelAttribute Attendance attendance,
            @RequestParam(value = "workDetailsId", required = false) Long workDetailsId,
            RedirectAttributes redirectAttributes) {
        try {
            // If engineer ID is provided, fetch the engineer
            if (attendance.getEngineer() != null && attendance.getEngineer().getId() != null) {
                Engineer engineer = engineerRepository.findById(attendance.getEngineer().getId())
                        .orElseThrow(() -> new RuntimeException("Engineer not found"));
                attendance.setEngineer(engineer);
            }

            // Link to work details if provided
            if (workDetailsId != null && workDetailsId > 0) {
                WorkDetails workDetails = workDetailsRepository.findById(workDetailsId)
                        .orElseThrow(() -> new RuntimeException("Work details not found"));
                attendance.setWorkDetails(workDetails);
            }

            attendanceService.saveAttendance(attendance);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance saved successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/attendance/form" + (attendance.getId() != null ? "?id=" + attendance.getId() : "");
        }

        return "redirect:/attendance/list";
    }

    // Delete attendance
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String deleteAttendance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.deleteAttendance(id);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance record deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/attendance/list";
    }

    // Attendance reports
    @GetMapping("/reports")
    @PreAuthorize("hasAnyAuthority('ENGINEER_ACCESS', 'SUPERADMIN')")
    public String attendanceReports(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "engineerId", required = false) Long engineerId,
            Model model) {

        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        if (engineerId != null && engineerId > 0) {
            // Individual engineer report
            Map<String, Object> summary = attendanceService.getAttendanceSummary(engineerId, startDate, endDate);
            List<Attendance> attendanceList = attendanceService.getAttendanceByEngineerAndDateRange(engineerId,
                    startDate, endDate);
            List<Attendance> overtimeRecords = attendanceService.getOvertimeRecordsByEngineer(engineerId, startDate,
                    endDate);

            Engineer engineer = engineerRepository.findById(engineerId).orElse(null);

            model.addAttribute("summary", summary);
            model.addAttribute("attendanceList", attendanceList);
            model.addAttribute("overtimeRecords", overtimeRecords);
            model.addAttribute("selectedEngineer", engineer);
        } else {
            // All engineers summary
            List<Map<String, Object>> statistics = attendanceService.getAttendanceStatistics(startDate, endDate);
            List<Attendance> overtimeRecords = attendanceService.getOvertimeRecords(startDate, endDate);

            model.addAttribute("statistics", statistics);
            model.addAttribute("overtimeRecords", overtimeRecords);
        }

        model.addAttribute("engineers", engineerRepository.findAll());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedEngineerId", engineerId);

        activityLogger.logActivity("VIEW", "Viewed attendance reports");
        return "attendance/attendance-reports";
    }

    // Overtime records
    @GetMapping("/overtime")
    public String overtimeRecords(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<Attendance> overtimeRecords = attendanceService.getOvertimeRecords(startDate, endDate);

        model.addAttribute("overtimeRecords", overtimeRecords);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        activityLogger.logActivity("VIEW", "Viewed overtime records");
        return "attendance/overtime-records";
    }

    // API endpoints for AJAX calls
    @GetMapping("/api/today-status/{engineerId}")
    @ResponseBody
    public Map<String, Object> getTodayStatus(@PathVariable Long engineerId) {
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(engineerId);

        Map<String, Object> response = new java.util.HashMap<>();
        if (todayAttendance.isPresent()) {
            Attendance attendance = todayAttendance.get();
            response.put("hasRecord", true);
            response.put("checkInTime", attendance.getCheckInTime());
            response.put("checkOutTime", attendance.getCheckOutTime());
            response.put("status", attendance.getStatus());
            response.put("totalWorkHours", attendance.getFormattedTotalWorkHours());
            response.put("overtimeHours", attendance.getFormattedOvertimeHours());
        } else {
            response.put("hasRecord", false);
        }

        return response;
    }

    // Bulk create attendance for all engineers for a specific date
    @PostMapping("/bulk-create")
    public String bulkCreateAttendance(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            RedirectAttributes redirectAttributes) {
        try {
            attendanceService.createBulkAttendanceForDate(date);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Bulk attendance records created for " + date);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/attendance/list";
    }
}