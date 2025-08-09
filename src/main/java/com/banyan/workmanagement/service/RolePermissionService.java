package com.banyan.workmanagement.service;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.banyan.workmanagement.model.Permission;
import com.banyan.workmanagement.model.Role;
import com.banyan.workmanagement.model.RolePermission;
import com.banyan.workmanagement.repository.PermissionRepository;
import com.banyan.workmanagement.repository.RoleRepository;
import com.banyan.workmanagement.repository.RolePermissionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;

@Service
public class RolePermissionService {

    private static final Set<String> ALLOWED_PERMISSIONS = new HashSet<>(Arrays.asList(
            "CUSTOMER_DELETE", "CUSTOMER_EDIT", "CUSTOMER_PAGE", "CUSTOMER_SAVE",
            "DETAILS_DATE", "DETAILS_DELETE", "DETAILS_EDIT", "DETAILS_PAGE", "DETAILS_SAVE",
            "ENGINEER_DELETE", "ENGINEER_EDIT", "ENGINEER_PAGE", "ENGINEER_SAVE",
            "ENGINEER_ACCESS"));

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PermissionRepository permissionRepo;

    @Autowired
    private RolePermissionRepository rolePermissionRepo;

    // Get all roles without filtering
    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    // Get all permissions filtered to allowed permissions only
    public List<Permission> getAllPermissions() {
        return permissionRepo.findAll().stream()
                .filter(perm -> ALLOWED_PERMISSIONS.contains(perm.getName()))
                .collect(Collectors.toList());
    }

    // Get a specific role
    public Role getRoleByName(String roleName) {
        return roleRepo.findByName(roleName).orElse(null);
    }

    // Get permission names assigned to a role
    public Set<String> getPermissionsByRole(String roleName) {
        Optional<Role> roleOpt = roleRepo.findByName(roleName);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            return rolePermissionRepo.findByRole(role).stream()
                    .map(rp -> rp.getPermission().getName())
                    .filter(ALLOWED_PERMISSIONS::contains)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    // Grant and revoke permissions using RolePermission table with validation
    @Transactional
    public void updatePermissions(String roleName, List<String> grants, List<String> revokes) {
        // Filter grants and revokes to allowed permissions only
        List<String> filteredGrants = (grants == null) ? Collections.emptyList()
                : grants.stream().filter(ALLOWED_PERMISSIONS::contains).collect(Collectors.toList());
        List<String> filteredRevokes = (revokes == null) ? Collections.emptyList()
                : revokes.stream().filter(ALLOWED_PERMISSIONS::contains).collect(Collectors.toList());

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // Combine grant + revoke permissions to minimize DB calls
        Set<String> allNames = Stream.concat(
                filteredGrants.stream(),
                filteredRevokes.stream()).collect(Collectors.toSet());

        List<Permission> allPermissions = permissionRepo.findAllByNameIn(allNames);

        for (Permission permission : allPermissions) {
            String permName = permission.getName();

            if (filteredGrants.contains(permName)) {
                if (!rolePermissionRepo.existsByRoleAndPermission(role, permission)) {
                    rolePermissionRepo.save(new RolePermission(role, permission));
                }
            }

            if (filteredRevokes.contains(permName)) {
                rolePermissionRepo.deleteByRoleAndPermission(role, permission);
            }
        }
    }
}
