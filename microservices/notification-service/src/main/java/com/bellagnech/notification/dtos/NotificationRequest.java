package com.bellagnech.notification.dtos;

import com.bellagnech.notification.entities.Notification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotBlank(message = "Recipient is required")
    @Email(message = "Invalid email format")
    private String recipient;

    @NotNull(message = "Notification type is required")
    private Notification.NotificationType type;

    @NotNull(message = "Channel is required")
    private Notification.NotificationChannel channel;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;
}

