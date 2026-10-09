package com.ncpl.sales.cashflow.repository;

import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for InvoiceEntity - handles database operations
 * Supports fetching invoices from Tally sync and Excel uploads
 */
@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    
    /**
     * Find all invoices from a specific source (TALLY, EXCEL)
     */
    List<InvoiceEntity> findBySource(String source);
    
    /**
     * Find invoices by customer name
     */
    List<InvoiceEntity> findByCustomerNameContainingIgnoreCase(String customerName);
    
    /**
     * Find invoices by type (INFLOW/OUTFLOW)
     */
    List<InvoiceEntity> findByInvoiceType(InvoiceEntity.InvoiceType invoiceType);
    
    /**
     * Find invoices within a date range
     */
    List<InvoiceEntity> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find overdue invoices (due date before today)
     */
    @Query("SELECT i FROM InvoiceEntity i WHERE i.dueDate < CURRENT_DATE AND i.dueDate IS NOT NULL")
    List<InvoiceEntity> findOverdueInvoices();

    @Query("SELECT MIN(i.invoiceDate) FROM InvoiceEntity i WHERE i.source = :source")
    LocalDate findMinInvoiceDateBySource(@Param("source") String source);

    @Query("SELECT MAX(i.invoiceDate) FROM InvoiceEntity i WHERE i.source = :source")
    LocalDate findMaxInvoiceDateBySource(@Param("source") String source);
    
    /**
     * Find upcoming invoices (due date after today)
     */
    @Query("SELECT i FROM InvoiceEntity i WHERE i.dueDate >= CURRENT_DATE AND i.dueDate IS NOT NULL")
    List<InvoiceEntity> findUpcomingInvoices();
    
    /**
     * Find all invoices sorted by due date
     */
    List<InvoiceEntity> findAllByOrderByDueDateAsc();
    
    /**
     * Get count of invoices by source
     */
    Long countBySource(String source);

    @Modifying
    @Transactional
    void deleteBySource(String source);

    /**
     * Check existing invoice by source, number, and date
     */
    boolean existsBySourceAndInvoiceNumberAndInvoiceDate(String source, String invoiceNumber, java.time.LocalDate invoiceDate);
}
