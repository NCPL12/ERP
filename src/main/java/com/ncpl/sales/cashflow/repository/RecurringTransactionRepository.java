package com.ncpl.sales.cashflow.repository;

import com.ncpl.sales.cashflow.entity.RecurringTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransactionEntity, Long> {
    List<RecurringTransactionEntity> findByIsActiveOrderByNextDueDateAsc(Boolean isActive);
    List<RecurringTransactionEntity> findByCategory(String category);
    List<RecurringTransactionEntity> findByNextDueDateBetween(LocalDate startDate, LocalDate endDate);
}
