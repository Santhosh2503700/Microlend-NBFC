package com.microlend.audit.repository;

import com.microlend.audit.entity.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByModuleOrderByTimestampDesc(String module);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);

    List<AuditLog> findTop200ByOrderByTimestampDesc();


    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:userId IS NULL OR a.userId = :userId)
              AND (:module IS NULL OR a.module = :module)
              AND (:action IS NULL OR UPPER(a.action) LIKE UPPER(CONCAT('%', :action, '%')))
              AND (:from IS NULL OR a.timestamp >= :from)
              AND (:to IS NULL OR a.timestamp <= :to)
            ORDER BY a.timestamp DESC
            """)
    List<AuditLog> search(@Param("userId") Long userId,
                          @Param("module") String module,
                          @Param("action") String action,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to,
                          Pageable pageable);
}
