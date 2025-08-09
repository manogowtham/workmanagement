package com.banyan.workmanagement.controller;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.banyan.workmanagement.dto.RolePermissionForm;
import com.banyan.workmanagement.service.RolePermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.banyan.workmanagement.util.ActivityLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Set;

@Controller
@RequestMapping("/manage-role-access")
public class RoleAccessController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private ActivityLogger activityLogger;

    /**
     * Displays the role access management page with list of roles and permissions.
     */
    @GetMapping
    public String showRoleAccessPage(@RequestParam(value = "role", required = false) String roleName, Model model) {
        // Log activity
        activityLogger.logActivity("VIEW", "Visited manage role access page");

        model.addAttribute("roles", rolePermissionService.getAllRoles());
        model.addAttribute("permissions", rolePermissionService.getAllPermissions());

        if (roleName != null && !roleName.trim().isEmpty()) {
            model.addAttribute("selectedRole", roleName);

            Set<String> assignedPermissions = rolePermissionService.getPermissionsByRole(roleName);
            if (assignedPermissions == null) {
                assignedPermissions = Collections.emptySet();
            }
            model.addAttribute("assignedPermissions", assignedPermissions);
        } else {
            model.addAttribute("assignedPermissions", Collections.emptySet());
        }

        return "manage-role-access";
    }

    /**
     * Updates the selected role with granted and revoked permissions using
     * RolePermissionForm.
     */
    @PostMapping("/update")
    public String updateRolePermissions(
            @ModelAttribute RolePermissionForm form,
            RedirectAttributes redirectAttributes) {

        // Log activity
        activityLogger.logActivity("UPDATE", "Updated permissions for role: " + form.getRoleName());

        try {
            rolePermissionService.updatePermissions(
                    form.getRoleName(),
                    form.getGrantPermissions(),
                    form.getRevokePermissions());
            redirectAttributes.addFlashAttribute("message",
                    "Permissions successfully updated for role: " + form.getRoleName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update permissions: " + e.getMessage());
        }

        return "redirect:/manage-role-access?role=" + form.getRoleName();
    }
}
