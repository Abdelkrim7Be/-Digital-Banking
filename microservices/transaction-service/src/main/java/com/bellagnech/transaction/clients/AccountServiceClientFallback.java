package com.bellagnech.transaction.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AccountServiceClientFallback implements AccountServiceClient {
    @Override
    public AccountDTO getAccount(String id) {
        log.warn("Fallback: Account service unavailable for account ID: {}", id);
        return null;
    }
    
    @Override
    public Map<String, Object> getAccountBalance(String id) {
        log.warn("Fallback: Account service unavailable for balance check, account ID: {}", id);
        return Map.of("error", "Service unavailable");
    }
    
    @Override
    public void updateBalance(String id, Map<String, Double> balanceUpdate) {
        log.warn("Fallback: Account service unavailable for balance update, account ID: {}", id);
    }
}

