package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.SalesOrderAudit;

@Repository
public interface SalesOrderAuditRepo extends JpaRepository<SalesOrderAudit, Long> {

	List<SalesOrderAudit> findBySalesOrderId(String salesOrderId);

	List<SalesOrderAudit> findByPerformedBy(String performedBy);

	List<SalesOrderAudit> findByAction(String action);

	@Query("SELECT a FROM SalesOrderAudit a WHERE a.salesOrderId = :salesOrderId AND a.action = :action")
	List<SalesOrderAudit> findBySalesOrderIdAndAction(@Param("salesOrderId") String salesOrderId, @Param("action") String action);

	@Query("SELECT a FROM SalesOrderAudit a WHERE a.actionPerformed BETWEEN :startDate AND :endDate")
	List<SalesOrderAudit> findByDateRange(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM SalesOrderAudit a WHERE a.performedBy = :performedBy AND a.actionPerformed BETWEEN :startDate AND :endDate")
	List<SalesOrderAudit> findByPerformedByAndDateRange(@Param("performedBy") String performedBy, 
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM SalesOrderAudit a WHERE a.salesOrderId = :salesOrderId AND a.actionPerformed BETWEEN :startDate AND :endDate")
	List<SalesOrderAudit> findBySalesOrderIdAndDateRange(@Param("salesOrderId") String salesOrderId, 
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM SalesOrderAudit a WHERE a.action = :action AND a.actionPerformed BETWEEN :startDate AND :endDate")
	List<SalesOrderAudit> findByActionAndDateRange(@Param("action") String action, 
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM SalesOrderAudit a WHERE " +
			"(:salesOrderId IS NULL OR a.salesOrderId = :salesOrderId) AND " +
			"(:performedBy IS NULL OR a.performedBy = :performedBy) AND " +
			"(:action IS NULL OR a.action = :action) AND " +
			"(:startDate IS NULL OR a.actionPerformed >= :startDate) AND " +
			"(:endDate IS NULL OR a.actionPerformed <= :endDate)")
	List<SalesOrderAudit> findByMultipleCriteria(@Param("salesOrderId") String salesOrderId,
			@Param("performedBy") String performedBy, @Param("action") String action,
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
