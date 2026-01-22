package com.bellagnech.notification.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        if (status == null) {
            status = NotificationStatus.PENDING;
        }
    }

    public enum NotificationType {
        ACCOUNT_CREATED,
        TRANSACTION_SUCCESS,
        TRANSACTION_FAILED,
        BALANCE_LOW,
        PASSWORD_RESET,
        SECURITY_ALERT,
        MONTHLY_STATEMENT
    }

    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED
    }
}

