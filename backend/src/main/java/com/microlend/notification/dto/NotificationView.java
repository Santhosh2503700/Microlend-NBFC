package com.microlend.notification.dto;

import com.microlend.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationView(
        Long notificationId,
        Long recipientUserId,
        String recipientRole,
        String message,
        String category,
        String relatedEntityType,
        Long relatedEntityId,
        String status,
        LocalDateTime createdDate
) {
    public static NotificationView from(Notification n) {
        return new NotificationView(n.getNotificationId(), n.getRecipientUserId(), n.getRecipientRole(),
                n.getMessage(), n.getCategory() == null ? null : n.getCategory().name(),
                n.getRelatedEntityType(), n.getRelatedEntityId(),
                n.getStatus() == null ? null : n.getStatus().name(), n.getCreatedDate());
    }
}
