package com.microlend.audit.controller;

import com.microlend.audit.dto.AuditLogResponse;
import com.microlend.audit.service.AuditQueryGateway;
import com.microlend.audit.service.AuditQueryGateway.AuditEntry;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/audit-log")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AuditQueryGateway auditQueryGateway;
    private final UserRepository userRepository;

    @GetMapping
    public List<AuditLogResponse> recent(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "200") int limit) {

        String mod = normalize(module);
        String act = normalize(action);
        // Date range is inclusive of the whole day.
        LocalDateTime fromTs = from == null ? null : from.atStartOfDay();
        LocalDateTime toTs = to == null ? null : to.atTime(23, 59, 59);

        List<AuditEntry> rows = auditQueryGateway.query(userId, mod, act, fromTs, toTs, limit);
        Map<Long, String> nameCache = new HashMap<>();
        return rows.stream().map(a -> {
            String name = a.userId() == null ? "System"
                    : nameCache.computeIfAbsent(a.userId(), id ->
                        userRepository.findById(id).map(User::getName).orElse("User #" + id));
            return new AuditLogResponse(a.auditId(), a.userId(), name, a.action(),
                    a.module(), a.details(), a.timestamp());
        }).toList();
    }

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() || t.equalsIgnoreCase("ALL") ? null : t;
    }
}
