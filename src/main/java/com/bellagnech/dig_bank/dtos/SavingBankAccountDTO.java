package com.bellagnech.dig_bank.dtos;

import lombok.Data;
import com.bellagnech.dig_bank.enums.AccountStatus;

import java.util.Date;


@Data
public class SavingBankAccountDTO extends BankAccountDTO {
    private String id;
    private double balance;
    private Date createDate;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double interestRate;
}
