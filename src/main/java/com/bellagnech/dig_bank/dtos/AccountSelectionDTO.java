package com.bellagnech.dig_bank.dtos;

import com.bellagnech.dig_bank.enums.AccountStatus;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO for account selection in dropdowns
 * Contains minimal information needed for account selection UI
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountSelectionDTO {
    private String accountId;
    private String customerUsername;
    private String customerName;
    private String accountType;
    private double balance;
    private AccountStatus status;
    
    // Display format: "username - Customer Name (Type: $balance)"
    public String getDisplayText() {
        return String.format("%s - %s (%s: $%.2f)", 
            customerUsername, customerName, accountType, balance);
    }
}
