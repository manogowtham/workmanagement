package com.banyan.workmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banyan.workmanagement.model.Role;
import com.banyan.workmanagement.model.Permission;
import com.banyan.workmanagement.model.RolePermission;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    boolean existsByRoleAndPermission(Role role, Permission permission);

    void deleteByRoleAndPermission(Role role, Permission permission);

    List<RolePermission> findByRole(Role role);
}
