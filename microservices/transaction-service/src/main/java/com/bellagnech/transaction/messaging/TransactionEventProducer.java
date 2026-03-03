package com.bellagnech.transaction.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionEventProducer {

    private static final String TOPIC = "transaction-events";

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    public TransactionEventProducer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void sendTransactionEvent(String key, TransactionEvent event) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            log.debug("Kafka disabled or not available, skipping event for key={}", key);
            return;
        }
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            log.info("Publishing transaction event to Kafka. key={}, payload={}", key, payloadJson);
            kafkaTemplate.send(TOPIC, key, payloadJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TransactionEvent for Kafka: {}", e.getMessage(), e);
        }
    }
}

