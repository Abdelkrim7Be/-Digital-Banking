package com.bellagnech.dig_bank.dtos;

import com.bellagnech.dig_bank.enums.OperationType;
import lombok.Data;

import java.util.Date;

@Data
public class AccountOperationDTO {
    private Long id;
    private Date operationDate;
    private double amount;
    private String description;
    private OperationType type;
    private String bankAccountId;
    
    // Add user information
    private Long userId;
    private String username;
}