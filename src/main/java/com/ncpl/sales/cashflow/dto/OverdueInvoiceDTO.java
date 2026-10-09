package com.ncpl.sales.cashflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OverdueInvoiceDTO {
    private String invoiceNumber;
    private String customerName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private long daysOverdue;
    private String severity;
    
    public OverdueInvoiceDTO() {}
    
    public OverdueInvoiceDTO(String invoiceNumber, String customerName, BigDecimal amount, 
                            LocalDate dueDate, long daysOverdue) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.amount = amount;
        this.dueDate = dueDate;
        this.daysOverdue = daysOverdue;
        this.severity = calculateSeverity();
    }
    
    private String calculateSeverity() {
        if (daysOverdue <= 30) return "Minor";
        if (daysOverdue <= 60) return "Moderate";
        if (daysOverdue <= 180) return "Serious";
        return "Critical";
    }
    
    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public long getDaysOverdue() {
        return daysOverdue;
    }
    
    public void setDaysOverdue(long daysOverdue) {
        this.daysOverdue = daysOverdue;
        this.severity = calculateSeverity();
    }
    
    public String getSeverity() {
        if (severity == null) {
            severity = calculateSeverity();
        }
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
