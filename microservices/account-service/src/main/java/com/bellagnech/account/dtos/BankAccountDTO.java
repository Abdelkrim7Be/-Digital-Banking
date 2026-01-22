package com.bellagnech.account.dtos;

import com.bellagnech.account.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDTO {
    private String id;
    private double balance;
    private Date createDate;
    private AccountStatus status;
    private Long customerId;
    private String type;
}

