package com.bellagnech.dig_bank.dtos;

import lombok.Data;

@Data
public class SavingBankAccountDTO extends BankAccountDTO {
    private double interestRate;
}
