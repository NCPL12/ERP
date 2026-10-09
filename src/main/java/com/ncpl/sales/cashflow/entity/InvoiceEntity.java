package com.ncpl.sales.cashflow.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for Invoice - persists to MySQL database
 * Synced from Tally 6.2 or uploaded Excel files
 */
@Entity
@Table(name = "cashflow_invoices", indexes = {
    @Index(name = "idx_customer_name", columnList = "customer_name"),
    @Index(name = "idx_invoice_type", columnList = "invoice_type"),
    @Index(name = "idx_invoice_date", columnList = "invoice_date"),
    @Index(name = "idx_due_date", columnList = "due_date"),
    @Index(name = "idx_invoice_number", columnList = "invoice_number"),
    @Index(name = "idx_source", columnList = "source")
})
public class InvoiceEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_name", length = 500, nullable = false)
    private String customerName;
    
    @Column(name = "invoice_number", length = 200)
    private String invoiceNumber;
    
    @Column(name = "invoice_date")
    private LocalDate invoiceDate;
    
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "expected_cash_date")
    private LocalDate expectedCashDate;

    @Column(name = "cashflow_remarks", length = 1000)
    private String cashflowRemarks;
    
    @Column(name = "invoice_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal invoiceValue;
    
    @Column(name = "invoice_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceType invoiceType;
    
    @Column(name = "source", length = 50)
    private String source; // TALLY, EXCEL, etc.
    
    @Column(name = "reconciled", nullable = false, columnDefinition = "boolean default false")
    private boolean reconciled = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum InvoiceType {
        INFLOW, OUTFLOW
    }
    
    public InvoiceEntity() {
    }
    
    public InvoiceEntity(String customerName, String invoiceNumber, LocalDate invoiceDate,
                         LocalDate dueDate, BigDecimal invoiceValue, InvoiceType invoiceType, String source) {
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.invoiceValue = invoiceValue;
        this.invoiceType = invoiceType;
        this.source = source;
        this.reconciled = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getExpectedCashDate() {
        return expectedCashDate;
    }

    public void setExpectedCashDate(LocalDate expectedCashDate) {
        this.expectedCashDate = expectedCashDate;
    }

    public String getCashflowRemarks() {
        return cashflowRemarks;
    }

    public void setCashflowRemarks(String cashflowRemarks) {
        this.cashflowRemarks = cashflowRemarks;
    }
    
    public BigDecimal getInvoiceValue() {
        return invoiceValue;
    }
    
    public void setInvoiceValue(BigDecimal invoiceValue) {
        this.invoiceValue = invoiceValue;
    }
    
    public InvoiceType getInvoiceType() {
        return invoiceType;
    }
    
    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }
}
