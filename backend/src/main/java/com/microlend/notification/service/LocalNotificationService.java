package com.microlend.notification.service;

import com.microlend.common.ApiException;
import com.microlend.notification.dto.NotificationView;
import com.microlend.notification.entity.Notification;
import com.microlend.notification.enums.NotificationCategory;
import com.microlend.notification.enums.NotificationStatus;
import com.microlend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class LocalNotificationService implements NotificationGateway, NotificationQueryGateway {

    private final NotificationRepository notificationRepository;

    @Override
    public void notifyUser(Long recipientUserId, String recipientRole, String message,
                           NotificationCategory category, String relatedEntityType, Long relatedEntityId) {
        Notification n = Notification.builder()
                .recipientUserId(recipientUserId)
                .recipientRole(recipientRole)
                .message(message)
                .category(category)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .status(NotificationStatus.UNREAD)
                .build();
        notificationRepository.save(n);
        log.info("NOTIFY user={} category={} entity={}:{}", recipientUserId, category,
                relatedEntityType, relatedEntityId);
    }

    @Override
    public List<NotificationView> list(Long userId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedDateDesc(userId).stream()
                .map(NotificationView::from).toList();
    }

    @Override
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getRecipientUserId().equals(userId)) {
            throw ApiException.forbidden("Notification does not belong to you");
        }
        n.setStatus(NotificationStatus.READ);
        notificationRepository.save(n);
    }
}
