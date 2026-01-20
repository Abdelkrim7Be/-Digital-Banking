package com.bellagnech.dig_bank.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Account type is required")
    private String accountType; // "CURRENT" or "SAVING"
    
    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    private double initialBalance;
    
    // For Current Account
    @DecimalMin(value = "0.0", message = "Overdraft cannot be negative")
    private double overdraft;
    
    // For Saving Account
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    private double interestRate;
    
    private String description;
}
