package com.bellagnech.notification.repositories;

import com.bellagnech.notification.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
    List<Notification> findByStatus(Notification.NotificationStatus status);
    List<Notification> findByType(Notification.NotificationType type);
}

