package com.microlend.audit.service;


public interface AuditGateway {

    void record(Long userId, String action, String module, String details);

    default void record(Long userId, String action, String module) {
        record(userId, action, module, null);
    }
}
