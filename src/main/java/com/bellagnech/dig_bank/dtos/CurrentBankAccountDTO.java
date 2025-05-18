package com.bellagnech.dig_bank.dtos;

import lombok.Data;

@Data
public class CurrentBankAccountDTO extends BankAccountDTO {
    private double overDraft;
}
