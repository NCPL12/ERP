package com.ncpl.sales.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ncpl.sales.model.MonthlyReportStock;

@Repository
public interface MonthlyReportStockRepo extends JpaRepository<MonthlyReportStock, Long> {

	List<MonthlyReportStock> findByReportDate(LocalDate reportDate);

	@Query("SELECT DISTINCT m.reportDate FROM MonthlyReportStock m ORDER BY m.reportDate DESC")
	List<LocalDate> findDistinctReportDates();

	@Query("SELECT m.outstandingQty FROM MonthlyReportStock m WHERE m.itemMasterId = ?1 AND m.reportDate < ?2 ORDER BY m.reportDate DESC")
	List<BigDecimal> findPreviousOutstandingQtyByItemAndBeforeDate(String itemMasterId, LocalDate beforeDate);

	@Query(value = "SELECT m.item_master_id, " +
			"COALESCE(m.outstanding_qty, " +
			"  CASE WHEN COALESCE(s.cost_price, 0) > 0 THEN m.outstanding_value / s.cost_price ELSE 0 END" +
			") AS qty " +
			"FROM tbl_monthly_report_stock m " +
			"INNER JOIN (SELECT item_master_id, MAX(report_date) AS max_date FROM tbl_monthly_report_stock " +
			"  WHERE report_date < :beforeDate GROUP BY item_master_id) latest " +
			"ON m.item_master_id = latest.item_master_id AND m.report_date = latest.max_date " +
			"LEFT JOIN (SELECT item_master_id, " +
			"  COALESCE(MAX(CASE WHEN preferred = 'yes' THEN cost_price END), MIN(cost_price)) AS cost_price " +
			"  FROM tbl_supplier GROUP BY item_master_id) s " +
			"ON m.item_master_id = s.item_master_id", nativeQuery = true)
	List<Object[]> findLatestOpeningQtyBeforeDate(@Param("beforeDate") LocalDate beforeDate);

	@Modifying
	@Transactional
	@Query("DELETE FROM MonthlyReportStock m WHERE m.reportDate = :reportDate")
	void deleteByReportDate(@Param("reportDate") LocalDate reportDate);
}
