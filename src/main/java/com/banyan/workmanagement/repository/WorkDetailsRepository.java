// src/main/java/com/banyan/workmanagement/repository/WorkDetailsRepository.java

package com.banyan.workmanagement.repository;

import com.banyan.workmanagement.model.WorkDetails;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkDetailsRepository
                extends JpaRepository<WorkDetails, Long>,
                JpaSpecificationExecutor<WorkDetails> {
        // No additional methods needed here; we’ll use Specifications in the service.

        List<WorkDetails> findAllByOrderByIdDesc();

        @Query("SELECT w FROM WorkDetails w WHERE w.engineerName = :engineerName AND w.date >= :startOfDay AND w.date < :nextDay")
        List<WorkDetails> findByEngineerNameAndDateRange(@Param("engineerName") String engineerName,
                        @Param("startOfDay") LocalDate startOfDay,
                        @Param("nextDay") LocalDate nextDay);

        @Query("SELECT w FROM WorkDetails w WHERE w.engineerName = :engineerName AND w.customerName = :customerName AND w.date >= :startOfDay AND w.date <= :endDay")
        List<WorkDetails> findByEngineerAndCustomerAndDateRange(@Param("engineerName") String engineerName,
                        @Param("customerName") String customerName,
                        @Param("startOfDay") LocalDate startOfDay,
                        @Param("endDay") LocalDate endDay);

        List<WorkDetails> findByEngineerNameAndDate(String engineer, LocalDate date);

        @Query("SELECT DISTINCT w.engineerName FROM WorkDetails w WHERE w.engineerName IS NOT NULL")
        List<String> findDistinctEngineerNames();

        @Query("SELECT DISTINCT w.customerName FROM WorkDetails w WHERE w.customerName IS NOT NULL")
        List<String> findDistinctCustomerNames();

}
