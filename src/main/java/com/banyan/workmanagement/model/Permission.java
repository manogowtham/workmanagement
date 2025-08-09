package com.banyan.workmanagement.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // equals and hashCode based on name
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Permission))
            return false;
        Permission that = (Permission) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Permission{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
