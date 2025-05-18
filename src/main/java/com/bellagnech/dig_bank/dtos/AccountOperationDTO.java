package com.bellagnech.dig_bank.dtos;

import lombok.Data;
import com.bellagnech.dig_bank.enums.OperationType;

import java.util.Date;


@Data
public class AccountOperationDTO {
    private Long id;
    private Date operationDate;
    private double amount;
    private OperationType type;
    private String description;
}