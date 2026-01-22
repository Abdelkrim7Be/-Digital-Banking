package com.bellagnech.transaction.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
    name = "account-service", 
    fallback = AccountServiceClientFallback.class
)
public interface AccountServiceClient {
    
    @GetMapping("/api/accounts/{id}")
    AccountDTO getAccount(@PathVariable String id);
    
    @GetMapping("/api/accounts/{id}/balance")
    Map<String, Object> getAccountBalance(@PathVariable String id);
    
    @PatchMapping("/api/accounts/{id}/balance")
    void updateBalance(@PathVariable String id, @RequestBody Map<String, Double> balanceUpdate);
    
    class AccountDTO {
        public String id;
        public Double balance;
        public String status;
        public Long customerId;
        public String type;
    }
}

