package com.ncpl.sales.cashflow.repository;

import com.ncpl.sales.cashflow.entity.FinanceCashPlanEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface FinanceCashPlanEntryRepository extends JpaRepository<FinanceCashPlanEntryEntity, Long> {
    List<FinanceCashPlanEntryEntity> findByActiveTrueAndPlannedDateBetweenOrderByPlannedDateAsc(LocalDate from, LocalDate to);
    List<FinanceCashPlanEntryEntity> findByActiveTrueOrderByPlannedDateAsc();
}
