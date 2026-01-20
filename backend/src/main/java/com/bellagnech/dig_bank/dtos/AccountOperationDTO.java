package com.bellagnech.dig_bank.dtos;

import com.bellagnech.dig_bank.enums.OperationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class AccountOperationDTO {
    private Long id;

    @NotNull(message = "Operation date is required")
    private Date operationDate;

    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Operation type is required")
    private OperationType type;

    @NotBlank(message = "Bank account ID is required")
    private String bankAccountId;

    private String performedBy;
}