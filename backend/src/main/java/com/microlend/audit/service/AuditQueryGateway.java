package com.microlend.audit.service;

import java.time.LocalDateTime;
import java.util.List;


public interface AuditQueryGateway {

    record AuditEntry(Long auditId, Long userId, String action, String module,
                      String details, LocalDateTime timestamp) {
    }

    List<AuditEntry> query(Long userId, String module, String action,
                           LocalDateTime from, LocalDateTime to, int limit);
}
