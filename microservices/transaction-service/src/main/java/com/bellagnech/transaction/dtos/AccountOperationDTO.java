package com.bellagnech.transaction.dtos;

import com.bellagnech.transaction.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountOperationDTO {
    private Long id;
    private Date operationDate;
    private double amount;
    private String description;
    private OperationType type;
    private String bankAccountId;
    private String performedBy;
    /** Customer display name (or username) resolved from account-service. */
    private String customerName;
}

