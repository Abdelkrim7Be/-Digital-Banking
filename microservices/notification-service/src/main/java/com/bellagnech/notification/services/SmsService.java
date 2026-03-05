package com.bellagnech.notification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Stub SMS sender; enable and wire provider (e.g. Twilio) for production. */
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
            log.info("SMS sent to {}: {}", to, message);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
}

