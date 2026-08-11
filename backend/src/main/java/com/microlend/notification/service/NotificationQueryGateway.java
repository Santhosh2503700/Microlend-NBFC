package com.microlend.notification.service;

import com.microlend.notification.dto.NotificationView;

import java.util.List;


public interface NotificationQueryGateway {

    List<NotificationView> list(Long userId);

    long unreadCount(Long userId);

    void markRead(Long userId, Long notificationId);
}
