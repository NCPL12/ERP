package com.ncpl.sales.cashflow.model;

import java.util.Map;

public class CashFlowSummary {
    private double totalInflow;
    private double totalOutflow;
    private double netCashFlow;
    private int totalInflowInvoices;
    private int totalOutflowInvoices;
    private int overdueInflowCount;
    private int overdueOutflowCount;
    private double overdueInflowAmount;
    private double overdueOutflowAmount;
    private Map<String, Double> monthlyInflow;
    private Map<String, Double> monthlyOutflow;
    private Map<String, Double> topCustomers;
    private Map<String, Double> topSuppliers;
    private Map<String, Double> agingBuckets;
    private double totalOverdueAmount;
    private int overdueInvoiceCount;
    private double averageDaysOverdue;
    private long maxDaysOverdue;
    
    // Constructor
    public CashFlowSummary() {}
    
    // Getters and Setters
    public double getTotalInflow() { return totalInflow; }
    public void setTotalInflow(double totalInflow) { this.totalInflow = totalInflow; }
    
    public double getTotalOutflow() { return totalOutflow; }
    public void setTotalOutflow(double totalOutflow) { this.totalOutflow = totalOutflow; }
    
    public double getNetCashFlow() { return netCashFlow; }
    public void setNetCashFlow(double netCashFlow) { this.netCashFlow = netCashFlow; }
    
    public int getTotalInflowInvoices() { return totalInflowInvoices; }
    public void setTotalInflowInvoices(int totalInflowInvoices) { 
        this.totalInflowInvoices = totalInflowInvoices; 
    }
    
    public int getTotalOutflowInvoices() { return totalOutflowInvoices; }
    public void setTotalOutflowInvoices(int totalOutflowInvoices) { 
        this.totalOutflowInvoices = totalOutflowInvoices; 
    }
    
    public int getOverdueInflowCount() { return overdueInflowCount; }
    public void setOverdueInflowCount(int overdueInflowCount) { 
        this.overdueInflowCount = overdueInflowCount; 
    }
    
    public int getOverdueOutflowCount() { return overdueOutflowCount; }
    public void setOverdueOutflowCount(int overdueOutflowCount) { 
        this.overdueOutflowCount = overdueOutflowCount; 
    }
    
    public double getOverdueInflowAmount() { return overdueInflowAmount; }
    public void setOverdueInflowAmount(double overdueInflowAmount) { 
        this.overdueInflowAmount = overdueInflowAmount; 
    }
    
    public double getOverdueOutflowAmount() { return overdueOutflowAmount; }
    public void setOverdueOutflowAmount(double overdueOutflowAmount) { 
        this.overdueOutflowAmount = overdueOutflowAmount; 
    }
    
    public Map<String, Double> getMonthlyInflow() { return monthlyInflow; }
    public void setMonthlyInflow(Map<String, Double> monthlyInflow) { 
        this.monthlyInflow = monthlyInflow; 
    }
    
    public Map<String, Double> getMonthlyOutflow() { return monthlyOutflow; }
    public void setMonthlyOutflow(Map<String, Double> monthlyOutflow) { 
        this.monthlyOutflow = monthlyOutflow; 
    }
    
    public Map<String, Double> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(Map<String, Double> topCustomers) { 
        this.topCustomers = topCustomers; 
    }
    
    public Map<String, Double> getTopSuppliers() { return topSuppliers; }
    public void setTopSuppliers(Map<String, Double> topSuppliers) { 
        this.topSuppliers = topSuppliers; 
    }
    
    public Map<String, Double> getAgingBuckets() { return agingBuckets; }
    public void setAgingBuckets(Map<String, Double> agingBuckets) { 
        this.agingBuckets = agingBuckets; 
    }
    
    public double getTotalOverdueAmount() { return totalOverdueAmount; }
    public void setTotalOverdueAmount(double totalOverdueAmount) {
        this.totalOverdueAmount = totalOverdueAmount;
    }
    
    public int getOverdueInvoiceCount() { return overdueInvoiceCount; }
    public void setOverdueInvoiceCount(int overdueInvoiceCount) {
        this.overdueInvoiceCount = overdueInvoiceCount;
    }
    
    public double getAverageDaysOverdue() { return averageDaysOverdue; }
    public void setAverageDaysOverdue(double averageDaysOverdue) {
        this.averageDaysOverdue = averageDaysOverdue;
    }
    
    public long getMaxDaysOverdue() { return maxDaysOverdue; }
    public void setMaxDaysOverdue(long maxDaysOverdue) {
        this.maxDaysOverdue = maxDaysOverdue;
    }
}
