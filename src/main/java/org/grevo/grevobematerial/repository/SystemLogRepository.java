package org.grevo.grevobematerial.repository;

import org.grevo.grevobematerial.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    Page<SystemLog> findByLevelContainingIgnoreCaseOrActionContainingIgnoreCaseOrMessageContainingIgnoreCase(
            String level, String action, String message, Pageable pageable);
}