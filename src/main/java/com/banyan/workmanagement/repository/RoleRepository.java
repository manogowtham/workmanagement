package com.banyan.workmanagement.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.banyan.workmanagement.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name); // ✅ Auto-implemented by Spring Data JPA

}
