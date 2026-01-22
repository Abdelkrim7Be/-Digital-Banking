package com.bellagnech.account.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    @NotNull(message = "Account type is required")
    private String accountType; // CURRENT or SAVING

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @Positive(message = "Initial balance must be positive")
    private double initialBalance;

    private Double overdraft; // For current accounts
    private Double interestRate; // For saving accounts
}

