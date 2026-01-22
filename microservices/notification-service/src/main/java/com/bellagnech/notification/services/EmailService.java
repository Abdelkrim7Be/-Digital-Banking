package com.bellagnech.notification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@digitalbank.com}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Async
    public void sendEmail(String to, String subject, String message) {
        if (!emailEnabled) {
            log.warn("Email sending is disabled. Skipping email to: {}", to);
            return;
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.warn("Email sending is disabled. Skipping HTML email to: {}", to);
            return;
        }

        try {
            org.springframework.mail.javamail.MimeMessageHelper helper = 
                new org.springframework.mail.javamail.MimeMessageHelper(
                    mailSender.createMimeMessage(), true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(helper.getMimeMessage());
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }
}

