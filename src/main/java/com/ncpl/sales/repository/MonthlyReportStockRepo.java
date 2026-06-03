package com.ncpl.sales.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.MonthlyReportStock;

@Repository
public interface MonthlyReportStockRepo extends JpaRepository<MonthlyReportStock, Long> {

	List<MonthlyReportStock> findByReportDate(LocalDate reportDate);

	@Query("SELECT DISTINCT m.reportDate FROM MonthlyReportStock m ORDER BY m.reportDate DESC")
	List<LocalDate> findDistinctReportDates();

	@Query("SELECT m.outstandingQty FROM MonthlyReportStock m WHERE m.itemMasterId = ?1 AND m.reportDate < ?2 ORDER BY m.reportDate DESC")
	List<BigDecimal> findPreviousOutstandingQtyByItemAndBeforeDate(String itemMasterId, LocalDate beforeDate);
}
