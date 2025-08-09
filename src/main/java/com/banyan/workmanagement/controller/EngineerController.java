package com.banyan.workmanagement.controller;

import com.banyan.workmanagement.model.Engineer;
import com.banyan.workmanagement.repository.EngineerRepository;
import com.banyan.workmanagement.util.ActivityLogger;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EngineerController {

    private final EngineerRepository engineerRepository;

    @Autowired
    private ActivityLogger activityLogger;

    public EngineerController(EngineerRepository engineerRepository) {
        this.engineerRepository = engineerRepository;
    }

    @PreAuthorize("hasAnyAuthority('ENGINEER_PAGE')")
    @GetMapping("/engineer-form")
    public String showForm(Model model) {
        activityLogger.logActivity("VIEW", "Visited engineer form");
        model.addAttribute("engineer", new Engineer());
        return "engineer"; // This refers to the engineer.html template
    }

    @PreAuthorize("hasAnyAuthority('ENGINEER_SAVE')")
    @PostMapping("/saveEngineer")
    public String saveEngineer(@ModelAttribute Engineer engineer) {
        engineerRepository.save(engineer); // Save the engineer data into the database
        activityLogger.logActivity("SAVE", "Saved engineer with ID: " + engineer.getId());
        return "redirect:/engineer-list"; // Redirect to the list page
    }

    @PreAuthorize("hasAnyAuthority('ENGINEER_SAVE')")
    @GetMapping("/engineer-list")
    public String listEngineers(Model model) {
        activityLogger.logActivity("FILTER_SEARCH", "Viewed engineer list");
        model.addAttribute("engineers", engineerRepository.findAllByOrderByIdDesc());
        return "engineer-list";
    }

    @PreAuthorize("hasAnyAuthority('ENGINEER_DELETE')")
    @GetMapping("/deleteEngineer/{id}")
    public String deleteEngineer(@PathVariable Long id) {
        activityLogger.logActivity("DELETE", "Deleted engineer with ID: " + id);
        engineerRepository.deleteById(id); // Delete the engineer by id
        return "redirect:/engineer-list"; // Redirect back to the list page
    }

    @PreAuthorize("hasAnyAuthority('ENGINEER_EDIT')")
    @GetMapping("/edit-engineer/{id}")
    public String editEngineer(@PathVariable Long id, Model model) {
        activityLogger.logActivity("EDIT", "Editing engineer with ID: " + id);
        Engineer engineer = engineerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid engineer ID: " + id));
        model.addAttribute("engineer", engineer);
        return "engineer"; // Reuse the same form
    }

    @PreAuthorize("hasAnyAuthority('ENGINEER_SAVE')")
    @PostMapping("/update-engineer")
    public String updateEngineer(@ModelAttribute Engineer engineer) {
        engineerRepository.save(engineer); // save() will update if ID is present
        activityLogger.logActivity("EDIT", "Updated engineer with ID: " + engineer.getId());
        return "redirect:/engineer-list";
    }

    @GetMapping("/api/engineers")
    @ResponseBody
    public List<String> getEngineers() {
        return engineerRepository.findAll()
                .stream()
                .map(Engineer::getName) // Assuming 'name' is the field for engineer name
                .collect(Collectors.toList());
    }
}
