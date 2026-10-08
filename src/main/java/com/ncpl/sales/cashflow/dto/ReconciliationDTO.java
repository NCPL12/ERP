package com.ncpl.sales.cashflow.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReconciliationDTO {

    private YearMonth month;
    private Integer week;
    private double receivablesTotal;
    private double payablesTotal;
    private List<InvoiceSelectionDTO> receivables = new ArrayList<>();
    private List<InvoiceSelectionDTO> payables = new ArrayList<>();

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public double getReceivablesTotal() {
        return receivablesTotal;
    }

    public void setReceivablesTotal(double receivablesTotal) {
        this.receivablesTotal = receivablesTotal;
    }

    public double getPayablesTotal() {
        return payablesTotal;
    }

    public void setPayablesTotal(double payablesTotal) {
        this.payablesTotal = payablesTotal;
    }

    public List<InvoiceSelectionDTO> getReceivables() {
        return receivables;
    }

    public void setReceivables(List<InvoiceSelectionDTO> receivables) {
        this.receivables = (receivables != null ? receivables : new ArrayList<>());
    }

    public List<InvoiceSelectionDTO> getPayables() {
        return payables;
    }

    public void setPayables(List<InvoiceSelectionDTO> payables) {
        this.payables = (payables != null ? payables : new ArrayList<>());
    }

    public static class InvoiceSelectionDTO {
        private Long id;
        private String invoiceNumber;
        private String name;
        private LocalDate dueDate;
        private LocalDate invoiceDate;
        private double amount;
        private String status;
        private boolean reconciled;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }

        public LocalDate getInvoiceDate() {
            return invoiceDate;
        }

        public void setInvoiceDate(LocalDate invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isReconciled() {
            return reconciled;
        }

        public void setReconciled(boolean reconciled) {
            this.reconciled = reconciled;
        }
    }
}

