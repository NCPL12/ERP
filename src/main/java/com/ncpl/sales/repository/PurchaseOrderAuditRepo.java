package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.PurchaseOrderAudit;

// @D0017 Purchase Order audit log, mirrors SalesOrderAuditRepo (see README.md)
@Repository
public interface PurchaseOrderAuditRepo extends JpaRepository<PurchaseOrderAudit, Long> {

	List<PurchaseOrderAudit> findByPoNumber(String poNumber);

	List<PurchaseOrderAudit> findByPerformedBy(String performedBy);

	List<PurchaseOrderAudit> findByAction(String action);

	@Query("SELECT a FROM PurchaseOrderAudit a WHERE a.poNumber = :poNumber AND a.action = :action")
	List<PurchaseOrderAudit> findByPoNumberAndAction(@Param("poNumber") String poNumber, @Param("action") String action);

	@Query("SELECT a FROM PurchaseOrderAudit a WHERE a.actionPerformed BETWEEN :startDate AND :endDate")
	List<PurchaseOrderAudit> findByDateRange(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM PurchaseOrderAudit a WHERE a.performedBy = :performedBy AND a.actionPerformed BETWEEN :startDate AND :endDate")
	List<PurchaseOrderAudit> findByPerformedByAndDateRange(@Param("performedBy") String performedBy,
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM PurchaseOrderAudit a WHERE a.poNumber = :poNumber AND a.actionPerformed BETWEEN :startDate AND :endDate")
	List<PurchaseOrderAudit> findByPoNumberAndDateRange(@Param("poNumber") String poNumber,
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM PurchaseOrderAudit a WHERE a.action = :action AND a.actionPerformed BETWEEN :startDate AND :endDate")
	List<PurchaseOrderAudit> findByActionAndDateRange(@Param("action") String action,
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

	@Query("SELECT a FROM PurchaseOrderAudit a WHERE " +
			"(:poNumber IS NULL OR a.poNumber = :poNumber) AND " +
			"(:performedBy IS NULL OR a.performedBy = :performedBy) AND " +
			"(:action IS NULL OR a.action = :action) AND " +
			"(:startDate IS NULL OR a.actionPerformed >= :startDate) AND " +
			"(:endDate IS NULL OR a.actionPerformed <= :endDate)")
	List<PurchaseOrderAudit> findByMultipleCriteria(@Param("poNumber") String poNumber,
			@Param("performedBy") String performedBy, @Param("action") String action,
			@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
