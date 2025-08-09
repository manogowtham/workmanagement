package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.model.WorkDetails;
import com.banyan.workmanagement.repository.WorkDetailsRepository;
import com.banyan.workmanagement.util.ActivityLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TimeManagementController {

    @Autowired
    private WorkDetailsRepository workDetailsRepository;

    @Autowired
    private ActivityLogger activityLogger;

    // Serve the HTML page
    @GetMapping("/time-management")
    public String timeManagementPage() {
        activityLogger.logActivity("VIEW", "Visited time management page");
        return "time-management"; // This should match time-management.html in templates
    }

    // API to get all work details (not paginated)
    @GetMapping("/api/work-details")
    @ResponseBody
    public List<WorkDetailsDTO> getWorkDetails() {
        List<WorkDetails> all = workDetailsRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return all.stream().map(wd -> new WorkDetailsDTO(
                wd.getDate(),
                wd.getInTime() != null ? wd.getInTime().toString() : "",
                wd.getOutTime() != null ? wd.getOutTime().toString() : "",
                wd.getEngineerName(),
                wd.getCustomerName()))
                .collect(Collectors.toList());
    }

    // New API to get filtered work details by engineer, customer, and date range
    @GetMapping("/api/work-details-filtered")
    @ResponseBody
    public List<WorkDetailsDTO> getFilteredWorkDetails(
            @RequestParam String engineerName,
            @RequestParam String customerName,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<WorkDetails> filtered = workDetailsRepository.findByEngineerAndCustomerAndDateRange(engineerName,
                customerName, start, end);

        return filtered.stream().map(wd -> new WorkDetailsDTO(
                wd.getDate(),
                wd.getInTime() != null ? wd.getInTime().toString() : "",
                wd.getOutTime() != null ? wd.getOutTime().toString() : "",
                wd.getEngineerName(),
                wd.getCustomerName()))
                .collect(Collectors.toList());
    }

    // New API to get distinct engineer names
    @GetMapping("/api/time-management-engineers")
    @ResponseBody
    public List<String> getEngineers() {
        return workDetailsRepository.findDistinctEngineerNames();
    }

    // New API to get distinct customer names
    @GetMapping("/api/time-management-customers")
    @ResponseBody
    public List<String> getCustomers() {
        return workDetailsRepository.findDistinctCustomerNames();
    }

    // Inner static DTO class
    public static class WorkDetailsDTO {
        private LocalDate date;
        private String inTime;
        private String outTime;
        private String engineerName;
        private String customerName;

        public WorkDetailsDTO(LocalDate date, String inTime, String outTime, String engineerName, String customerName) {
            this.date = date;
            this.inTime = inTime;
            this.outTime = outTime;
            this.engineerName = engineerName;
            this.customerName = customerName;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getInTime() {
            return inTime;
        }

        public String getOutTime() {
            return outTime;
        }

        public String getEngineerName() {
            return engineerName;
        }

        public String getCustomerName() {
            return customerName;
        }
    }
}
