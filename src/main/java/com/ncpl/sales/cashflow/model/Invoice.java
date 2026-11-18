package com.ncpl.sales.cashflow.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Invoice {
    public enum AgingMode {
        OVERDUE_ONLY,
        RECEIVABLES_FULL
    }
    private Long id;
    private String customerName;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private double invoiceValue;
    private InvoiceType type;
    private boolean reconciled = false;
    
    public enum InvoiceType {
        INFLOW, OUTFLOW
    }
    
    public Invoice() {}

    public Invoice(String customerName, String invoiceNumber, LocalDate invoiceDate, 
                   LocalDate dueDate, double invoiceValue, InvoiceType type) {
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.invoiceValue = invoiceValue;
        this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    // Calculate days overdue
    public long getDaysOverdue() {
        if (dueDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (today.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, today);
        }
        return 0;
    }
    
    public boolean isOverdue() {
        return dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    // Bill age is independent of payment terms and always starts at the bill date.
    public Long getBillAgeDays() {
        if (invoiceDate == null) return null;
        return Math.max(0, ChronoUnit.DAYS.between(invoiceDate, LocalDate.now()));
    }
    
    public long getDaysUntilDue() {
        if (dueDate == null) return Long.MAX_VALUE;
        LocalDate today = LocalDate.now();
        if (today.isAfter(dueDate)) {
            return -getDaysOverdue(); // Negative value indicates overdue
        }
        return ChronoUnit.DAYS.between(today, dueDate);
    }
    
    public String getAgingBucket() {
        return getAgingBucket(AgingMode.OVERDUE_ONLY);
    }
    
    public String getAgingBucket(AgingMode mode) {
        if (dueDate == null) return "Not Due";
        
        LocalDate today = LocalDate.now();
        if (today.isBefore(dueDate) || today.equals(dueDate)) {
            if (mode == AgingMode.RECEIVABLES_FULL) {
                long daysUntilDue = getDaysUntilDue();
                if (daysUntilDue <= 30) return "0-30 days";
                if (daysUntilDue <= 60) return "31-60 days";
                if (daysUntilDue <= 90) return "61-90 days";
                return ">90 days";
            }
            return "Not Due";
        }
        
        // For overdue invoices
        long daysOverdue = getDaysOverdue();
        if (daysOverdue <= 30) return "0-30 days";
        if (daysOverdue <= 60) return "31-60 days";
        if (daysOverdue <= 90) return "61-90 days";
        return ">90 days";
    }
    
    // Getters and Setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public double getInvoiceValue() { return invoiceValue; }
    public void setInvoiceValue(double invoiceValue) { this.invoiceValue = invoiceValue; }
    
    public InvoiceType getType() { return type; }
    public void setType(InvoiceType type) { this.type = type; }

    public boolean isReconciled() { return reconciled; }
    public void setReconciled(boolean reconciled) { this.reconciled = reconciled; }
}
