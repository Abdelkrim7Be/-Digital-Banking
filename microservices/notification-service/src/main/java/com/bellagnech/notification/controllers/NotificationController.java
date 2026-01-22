package com.bellagnech.notification.controllers;

import com.bellagnech.notification.dtos.NotificationRequest;
import com.bellagnech.notification.entities.Notification;
import com.bellagnech.notification.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Notification> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Notification request received for: {}", request.getRecipient());
        Notification notification = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PostMapping("/account-created")
    public ResponseEntity<Void> sendAccountCreatedNotification(
            @RequestBody Map<String, String> payload) {
        log.info("Account created notification requested");
        notificationService.sendAccountCreatedNotification(
            payload.get("email"),
            payload.get("customerName"),
            payload.get("accountId")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transaction")
    public ResponseEntity<Void> sendTransactionNotification(
            @RequestBody Map<String, Object> payload) {
        log.info("Transaction notification requested");
        notificationService.sendTransactionNotification(
            (String) payload.get("email"),
            (String) payload.get("transactionType"),
            ((Number) payload.get("amount")).doubleValue(),
            (String) payload.get("accountId")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/low-balance")
    public ResponseEntity<Void> sendLowBalanceAlert(
            @RequestBody Map<String, Object> payload) {
        log.info("Low balance alert requested");
        notificationService.sendLowBalanceAlert(
            (String) payload.get("email"),
            (String) payload.get("accountId"),
            ((Number) payload.get("balance")).doubleValue()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<Notification>> getNotificationsByRecipient(@PathVariable String recipient) {
        log.info("Retrieving notifications for recipient: {}", recipient);
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(recipient));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(
            @PathVariable Notification.NotificationStatus status) {
        log.info("Retrieving notifications with status: {}", status);
        return ResponseEntity.ok(notificationService.getNotificationsByStatus(status));
    }
}

