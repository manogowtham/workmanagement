package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.model.Customer;
import com.banyan.workmanagement.repository.CustomerRepository;
import com.banyan.workmanagement.service.CustomerService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import com.banyan.workmanagement.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

import org.springframework.security.web.csrf.CsrfToken;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    private final CustomerRepository customerRepository;

    @Autowired
    private ActivityLogger activityLogger;

    public CustomerController(CustomerRepository customerRepository, CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER_PAGE') or hasAnyAuthority('CUSTOMER_SAVE')")
    @GetMapping("/customer-form")
    public String showCustomerForm(Model model, HttpServletRequest request) {
        // Log activity
        activityLogger.logActivity("VIEW", "Visited customer form");

        // Add CSRF token
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("_csrf", token);

        // Fetch all customers
        List<Customer> allCustomers = customerService.getAllCustomers();

        // Removed unique regular + non-regular logic as per request
        model.addAttribute("customers", allCustomers);

        // Add empty customer object to avoid null in template
        model.addAttribute("customer", new Customer());

        return "customer";
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER_SAVE')")
    @PostMapping("/saveCustomer")
    public String saveCustomer(@ModelAttribute Customer customer) {
        activityLogger.logActivity("SAVE", "Saved customer: " + customer.getCompanyName());
        customerService.save(customer);
        return "redirect:/customer-list"; // Redirect to the customer list after saving
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER_SAVE')")
    @GetMapping("/customer-list")
    public String showCustomerList(Model model, Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        activityLogger.logActivity("VIEW", "Viewed customer list");

        org.springframework.data.domain.Page<com.banyan.workmanagement.model.Customer> customerPage;

        if (search != null && !search.trim().isEmpty()) {
            customerPage = customerService.searchCustomersPaged(search.trim().toLowerCase(),
                    org.springframework.data.domain.PageRequest.of(page, size));
        } else {
            customerPage = customerService
                    .getCustomersPaged(org.springframework.data.domain.PageRequest.of(page, size));
        }

        java.util.List<com.banyan.workmanagement.model.Customer> customers = customerPage.getContent();

        // Removed call to markRegularCustomers as per request

        model.addAttribute("customers", customers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalItems", customerPage.getTotalElements());
        model.addAttribute("search", search);

        // Check if the current user is SUPERADMIN or ADMIN

        return "customer-list"; // Return to the customer list page
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER_DELETE')")
    @GetMapping("/deleteCustomer/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        activityLogger.logActivity("DELETE", "Deleted customer with ID: " + id);
        customerService.deleteCustomer(id);
        return "redirect:/customer-list"; // Redirect to the customer list after deleting
    }

    @PreAuthorize("hasAnyAuthority('CUSTOMER_EDIT')")
    @GetMapping("/editCustomer/{id}")
    public String editCustomer(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        // Log activity
        activityLogger.logActivity("EDIT", "Editing customer with ID: " + id);

        // Add CSRF token to model
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("_csrf", token);

        // Fetch the customer by id
        Customer customer = customerService.findById(id);
        model.addAttribute("customer", customer);

        // Fetch all customers for dropdown or other UI elements
        List<Customer> allCustomers = customerService.getAllCustomers();

        // Removed unique regular/non-regular logic as per request
        model.addAttribute("customers", allCustomers);

        // Return the edit page template
        return "customer-edit";
    }

    @GetMapping("/export-customers")
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String search) throws IOException {
        activityLogger.logActivity("DOWNLOAD",
                "Exported customers to Excel");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=customers.xlsx");

        List<Customer> customerList;

        if (search != null && !search.trim().isEmpty()) {
            customerList = customerService.searchCustomers(search.trim().toLowerCase());
        } else {
            customerList = customerService.getAllCustomers();
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Customers");
        // Title Row (Merged)
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Customer List");

        // Create and apply title style
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        titleFont.setFontName("Bookman Old Style");
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);

        // Merge title across all header columns (0 to 11 for 12 columns)
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));

        // Create font for header - Bookman Old Style
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setFontName("Bookman Old Style");
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        // Header cell style
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Create font for data cells - Bookman Old Style
        XSSFFont dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 11);
        dataFont.setFontName("Bookman Old Style");

        // Data cell style (default, no wrap)
        XSSFCellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setFont(dataFont);
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Data cell style with wrap text (for Address, GST Number, Email)
        XSSFCellStyle dataCellWrapStyle = workbook.createCellStyle();
        dataCellWrapStyle.cloneStyleFrom(dataCellStyle);
        dataCellWrapStyle.setWrapText(true);

        // Create header row
        Row header = sheet.createRow(1);
        String[] headers = {
                "Vendor Type", "Company Name", "Contact Person", "Mobile Number", "Address",
                "Area", "State", "City", "Pincode", "GST Number", "Company Contact", "Email"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Populate data rows
        int rowNum = 2;
        for (Customer c : customerList) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(c.getVendorType());
            row.getCell(0).setCellStyle(dataCellStyle);

            row.createCell(1).setCellValue(c.getCompanyName());
            row.getCell(1).setCellStyle(dataCellStyle);

            row.createCell(2).setCellValue(c.getContactPerson());
            row.getCell(2).setCellStyle(dataCellStyle);

            // Mobile Number - numeric if possible
            Cell mobileCell = row.createCell(3);
            if (c.getMobileNumber() != null && c.getMobileNumber().matches("\\d+")) {
                mobileCell.setCellValue(Long.parseLong(c.getMobileNumber()));
            } else {
                mobileCell.setCellValue(c.getMobileNumber());
            }
            mobileCell.setCellStyle(dataCellStyle);

            // Address - wrap text
            Cell addressCell = row.createCell(4);
            addressCell.setCellValue(c.getAddress());
            addressCell.setCellStyle(dataCellWrapStyle);

            row.createCell(5).setCellValue(c.getArea());
            row.getCell(5).setCellStyle(dataCellStyle);

            row.createCell(6).setCellValue(c.getState());
            row.getCell(6).setCellStyle(dataCellStyle);

            row.createCell(7).setCellValue(c.getCity());
            row.getCell(7).setCellStyle(dataCellStyle);

            // Pincode - numeric if possible
            Cell pinCell = row.createCell(8);
            if (c.getPincode() != null && c.getPincode().matches("\\d+")) {
                pinCell.setCellValue(Long.parseLong(c.getPincode()));
            } else {
                pinCell.setCellValue(c.getPincode());
            }
            pinCell.setCellStyle(dataCellStyle);

            // GST Number - wrap text (some GSTs can be long)
            Cell gstCell = row.createCell(9);
            gstCell.setCellValue(c.getGstNumber());
            gstCell.setCellStyle(dataCellWrapStyle);

            // Company Contact - numeric if possible
            Cell companyContactCell = row.createCell(10);
            if (c.getCompanyContact() != null && c.getCompanyContact().matches("\\d+")) {
                companyContactCell.setCellValue(Long.parseLong(c.getCompanyContact()));
            } else {
                companyContactCell.setCellValue(c.getCompanyContact());
            }
            companyContactCell.setCellStyle(dataCellStyle);

            // Email - wrap text
            Cell emailCell = row.createCell(11);
            emailCell.setCellValue(c.getEmail());
            emailCell.setCellStyle(dataCellWrapStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        // Don't close outputStream explicitly, container manages it

    }

}
