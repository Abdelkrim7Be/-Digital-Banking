package com.bellagnech.transaction.repositories;

import com.bellagnech.transaction.entities.AccountOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
    List<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String bankAccountId);
    Page<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String bankAccountId, Pageable pageable);
    Page<AccountOperation> findAllByOrderByOperationDateDesc(Pageable pageable);
}

