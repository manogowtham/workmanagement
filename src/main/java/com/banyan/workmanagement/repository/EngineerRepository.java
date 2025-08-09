package com.banyan.workmanagement.repository;

import com.banyan.workmanagement.model.Engineer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EngineerRepository extends JpaRepository<Engineer, Long> {
    List<Engineer> findAllByOrderByIdDesc();
}
