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

    // Admin-specific queries for transaction management

    //Find operations by multiple account IDs (for customer transactions)
    Page<AccountOperation> findByBankAccountIdIn(List<String> accountIds, Pageable pageable);

    //Count operations by multiple account IDs
    long countByBankAccountIdIn(List<String> accountIds);

    //Count operations by type
    long countByType(OperationType type);

    //Count operations within date range
    long countByOperationDateBetween(Date startDate, Date endDate);

    //Find operations within date range
    List<AccountOperation> findByOperationDateBetween(Date startDate, Date endDate);

    //Sum all transaction amounts
    @Query("SELECT SUM(ao.amount) FROM AccountOperation ao")
    Double sumAllAmounts();

    //Complex filtering query for admin transaction search
    @Query("SELECT ao FROM AccountOperation ao WHERE " +
           "(:search IS NULL OR LOWER(ao.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(ao.bankAccount.id) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:type IS NULL OR ao.type = :type) AND " +
           "(:accountId IS NULL OR ao.bankAccount.id = :accountId) AND " +
           "(:startDate IS NULL OR ao.operationDate >= :startDate) AND " +
           "(:endDate IS NULL OR ao.operationDate <= :endDate) AND " +
           "(:minAmount IS NULL OR ao.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR ao.amount <= :maxAmount)")
    Page<AccountOperation> findWithFilters(@Param("search") String search,
                                         @Param("type") String type,
                                         @Param("accountId") String accountId,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("minAmount") Double minAmount,
                                         @Param("maxAmount") Double maxAmount,
                                         Pageable pageable);
}