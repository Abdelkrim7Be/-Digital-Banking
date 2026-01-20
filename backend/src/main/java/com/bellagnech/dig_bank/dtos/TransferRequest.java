package com.bellagnech.dig_bank.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO for transfer requests between accounts
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    
    @NotBlank(message = "Source account ID is required")
    private String sourceAccountId;
    
    @NotBlank(message = "Destination account ID is required")
    private String destinationAccountId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    private String description;
}
