package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.model.WorkDetails;
import com.banyan.workmanagement.service.WorkDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.banyan.workmanagement.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Controller
public class ReportController {

    @Autowired
    private WorkDetailsService workDetailsService;

    @Autowired
    private ActivityLogger activityLogger;

    // Step 1: Preview the report based on work details ID
    @GetMapping("/report/preview/{id}")
    public String previewReport(@PathVariable Long id, Model model) {
        // Log activity
        activityLogger.logActivity("VIEW", "Viewed report preview for work detail ID: " + id);

        // Fetch WorkDetails from the service
        WorkDetails workDetails = workDetailsService.findById(id);

        if (workDetails != null) {
            // Step 2: Generate the PDF report
            byte[] reportFilePath = workDetailsService.generatePdfReport(id);

            // Step 3: Define the report file path (using a temporary directory for safety)
            String reportFileName = "report-preview-" + id + ".pdf";
            Path reportPath = Paths.get("src/main/resources/static/reports/" + reportFileName);

            try {
                // Write the byte array to the file
                Files.write(reportPath, reportFilePath);

                // Step 4: Add the file name and path to the model for redirection
                model.addAttribute("reportFileName", reportFileName);
                model.addAttribute("reportPath", "/reports/" + reportFileName);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Error generating the report.");
                return "error"; // Return an error page if something goes wrong
            }
        } else {
            model.addAttribute("error", "Work details not found.");
            return "error"; // Return error page if work details are not found
        }

        // Step 5: Redirect to the preview page where the file can be viewed or
        // downloaded
        return "preview"; // Render preview page that shows the report
    }

    // Step 6: Download the generated report
    @GetMapping("/report/download/{id}")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {
        try {
            // Dynamically use the file name based on the ID from the preview step
            String reportFileName = "report-preview-" + id + ".pdf";

            // Define the file path for downloading the report
            Path reportPath = Paths.get("src/main/resources/static/reports/" + reportFileName);

            if (!Files.exists(reportPath)) {
                return ResponseEntity.notFound().build(); // Return 404 if the file doesn't exist
            }

            Resource resource = new UrlResource(reportPath.toUri());

            // Return the file as a downloadable resource
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportFileName + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
