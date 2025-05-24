package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.AccountOperation;
import com.bellagnech.dig_bank.enums.OperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {

    //Find all operations for a given bank account ID
    List<AccountOperation> findByBankAccountId(String accountId);

    //Find all operations for a given bank account ID with pagination
    Page<AccountOperation> findByBankAccountId(String accountId, Pageable pageable);

    //Find operations by bank account ID and operation type
    List<AccountOperation> findByBankAccountIdAndType(String accountId, OperationType type);

    //Find operations by bank account ID within a date range
    @Query("SELECT ao FROM AccountOperation ao WHERE ao.bankAccount.id = :accountId AND ao.operationDate BETWEEN :startDate AND :endDate")
    List<AccountOperation> findByBankAccountIdAndDateRange(@Param("accountId") String accountId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);
    
    //Find operations by bank account ID and operation type with pagination
    Page<AccountOperation> findByBankAccountIdAndType(String accountId, OperationType type, Pageable pageable);

    //Find operations by performer
    List<AccountOperation> findByPerformedBy(String performedBy);
}