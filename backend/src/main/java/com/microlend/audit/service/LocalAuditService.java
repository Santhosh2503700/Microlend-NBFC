package com.microlend.audit.service;

import com.microlend.audit.entity.AuditLog;
import com.microlend.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class LocalAuditService implements AuditGateway, AuditQueryGateway {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String action, String module, String details) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .module(module)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();
            auditLogRepository.save(entry);
            log.info("AUDIT module={} action={} userId={}", module, action, userId);
        } catch (Exception e) {
            // Never let an audit write break the triggering flow.
            log.error("Failed to write audit entry module={} action={}: {}", module, action, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEntry> query(Long userId, String module, String action,
                                  LocalDateTime from, LocalDateTime to, int limit) {
        int capped = Math.min(Math.max(limit, 1), 500);
        return auditLogRepository.search(userId, module, action, from, to, PageRequest.of(0, capped))
                .stream()
                .map(a -> new AuditEntry(a.getAuditId(), a.getUserId(), a.getAction(),
                        a.getModule(), a.getDetails(), a.getTimestamp()))
                .toList();
    }
}
