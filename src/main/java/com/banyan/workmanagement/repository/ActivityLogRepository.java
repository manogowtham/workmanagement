package com.banyan.workmanagement.repository;

import com.banyan.workmanagement.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<ActivityLog> findByUsernameContainingIgnoreCaseOrderByTimestampDesc(String username, Pageable pageable);

    Page<ActivityLog> findByActionContainingIgnoreCaseOrderByTimestampDesc(String action, Pageable pageable);

    Page<ActivityLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end,
            Pageable pageable);

    void deleteByTimestampBefore(LocalDateTime timestamp);
}
