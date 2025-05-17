package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
}