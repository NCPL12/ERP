package com.ncpl.sales.cashflow.dto;

import java.math.BigDecimal;
import java.util.List;

public class OverviewAnalysisDTO {
    private int activeCustomers;
    private BigDecimal totalInflow;
    private BigDecimal totalOutflow;
    private BigDecimal netCashflow;
    private int totalOverdueCount;
    private double collectionSuccessRate;
    private List<OverdueInvoiceDTO> overdueReceivables;
    private List<OverdueInvoiceDTO> overduePayables;
    private BigDecimal overdueReceivablesAmount;
    private int overdueReceivablesCount;
    private BigDecimal overduePayablesAmount;
    private int overduePayablesCount;
    private List<UpcomingInvoiceDTO> dueReceivables;
    private List<UpcomingInvoiceDTO> duePayables;
    private BigDecimal dueReceivablesAmount;
    private int dueReceivablesCount;
    private BigDecimal duePayablesAmount;
    private int duePayablesCount;
    private BigDecimal bankBalance = BigDecimal.ZERO;
    
    public OverviewAnalysisDTO() {}
    
    public OverviewAnalysisDTO(int activeCustomers, BigDecimal totalInflow, 
                              BigDecimal totalOutflow, BigDecimal netCashflow,
                              int totalOverdueCount, double collectionSuccessRate,
                              List<OverdueInvoiceDTO> overdueReceivables,
                              List<OverdueInvoiceDTO> overduePayables,
                              List<UpcomingInvoiceDTO> dueReceivables,
                              List<UpcomingInvoiceDTO> duePayables) {
        this.activeCustomers = activeCustomers;
        this.totalInflow = totalInflow;
        this.totalOutflow = totalOutflow;
        this.netCashflow = netCashflow;
        this.totalOverdueCount = totalOverdueCount;
        this.collectionSuccessRate = collectionSuccessRate;
        this.overdueReceivables = overdueReceivables != null ? overdueReceivables : com.ncpl.sales.cashflow.util.Java8Collections.list();
        this.overduePayables = overduePayables != null ? overduePayables : com.ncpl.sales.cashflow.util.Java8Collections.list();
        this.dueReceivables = dueReceivables != null ? dueReceivables : com.ncpl.sales.cashflow.util.Java8Collections.list();
        this.duePayables = duePayables != null ? duePayables : com.ncpl.sales.cashflow.util.Java8Collections.list();
        
        // Calculate overdue amounts and counts
        this.overdueReceivablesAmount = this.overdueReceivables.stream()
                .map(OverdueInvoiceDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.overdueReceivablesCount = this.overdueReceivables.size();
        
        this.overduePayablesAmount = this.overduePayables.stream()
                .map(OverdueInvoiceDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.overduePayablesCount = this.overduePayables.size();
        
        this.dueReceivablesAmount = this.dueReceivables.stream()
                .map(UpcomingInvoiceDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.dueReceivablesCount = this.dueReceivables.size();
        
        this.duePayablesAmount = this.duePayables.stream()
                .map(UpcomingInvoiceDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.duePayablesCount = this.duePayables.size();
    }
    
    // Getters and Setters
    public int getActiveCustomers() {
        return activeCustomers;
    }
    
    public void setActiveCustomers(int activeCustomers) {
        this.activeCustomers = activeCustomers;
    }
    
    public BigDecimal getTotalInflow() {
        return totalInflow;
    }
    
    public void setTotalInflow(BigDecimal totalInflow) {
        this.totalInflow = totalInflow;
    }
    
    public BigDecimal getTotalOutflow() {
        return totalOutflow;
    }
    
    public void setTotalOutflow(BigDecimal totalOutflow) {
        this.totalOutflow = totalOutflow;
    }
    
    public BigDecimal getNetCashflow() {
        return netCashflow;
    }
    
    public void setNetCashflow(BigDecimal netCashflow) {
        this.netCashflow = netCashflow;
    }
    
    public int getTotalOverdueCount() {
        return totalOverdueCount;
    }
    
    public void setTotalOverdueCount(int totalOverdueCount) {
        this.totalOverdueCount = totalOverdueCount;
    }
    
    public double getCollectionSuccessRate() {
        return collectionSuccessRate;
    }
    
    public void setCollectionSuccessRate(double collectionSuccessRate) {
        this.collectionSuccessRate = collectionSuccessRate;
    }
    
    public List<OverdueInvoiceDTO> getOverdueReceivables() {
        return overdueReceivables;
    }
    
    public void setOverdueReceivables(List<OverdueInvoiceDTO> overdueReceivables) {
        this.overdueReceivables = overdueReceivables;
    }
    
    public List<OverdueInvoiceDTO> getOverduePayables() {
        return overduePayables;
    }
    
    public void setOverduePayables(List<OverdueInvoiceDTO> overduePayables) {
        this.overduePayables = overduePayables;
    }
    
    public BigDecimal getOverdueReceivablesAmount() {
        return overdueReceivablesAmount;
    }
    
    public void setOverdueReceivablesAmount(BigDecimal overdueReceivablesAmount) {
        this.overdueReceivablesAmount = overdueReceivablesAmount;
    }
    
    public int getOverdueReceivablesCount() {
        return overdueReceivablesCount;
    }
    
    public void setOverdueReceivablesCount(int overdueReceivablesCount) {
        this.overdueReceivablesCount = overdueReceivablesCount;
    }
    
    public BigDecimal getOverduePayablesAmount() {
        return overduePayablesAmount;
    }
    
    public void setOverduePayablesAmount(BigDecimal overduePayablesAmount) {
        this.overduePayablesAmount = overduePayablesAmount;
    }
    
    public int getOverduePayablesCount() {
        return overduePayablesCount;
    }
    
    public void setOverduePayablesCount(int overduePayablesCount) {
        this.overduePayablesCount = overduePayablesCount;
    }

    public List<UpcomingInvoiceDTO> getDueReceivables() {
        return dueReceivables;
    }

    public void setDueReceivables(List<UpcomingInvoiceDTO> dueReceivables) {
        this.dueReceivables = dueReceivables;
    }

    public List<UpcomingInvoiceDTO> getDuePayables() {
        return duePayables;
    }

    public void setDuePayables(List<UpcomingInvoiceDTO> duePayables) {
        this.duePayables = duePayables;
    }

    public BigDecimal getDueReceivablesAmount() {
        return dueReceivablesAmount;
    }

    public void setDueReceivablesAmount(BigDecimal dueReceivablesAmount) {
        this.dueReceivablesAmount = dueReceivablesAmount;
    }

    public int getDueReceivablesCount() {
        return dueReceivablesCount;
    }

    public void setDueReceivablesCount(int dueReceivablesCount) {
        this.dueReceivablesCount = dueReceivablesCount;
    }

    public BigDecimal getDuePayablesAmount() {
        return duePayablesAmount;
    }

    public void setDuePayablesAmount(BigDecimal duePayablesAmount) {
        this.duePayablesAmount = duePayablesAmount;
    }

    public int getDuePayablesCount() {
        return duePayablesCount;
    }

    public void setDuePayablesCount(int duePayablesCount) {
        this.duePayablesCount = duePayablesCount;
    }

    public BigDecimal getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(BigDecimal bankBalance) {
        this.bankBalance = bankBalance;
    }
}
