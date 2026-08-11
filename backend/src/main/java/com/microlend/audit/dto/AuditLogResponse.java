package com.microlend.audit.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long auditId,
        Long userId,
        String userName,
        String action,
        String module,
        String details,
        LocalDateTime timestamp
) {
}
