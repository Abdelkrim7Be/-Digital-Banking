package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.AccountOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
    // Find all operations for a given bank account ID
    List<AccountOperation> findByBankAccountId(String accountId);

    // Find all operations for a given bank account ID with pagination.
    Page<AccountOperation> findByBankAccountId(String accountId, Pageable pageable);
}