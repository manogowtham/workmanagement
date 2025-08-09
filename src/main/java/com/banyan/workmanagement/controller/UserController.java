package com.banyan.workmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.banyan.workmanagement.model.Role;
import com.banyan.workmanagement.model.User;
import com.banyan.workmanagement.model.Engineer;
import com.banyan.workmanagement.repository.RoleRepository;
import com.banyan.workmanagement.repository.UserRepository;
import com.banyan.workmanagement.repository.EngineerRepository;

import com.banyan.workmanagement.util.ActivityLogger;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EngineerRepository engineerRepository;

    @Autowired
    private ActivityLogger activityLogger;

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        // activityLogger.logActivity("VIEW", "Visited add user form");
        User user = new User();
        user.setRole(new Role()); // This is necessary for role.id to bind
        model.addAttribute("user", user);

        // Add list of engineers for dropdown
        model.addAttribute("engineers", engineerRepository.findAll());

        return "add-user";
    }

    @PostMapping("/add-user")
    public String addUser(@ModelAttribute User user) {
        if (user.getRole() == null || user.getRole().getName() == null) {
            throw new RuntimeException("Role is missing in the submitted form.");
        }

        String roleName = user.getRole().getName();

        Role roleFromDb = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRole(roleFromDb);

        // Fetch and set engineer if provided
        if (user.getEngineer() != null && user.getEngineer().getId() != null) {
            Engineer engineerFromDb = engineerRepository.findById(user.getEngineer().getId())
                    .orElseThrow(
                            () -> new RuntimeException("Engineer not found with id: " + user.getEngineer().getId()));
            user.setEngineer(engineerFromDb);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return "redirect:/add-user?success";
    }

    @GetMapping("/user-list")
    public String showUserList(Model model) {
        List<User> users = userRepository.findAll(); // use repository directly
        model.addAttribute("users", users);
        return "user-list";
    }

    @GetMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/user-list";
    }

    @GetMapping("/edit-user/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("engineers", engineerRepository.findAll());
        return "edit-user";
    }

    @PostMapping("/edit-user/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setUsername(user.getUsername());

        // Only update password if a new one is provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Update role if provided
        if (user.getRole() != null && user.getRole().getName() != null) {
            Role roleFromDb = roleRepository.findByName(user.getRole().getName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + user.getRole().getName()));
            existingUser.setRole(roleFromDb);
        }

        // Update engineer - handle both assignment and removal
        if (user.getEngineer() != null && user.getEngineer().getId() != null) {
            // User selected an engineer - assign it
            Engineer engineerFromDb = engineerRepository.findById(user.getEngineer().getId())
                    .orElseThrow(
                            () -> new RuntimeException("Engineer not found with id: " + user.getEngineer().getId()));
            existingUser.setEngineer(engineerFromDb);
        } else {
            // User selected "Select Engineer (Optional)" or no engineer - remove assignment
            existingUser.setEngineer(null);
        }

        userRepository.save(existingUser);
        return "redirect:/user-list";
    }
}
