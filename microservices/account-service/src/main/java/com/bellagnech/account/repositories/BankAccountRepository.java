package com.bellagnech.account.repositories;

import com.bellagnech.account.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    List<BankAccount> findByCustomerId(Long customerId);
    Optional<BankAccount> findByIdAndCustomerId(String id, Long customerId);
}

