package com.bellagnech.dig_bank.dtos;

import com.bellagnech.dig_bank.enums.AccountStatus;
import lombok.Data;

import java.util.Date;

@Data
public abstract class BankAccountDTO {
    private String id;
    private double balance;
    private Date createDate;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private String type;
}
