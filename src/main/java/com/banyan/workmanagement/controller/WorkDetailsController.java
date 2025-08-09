package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.model.WorkDetails;
import com.banyan.workmanagement.model.Customer;
import com.banyan.workmanagement.model.Engineer;
import com.banyan.workmanagement.model.User;
import com.banyan.workmanagement.model.Permission;
import com.banyan.workmanagement.repository.WorkDetailsRepository;
import com.banyan.workmanagement.repository.EngineerRepository;
import com.banyan.workmanagement.repository.UserRepository;
import com.banyan.workmanagement.service.CustomerService;
import com.banyan.workmanagement.service.WorkDetailsService;

import com.banyan.workmanagement.dto.WorkTimeDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller

public class WorkDetailsController {

    @Autowired
    private WorkDetailsRepository workDetailsRepository;

    @Autowired
    private EngineerRepository engineerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private WorkDetailsService workDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.banyan.workmanagement.util.ActivityLogger activityLogger;

    // Display the form for adding new work details
    @PreAuthorize("hasAuthority('DETAILS_PAGE')")
    @GetMapping("/details")
    public String showWorkDetailsForm(Model model, Principal principal) {
        activityLogger.logActivity("VIEW", "Visited work details form");
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> permissions = user.getRole().getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<Engineer> engineers = engineerRepository.findAll();
        List<Customer> allCustomers = customerService.getAllCustomers();

        // Pass all customers to maintain all location data
        List<Customer> customers = allCustomers;

        // Date formatting
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // ⛳️ Determine date range based on permission
        if (permissions.contains("DETAILS_DATE")) {
            // Show full date range
            model.addAttribute("minDate", "1900-01-01");
            model.addAttribute("maxDate", "2099-12-31");
        } else {
            // Restrict to yesterday and today
            model.addAttribute("minDate", yesterday.format(formatter));
            model.addAttribute("maxDate", today.format(formatter));
        }

        // Create new WorkDetails object
        WorkDetails workDetails = new WorkDetails();

        // Check if user has a tagged engineer and set engineerName accordingly
        if (user.getEngineer() != null) {
            workDetails.setEngineerName(user.getEngineer().getName());
            // Pass only the tagged engineer in the list to hide others
            model.addAttribute("engineers", List.of(user.getEngineer()));
        } else {
            model.addAttribute("engineers", engineers);
        }

        model.addAttribute("workDetails", workDetails);
        model.addAttribute("customers", customers);
        model.addAttribute("permissions", permissions);

        return "details";
    }

    // Edit an existing work detail
    @PreAuthorize("hasAuthority('DETAILS_EDIT')")
    @GetMapping("/details/{id}")
    public String editWorkDetails(@PathVariable Long id, Model model, Principal principal) {
        activityLogger.logActivity("EDIT", "Editing work details with ID: " + id);
        WorkDetails workDetail = workDetailsRepository.findById(id).orElse(null);
        if (workDetail == null) {
            return "redirect:/details-list";
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Engineer> engineers = engineerRepository.findAll();

        // Check if user has a tagged engineer and restrict engineers list
        if (user.getEngineer() != null) {
            engineers = List.of(user.getEngineer());
        }

        List<String> cities = Arrays.asList("Chennai", "Coimbatore", "Madurai", "Salem", "Tirunelveli", "Erode");

        List<Customer> allCustomers = customerService.getAllCustomers();

        // Pass all customers to maintain all location data
        List<Customer> customers = allCustomers;

        model.addAttribute("engineers", engineers);
        model.addAttribute("cities", cities);
        model.addAttribute("customers", customers);
        model.addAttribute("workDetails", workDetail);

        return "details";
    }

    // Save or update work details
    @PreAuthorize("hasAnyAuthority('DETAILS_SAVE')")
    @PostMapping("/save-details")
    public String saveWorkDetails(@ModelAttribute("workDetails") WorkDetails workDetails) {
        activityLogger.logActivity("SAVE", "Saved work details with ID: " + workDetails.getId());
        System.out.println("Saving workDetails with ID: " + workDetails.getId());
        workDetailsRepository.save(workDetails);
        return "redirect:/details-list";
    }

    // List all work details
    @PreAuthorize("hasAuthority('DETAILS_SAVE')")
    @GetMapping("/details-list")
    public String showDetailsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String engineer,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model,
            Principal principal) {
        activityLogger.logActivity("FILTER_SEARCH",
                "Filtered work details list with filters: engineer=" + engineer + ", customer=" + customer + ", status="
                        + status + ", startDate=" + startDate + ", endDate=" + endDate);

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> permissions = user.getRole().getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<Engineer> engineers = engineerRepository.findAll();

        // Check if logged-in user has a tagged engineer
        if (user.getEngineer() != null) {
            // Override engineer filter with the tagged engineer's name
            engineer = user.getEngineer().getName();
            // Restrict engineers list to show only the tagged engineer in dropdown
            engineers = List.of(user.getEngineer());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<WorkDetails> workDetailsPage = workDetailsService.findFilteredPage(engineer, customer, status, startDate,
                endDate, pageable);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<Long, String> formattedInTimes = new HashMap<>();
        Map<Long, String> formattedOutTimes = new HashMap<>();

        for (WorkDetails wd : workDetailsPage) {
            formattedInTimes.put(wd.getId(), wd.getInTime() != null ? wd.getInTime().format(timeFormatter) : "");
            formattedOutTimes.put(wd.getId(), wd.getOutTime() != null ? wd.getOutTime().format(timeFormatter) : "");
        }

        model.addAttribute("permissions", permissions);
        model.addAttribute("engineers", engineers);
        model.addAttribute("workDetailsPage", workDetailsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", workDetailsPage.getTotalPages());
        model.addAttribute("formattedInTimes", formattedInTimes);
        model.addAttribute("formattedOutTimes", formattedOutTimes);

        // Add filter params back to model for Thymeleaf to use in pagination links
        model.addAttribute("engineer", engineer);
        model.addAttribute("customer", customer);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "details-list";
    }

    // Delete work details
    @PreAuthorize("hasAuthority('DETAILS_DELETE')")
    @GetMapping("/delete/{id}")
    public String deleteWorkDetails(@PathVariable Long id) {
        activityLogger.logActivity("DELETE", "Deleted work details with ID: " + id);
        workDetailsRepository.deleteById(id);
        return "redirect:/details-list";
    }

    // View work details - for the "View" button

    @GetMapping("/view/{id}")
    public String viewWorkDetails(@PathVariable Long id, Model model) {
        WorkDetails workDetail = workDetailsRepository.findById(id).orElse(null);
        if (workDetail == null) {
            return "redirect:/details-list";
        }

        model.addAttribute("workDetail", workDetail);

        return "work-view"; // page to show details
    }

    // Generate PDF preview report for work details

    @GetMapping("/previewreport/{id}")
    public ResponseEntity<byte[]> generatePdfReport(@PathVariable Long id) {
        byte[] pdfBytes = workDetailsService.generatePdfReport(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("work_report_" + id + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // Preview work report in HTML

    @GetMapping("/preview.html")
    public String previewReport(@RequestParam("id") Long workDetailId, Model model) {
        WorkDetails workDetail = workDetailsService.getWorkDetailById(workDetailId);
        if (workDetail == null) {
            return "redirect:/details-list";
        }
        model.addAttribute("workDetail", workDetail);
        return "preview";
    }

    private String buildFilename(String engineer,
            String customer,
            String status,
            LocalDate start,
            LocalDate end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        List<String> parts = new ArrayList<>();

        if (engineer != null && !engineer.isBlank()) {
            parts.add(engineer.trim().replaceAll("\\s+", "_"));
        }
        if (customer != null && !customer.isBlank()) {
            parts.add(customer.trim().replaceAll("\\s+", "_"));
        }
        if (status != null && !status.isBlank()) {
            parts.add(status.trim().replaceAll("\\s+", "_"));
        }
        if (start != null && end != null) {
            parts.add(start.format(fmt) + "_to_" + end.format(fmt));
        }

        String base = parts.isEmpty() ? "work-details" : String.join("_", parts);
        return base + ".xlsx";
    }

    @GetMapping("/download-work-details")
    public ResponseEntity<byte[]> downloadWorkDetailsExcel(
            @RequestParam(required = false) String engineer,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) throws IOException {

        // Parse optional date parameters
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);

        // Fetch filtered and sorted work details list
        List<WorkDetails> filteredList = workDetailsService.findFiltered(engineer, customer, status, start, end);
        filteredList.sort(Comparator
                .comparing(WorkDetails::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(WorkDetails::getInTime, Comparator.nullsLast(Comparator.naturalOrder())));

        // Build Excel file and return as byte array response
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Work Details");
            CreationHelper createHelper = workbook.getCreationHelper();

            // Create reusable cell styles (no colours, all borders, center alignment,
            // wrap-text)
            Map<String, CellStyle> styles = createStyles(workbook, createHelper);

            // Write title row (row 0) with merged cells
            String filename = buildFilename(engineer, customer, status, start, end);
            createTitleRow(sheet, filename, styles.get("titleStyle"));

            // Write header row (row 1)
            createHeaderRow(sheet, styles.get("headerStyle"));

            // Write data rows starting at row 2
            writeDataRows(sheet, filteredList, styles, createHelper);

            // Auto-size all columns
            for (int i = 0; i < COLUMNS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);
            byte[] excelBytes = bos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        }
    }

    /** Constants for column headers */
    private static final String[] COLUMNS = {
            "Date", "In Time", "Out Time", "Total Hours", "Type of Work",
            "Engineer Name", "Customer Name", "Location", "Type of Service",
            "Status", "Bill Amount", "Payment Mode", "Work Description", "Kilometer"
    };

    /**
     * Parses a date string in ISO_LOCAL_DATE format, returns null if input is null
     * or empty.
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            // Optional: log warning about invalid format
            return null;
        }
    }

    /**
     * Creates commonly used cell styles for the workbook.
     * - No background colours.
     * - All borders = THIN.
     * - Horizontal & vertical alignment = CENTER.
     * - wrapText = true.
     */
    private Map<String, CellStyle> createStyles(Workbook workbook, CreationHelper createHelper) {
        Map<String, CellStyle> styles = new HashMap<>();

        // 1) Base font: Bookman Old Style, default size
        Font baseFont = workbook.createFont();
        baseFont.setFontName("Bookman Old Style");

        // 2) Title font: Bookman Old Style, size 16, bold
        Font titleFont = workbook.createFont();
        titleFont.setFontName("Bookman Old Style");
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);

        // 3) Header font: Bookman Old Style, bold (default size)
        Font headerFont = workbook.createFont();
        headerFont.setFontName("Bookman Old Style");
        headerFont.setBold(true);

        // --- Title style (for row 0) ---
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setWrapText(true);
        titleStyle.setBorderTop(BorderStyle.THIN);
        titleStyle.setBorderBottom(BorderStyle.THIN);
        titleStyle.setBorderLeft(BorderStyle.THIN);
        titleStyle.setBorderRight(BorderStyle.THIN);
        styles.put("titleStyle", titleStyle);

        // --- Header style (for row 1) ---
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("headerStyle", headerStyle);

        // --- Date style (for date cells) ---
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(headerStyle); // inherit borders, alignment, wrap
        short dateFormat = createHelper.createDataFormat().getFormat("dd-MM-yyyy");
        dateStyle.setDataFormat(dateFormat);
        // Override font to baseFont (so date text is default size, not bold)
        dateStyle.setFont(baseFont);
        styles.put("dateStyle", dateStyle);

        // --- Default data cell style (all other cells) ---
        CellStyle defaultStyle = workbook.createCellStyle();
        defaultStyle.setFont(baseFont);
        defaultStyle.setAlignment(HorizontalAlignment.CENTER);
        defaultStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        defaultStyle.setWrapText(true);
        defaultStyle.setBorderTop(BorderStyle.THIN);
        defaultStyle.setBorderBottom(BorderStyle.THIN);
        defaultStyle.setBorderLeft(BorderStyle.THIN);
        defaultStyle.setBorderRight(BorderStyle.THIN);
        styles.put("default", defaultStyle);

        return styles;
    }

    /**
     * Creates the title row (row 0) with merged cells and applies titleStyle.
     */
    private void createTitleRow(Sheet sheet, String filename, CellStyle titleStyle) {
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(filename);
        titleCell.setCellStyle(titleStyle);

        // Merge across all columns (0 to COLUMNS.length-1)
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, COLUMNS.length - 1));
    }

    /**
     * Creates the header row (row 1) using COLUMNS[] and headerStyle.
     */
    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(1);
        for (int i = 0; i < COLUMNS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(COLUMNS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Writes data rows for each WorkDetails entry, starting at row 2.
     * Uses only "default" and "dateStyle" from styles map (no alt shading).
     */
    private void writeDataRows(Sheet sheet, List<WorkDetails> workDetailsList,
            Map<String, CellStyle> styles, CreationHelper createHelper) {

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        CellStyle defaultStyle = styles.get("default");
        CellStyle dateStyle = styles.get("dateStyle");

        int rowNum = 2;
        for (WorkDetails wd : workDetailsList) {
            Row row = sheet.createRow(rowNum);

            // Create all cells up front, apply default style
            for (int c = 0; c < COLUMNS.length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellStyle(defaultStyle);
            }

            // -- Column 0: Date --
            Cell dateCell = row.getCell(0);
            if (wd.getDate() != null) {
                dateCell.setCellValue(java.sql.Date.valueOf(wd.getDate()));
                dateCell.setCellStyle(dateStyle);
            } else {
                dateCell.setCellValue("");
                dateCell.setCellStyle(defaultStyle);
            }

            // -- Column 1: In Time (HH:mm) --
            Cell inTimeCell = row.getCell(1);
            inTimeCell.setCellValue(Optional.ofNullable(wd.getInTime())
                    .map(t -> t.format(timeFormatter))
                    .orElse(""));
            inTimeCell.setCellStyle(defaultStyle);

            // -- Column 2: Out Time (HH:mm) --
            Cell outTimeCell = row.getCell(2);
            outTimeCell.setCellValue(Optional.ofNullable(wd.getOutTime())
                    .map(t -> t.format(timeFormatter))
                    .orElse(""));
            outTimeCell.setCellStyle(defaultStyle);

            // -- Column 3: Total Hours (HH:mm) --
            Cell totalHoursCell = row.getCell(3);
            if (wd.getInTime() != null && wd.getOutTime() != null) {
                try {
                    Duration duration = Duration.between(wd.getInTime(), wd.getOutTime());
                    long hours = duration.toHours();
                    long minutes = duration.toMinutes() % 60;
                    totalHoursCell.setCellValue(String.format("%02d:%02d", hours, minutes));
                } catch (DateTimeException e) {
                    totalHoursCell.setCellValue("Invalid");
                }
            } else {
                totalHoursCell.setCellValue("00:00");
            }
            totalHoursCell.setCellStyle(defaultStyle);

            // -- Column 4: Type of Work --
            setCellValueSafe(row.getCell(4), wd.getTypeOfWork(), defaultStyle);

            // -- Column 5: Engineer Name --
            setCellValueSafe(row.getCell(5), wd.getEngineerName(), defaultStyle);

            // -- Column 6: Customer Name --
            setCellValueSafe(row.getCell(6), wd.getCustomerName(), defaultStyle);

            // -- Column 7: Location --
            setCellValueSafe(row.getCell(7), wd.getLocation(), defaultStyle);

            // -- Column 8: Type of Service --
            setCellValueSafe(row.getCell(8), wd.getTypeOfService(), defaultStyle);

            // -- Column 9: Status --
            setCellValueSafe(row.getCell(9), wd.getStatus(), defaultStyle);

            // -- Column 10: Bill Amount (numeric) --
            Cell billAmountCell = row.getCell(10);
            if (wd.getBillAmount() != null) {
                billAmountCell.setCellValue(wd.getBillAmount().doubleValue());
            } else {
                billAmountCell.setCellValue(0);
            }
            billAmountCell.setCellStyle(defaultStyle);

            // -- Column 11: Payment Mode / Receive Payment --
            setCellValueSafe(row.getCell(11), wd.getReceivePayment(), defaultStyle);

            // -- Column 12: Work Description --
            setCellValueSafe(row.getCell(12), wd.getWorkDescription(), defaultStyle);

            // -- Column 13: Kilometer (numeric) --
            Cell kmCell = row.getCell(13);
            if (wd.getKilometer() != null) {
                kmCell.setCellValue(wd.getKilometer());
            } else {
                kmCell.setCellValue(0);
            }
            kmCell.setCellStyle(defaultStyle);

            rowNum++;
        }
    }

    /**
     * Helper method to safely set a String value to a cell (with style).
     */
    private void setCellValueSafe(Cell cell, String value, CellStyle style) {
        if (cell == null)
            return;
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

}