package com.bellagnech.dig_bank.entities;

import com.bellagnech.dig_bank.enums.OperationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class AccountOperation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Operation date is required")
    private Date operationDate;

    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Description is required")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Operation type is required")
    private OperationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    @NotNull(message = "Bank account is required")
    private BankAccount bankAccount;

    private String performedBy;

    @PrePersist
    protected void onCreate() {
        if (operationDate == null) {
            operationDate = new Date();
        }
        if (performedBy == null) {
            performedBy = "system";
        }
    }
}