package com.bellagnech.dig_bank.dtos;

import java.sql.Date;

import com.bellagnech.dig_bank.enums.AccountStatus;

import lombok.Data;

@Data
public class BankAccountDTO {
    private String id;
    private String type;
    private double balance;
    private Date createDate;
    private AccountStatus status;
    private CustomerDTO customerDTO;
}
