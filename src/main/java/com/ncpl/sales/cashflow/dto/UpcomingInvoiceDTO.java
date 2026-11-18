package com.ncpl.sales.cashflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpcomingInvoiceDTO {

    private String invoiceNumber;
    private String customerName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private long daysUntilDue;
    private String status;

    public UpcomingInvoiceDTO() {
    }

    public UpcomingInvoiceDTO(String invoiceNumber,
                              String customerName,
                              BigDecimal amount,
                              LocalDate dueDate,
                              long daysUntilDue) {
        this.invoiceNumber = invoiceNumber;
        this.customerName = customerName;
        this.amount = amount;
        this.dueDate = dueDate;
        this.daysUntilDue = daysUntilDue;
        this.status = buildStatus();
    }

    private String buildStatus() {
        if (daysUntilDue < 0) {
            return "Overdue";
        }
        if (daysUntilDue == 0) {
            return "Due today";
        }
        if (daysUntilDue == 1) {
            return "Due tomorrow";
        }
        if (daysUntilDue <= 7) {
            return "Due in " + daysUntilDue + " days";
        }
        return "Due in " + daysUntilDue + " days";
    }

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

    public long getDaysUntilDue() {
        return daysUntilDue;
    }

    public void setDaysUntilDue(long daysUntilDue) {
        this.daysUntilDue = daysUntilDue;
        this.status = buildStatus();
    }

    public String getStatus() {
        if (status == null) {
            status = buildStatus();
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

