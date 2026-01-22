package com.bellagnech.reporting.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "transaction-service", fallback = TransactionServiceClientFallback.class)
public interface TransactionServiceClient {
    @GetMapping("/api/transactions/account/{accountId}")
    List<TransactionDTO> getAccountTransactions(@PathVariable String accountId);
    
    class TransactionDTO {
        public Long id;
        public java.util.Date operationDate;
        public Double amount;
        public String description;
        public String type;
        public String bankAccountId;
    }
}

