package com.bellagnech.dig_bank.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SavingBankAccountDTO extends BankAccountDTO {
    private double interestRate;
}
