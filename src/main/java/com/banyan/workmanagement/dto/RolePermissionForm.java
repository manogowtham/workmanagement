package com.banyan.workmanagement.dto;

import java.util.List;

public class RolePermissionForm {
    private String roleName;
    private List<String> grantPermissions;
    private List<String> revokePermissions;

    // Constructors
    public RolePermissionForm() {
    }

    public RolePermissionForm(String roleName, List<String> grantPermissions, List<String> revokePermissions) {
        this.roleName = roleName;
        this.grantPermissions = grantPermissions;
        this.revokePermissions = revokePermissions;
    }

    // Getters and Setters
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getGrantPermissions() {
        return grantPermissions;
    }

    public void setGrantPermissions(List<String> grantPermissions) {
        this.grantPermissions = grantPermissions;
    }

    public List<String> getRevokePermissions() {
        return revokePermissions;
    }

    public void setRevokePermissions(List<String> revokePermissions) {
        this.revokePermissions = revokePermissions;
    }

    @Override
    public String toString() {
        return "RolePermissionForm{" +
                "roleName='" + roleName + '\'' +
                ", grantPermissions=" + grantPermissions +
                ", revokePermissions=" + revokePermissions +
                '}';
    }
}
