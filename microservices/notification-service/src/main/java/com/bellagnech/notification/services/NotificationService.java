package com.bellagnech.notification.services;

import com.bellagnech.notification.dtos.NotificationRequest;
import com.bellagnech.notification.entities.Notification;
import com.bellagnech.notification.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Async
    @Transactional
    public Notification sendNotification(NotificationRequest request) {
        log.info("Sending notification to {} via {}", request.getRecipient(), request.getChannel());

        Notification notification = new Notification();
        notification.setRecipient(request.getRecipient());
        notification.setType(request.getType());
        notification.setChannel(request.getChannel());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setStatus(Notification.NotificationStatus.PENDING);

        notification = notificationRepository.save(notification);

        try {
            switch (request.getChannel()) {
                case EMAIL:
                    emailService.sendEmail(
                        request.getRecipient(),
                        request.getSubject(),
                        request.getMessage()
                    );
                    break;
                case SMS:
                    smsService.sendSms(request.getRecipient(), request.getMessage());
                    break;
                default:
                    log.warn("Unsupported notification channel: {}", request.getChannel());
            }

            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(new Date());
            log.info("Notification sent successfully: {}", notification.getId());

        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
        }

        return notificationRepository.save(notification);
    }

    @Async
    public void sendAccountCreatedNotification(String email, String customerName, String accountId) {
        String subject = "Account Created Successfully";
        String message = String.format(
            "Dear %s,\n\n" +
            "Your account has been created successfully!\n" +
            "Account ID: %s\n\n" +
            "Thank you for choosing Digital Banking.\n\n" +
            "Best regards,\n" +
            "Digital Banking Team",
            customerName, accountId
        );

        NotificationRequest request = new NotificationRequest();
        request.setRecipient(email);
        request.setType(Notification.NotificationType.ACCOUNT_CREATED);
        request.setChannel(Notification.NotificationChannel.EMAIL);
        request.setSubject(subject);
        request.setMessage(message);

        sendNotification(request);
    }

    @Async
    public void sendTransactionNotification(String email, String transactionType, double amount, String accountId) {
        String subject = "Transaction " + transactionType;
        String message = String.format(
            "Dear Customer,\n\n" +
            "A %s transaction has been processed on your account.\n" +
            "Account ID: %s\n" +
            "Amount: %.2f\n\n" +
            "If you did not initiate this transaction, please contact us immediately.\n\n" +
            "Best regards,\n" +
            "Digital Banking Team",
            transactionType, accountId, amount
        );

        NotificationRequest request = new NotificationRequest();
        request.setRecipient(email);
        request.setType(Notification.NotificationType.TRANSACTION_SUCCESS);
        request.setChannel(Notification.NotificationChannel.EMAIL);
        request.setSubject(subject);
        request.setMessage(message);

        sendNotification(request);
    }

    @Async
    public void sendLowBalanceAlert(String email, String accountId, double balance) {
        String subject = "Low Balance Alert";
        String message = String.format(
            "Dear Customer,\n\n" +
            "Your account balance is low.\n" +
            "Account ID: %s\n" +
            "Current Balance: %.2f\n\n" +
            "Please consider depositing funds to avoid service charges.\n\n" +
            "Best regards,\n" +
            "Digital Banking Team",
            accountId, balance
        );

        NotificationRequest request = new NotificationRequest();
        request.setRecipient(email);
        request.setType(Notification.NotificationType.BALANCE_LOW);
        request.setChannel(Notification.NotificationChannel.EMAIL);
        request.setSubject(subject);
        request.setMessage(message);

        sendNotification(request);
    }

    public List<Notification> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    public List<Notification> getNotificationsByStatus(Notification.NotificationStatus status) {
        return notificationRepository.findByStatus(status);
    }
}

