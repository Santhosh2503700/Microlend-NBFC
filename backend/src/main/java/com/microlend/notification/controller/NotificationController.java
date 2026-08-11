package com.microlend.notification.controller;

import com.microlend.notification.dto.NotificationView;
import com.microlend.notification.service.NotificationQueryGateway;
import com.microlend.identity.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryGateway queryGateway;

    @GetMapping
    public List<NotificationView> myNotifications() {
        return queryGateway.list(SecurityUtil.currentUserId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("unread", queryGateway.unreadCount(SecurityUtil.currentUserId()));
    }

    @PutMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id) {
        queryGateway.markRead(SecurityUtil.currentUserId(), id);
        return Map.of("id", id, "status", "READ");
    }
}
