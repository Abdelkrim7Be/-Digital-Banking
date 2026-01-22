package com.bellagnech.notification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.sms.from:+1234567890}")
    private String fromNumber;

    @Async
    public void sendSms(String to, String message) {
        if (!smsEnabled) {
            log.warn("SMS sending is disabled. Skipping SMS to: {}", to);
            return;
        }

        try {
            // Placeholder for SMS implementation
            // In production, integrate with Twilio, AWS SNS, or similar service
            log.info("SMS sent to {}: {}", to, message);
            
            // Example Twilio integration (commented out):
            // Twilio.init(accountSid, authToken);
            // Message.creator(
            //     new PhoneNumber(to),
            //     new PhoneNumber(fromNumber),
            //     message
            // ).create();
            
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
}

