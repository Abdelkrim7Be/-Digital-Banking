package com.bellagnech.dig_bank.entities;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("CA")
@Data @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true) 
public class CurrentAccount extends BankAccount {
    private double overDraft;
}