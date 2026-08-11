package com.microlend.notification.repository;

import com.microlend.notification.entity.Notification;
import com.microlend.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUserIdOrderByCreatedDateDesc(Long recipientUserId);

    long countByRecipientUserIdAndStatus(Long recipientUserId, NotificationStatus status);
}
