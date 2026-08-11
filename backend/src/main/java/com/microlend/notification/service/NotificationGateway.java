package com.microlend.notification.service;

import com.microlend.notification.enums.NotificationCategory;


public interface NotificationGateway {

    void notifyUser(Long recipientUserId, String recipientRole, String message,
                    NotificationCategory category, String relatedEntityType, Long relatedEntityId);
}
