package com.bellagnech.reporting.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class DomainEventListeners {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "account-events", groupId = "reporting-service")
    public void onAccountEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "?";
            String accountId = node.has("accountId") ? node.get("accountId").asText() : "?";
            log.info("Reporting: account event type={}, accountId={}", eventType, accountId);
        } catch (Exception e) {
            log.warn("Reporting: failed to parse account-events payload: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "account-balance-updates", groupId = "reporting-service")
    public void onBalanceUpdate(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String accountId = node.has("accountId") ? node.get("accountId").asText() : "?";
            Double newBalance = node.has("newBalance") ? node.get("newBalance").asDouble() : null;
            log.info("Reporting: balance update accountId={}, newBalance={}", accountId, newBalance);
        } catch (Exception e) {
            log.warn("Reporting: failed to parse account-balance-updates payload: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "transaction-events", groupId = "reporting-service")
    public void onTransactionEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String type = node.has("type") ? node.get("type").asText() : "?";
            String accountId = node.has("accountId") ? node.get("accountId").asText() : "?";
            log.info("Reporting: transaction event type={}, accountId={}", type, accountId);
        } catch (Exception e) {
            log.warn("Reporting: failed to parse transaction-events payload: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "customer-events", groupId = "reporting-service")
    public void onCustomerEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            Long customerId = node.has("customerId") ? node.get("customerId").asLong() : null;
            String email = node.has("email") ? node.get("email").asText() : "?";
            log.info("Reporting: customer event customerId={}, email={}", customerId, email);
        } catch (Exception e) {
            log.warn("Reporting: failed to parse customer-events payload: {}", e.getMessage());
        }
    }
}
