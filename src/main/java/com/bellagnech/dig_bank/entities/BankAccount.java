package com.bellagnech.dig_bank.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.bellagnech.dig_bank.enums.AccountStatus;

import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", length = 4)
@Data @NoArgsConstructor @AllArgsConstructor
public abstract class BankAccount {
    @Id
    private String id;

    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private double balance;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Account status is required")
    private AccountStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @NotNull(message = "Customer is required")
    private Customer customer;

    @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AccountOperation> accountOperations;

    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    private String lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        createDate = new Date();
        if (createdBy == null) {
            createdBy = "system";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = new Date();
        if (lastModifiedBy == null) {
            lastModifiedBy = "system";
        }
    }
}