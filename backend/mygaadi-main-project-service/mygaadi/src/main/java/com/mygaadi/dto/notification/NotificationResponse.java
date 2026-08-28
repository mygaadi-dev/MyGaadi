package com.mygaadi.dto.notification;

import com.mygaadi.model.entity.AppNotification;
import com.mygaadi.model.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType notificationType;
    private String title;
    private String body;
    private Long referenceId;
    private String referenceType;
    private boolean read;
    private Instant createdAt;

    public static NotificationResponse from(AppNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
