package com.banyan.workmanagement.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "role_permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "role_id", "permission_id" })
})
public class RolePermission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    private Permission permission;

    public RolePermission() {
    }

    public RolePermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RolePermission))
            return false;
        RolePermission that = (RolePermission) o;
        return Objects.equals(role.getId(), that.role.getId()) &&
                Objects.equals(permission.getId(), that.permission.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(role.getId(), permission.getId());
    }
}
