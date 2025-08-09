package com.banyan.workmanagement.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banyan.workmanagement.model.Permission;
import com.banyan.workmanagement.model.Role;
import com.banyan.workmanagement.repository.PermissionRepository;
import com.banyan.workmanagement.repository.RoleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PermissionRepository permissionRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create permissions
        List<String> perms = List.of(
                "DETAILS_SAVE", "DETAILS_EDIT", "DETAILS_DELETE", "DETAILS_PAGE",
                "ENGINEER_PAGE", "ENGINEER_ACCESS", "CUSTOMER_PAGE", "CUSTOMER_SAVE",
                "SUPERADMIN");
        for (String permName : perms) {
            if (permissionRepo.findByName(permName).isEmpty()) {
                Permission perm = new Permission();
                perm.setName(permName);
                permissionRepo.save(perm);
                logger.info("Inserted permission: {}", permName);
            } else {
                logger.info("Permission already exists: {}", permName);
            }
        }

        // Create ADMIN role with all permissions
        if (roleRepo.findByName("ADMIN").isEmpty()) {
            Role admin = new Role();
            admin.setName("ADMIN");
            admin.setPermissions(new HashSet<>(permissionRepo.findAll()));
            roleRepo.save(admin);
            logger.info("Created ADMIN role with all permissions.");
        } else {
            logger.info("ADMIN role already exists.");
        }

        // Create USER role with only DETAILS_SAVE permission
        if (roleRepo.findByName("USER").isEmpty()) {
            Role user = new Role();
            user.setName("USER");
            Permission savePerm = permissionRepo.findByName("DETAILS_SAVE")
                    .orElseThrow(() -> new RuntimeException("DETAILS_SAVE permission not found"));
            user.setPermissions(Set.of(savePerm));
            roleRepo.save(user);
            logger.info("Created USER role with DETAILS_SAVE permission.");
        } else {
            logger.info("USER role already exists.");
        }

        // Create ENGINEER role with engineer-specific permissions
        if (roleRepo.findByName("ENGINEER").isEmpty()) {
            Role engineer = new Role();
            engineer.setName("ENGINEER");
            Permission engineerAccess = permissionRepo.findByName("ENGINEER_ACCESS")
                    .orElseThrow(() -> new RuntimeException("ENGINEER_ACCESS permission not found"));
            Permission detailsPage = permissionRepo.findByName("DETAILS_PAGE")
                    .orElseThrow(() -> new RuntimeException("DETAILS_PAGE permission not found"));
            Permission detailsSave = permissionRepo.findByName("DETAILS_SAVE")
                    .orElseThrow(() -> new RuntimeException("DETAILS_SAVE permission not found"));
            engineer.setPermissions(Set.of(engineerAccess, detailsPage, detailsSave));
            roleRepo.save(engineer);
            logger.info("Created ENGINEER role with engineer permissions.");
        } else {
            logger.info("ENGINEER role already exists.");
        }
    }
}
