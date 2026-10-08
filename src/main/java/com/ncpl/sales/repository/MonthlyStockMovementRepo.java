package com.ncpl.sales.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.MonthlyStockMovement;

@Repository
public interface MonthlyStockMovementRepo extends JpaRepository<MonthlyStockMovement, Long> {
    List<MonthlyStockMovement> findByReportDate(LocalDate reportDate);
    boolean existsByReportDate(LocalDate reportDate);
}
