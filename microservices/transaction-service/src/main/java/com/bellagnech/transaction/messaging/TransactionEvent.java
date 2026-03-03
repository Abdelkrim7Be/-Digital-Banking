package com.bellagnech.transaction.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {
    private String type;
    private String accountId;
    private double amount;
    private String description;
    private String recipientEmail;
    private String customerName;
}

