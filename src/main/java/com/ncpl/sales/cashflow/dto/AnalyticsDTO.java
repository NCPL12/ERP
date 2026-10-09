package com.ncpl.sales.cashflow.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsDTO {
    private List<MonthlyData> monthlyData;
    private Map<Integer, WeeklyData> weeklyData; // Key: week number (1-4)
    private String weeklyDataMonth; // e.g., "October 2025"
    
    public static class MonthlyData {
        private String month; // "Jan 2025", "Feb 2025", etc.
        private BigDecimal inflow;
        private BigDecimal outflow;
        private BigDecimal netCashflow;
        
        public MonthlyData(String month, BigDecimal inflow, BigDecimal outflow) {
            this.month = month;
            this.inflow = inflow;
            this.outflow = outflow;
            this.netCashflow = inflow.subtract(outflow);
        }
        
        // Getters and setters
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public BigDecimal getInflow() { return inflow; }
        public void setInflow(BigDecimal inflow) { this.inflow = inflow; }
        public BigDecimal getOutflow() { return outflow; }
        public void setOutflow(BigDecimal outflow) { this.outflow = outflow; }
        public BigDecimal getNetCashflow() { return netCashflow; }
    }
    
    public static class WeeklyData {
        private int weekNumber; // 1, 2, 3, 4
        private String weekLabel; // "Week 1 (Nov 1-7)", "Week 2 (Nov 8-14)", etc.
        private BigDecimal inflow;
        private BigDecimal outflow;
        private BigDecimal netCashflow;
        private int transactionCount;
        
        public WeeklyData(int weekNumber, String weekLabel, BigDecimal inflow, BigDecimal outflow, int transactionCount) {
            this.weekNumber = weekNumber;
            this.weekLabel = weekLabel;
            this.inflow = inflow;
            this.outflow = outflow;
            this.netCashflow = inflow.subtract(outflow);
            this.transactionCount = transactionCount;
        }
        
        // Getters and setters
        public int getWeekNumber() { return weekNumber; }
        public String getWeekLabel() { return weekLabel; }
        public BigDecimal getInflow() { return inflow; }
        public BigDecimal getOutflow() { return outflow; }
        public BigDecimal getNetCashflow() { return netCashflow; }
        public int getTransactionCount() { return transactionCount; }
    }
    
    // Constructor, getters, setters
    public AnalyticsDTO(List<MonthlyData> monthlyData, Map<Integer, WeeklyData> weeklyData, String weeklyDataMonth) {
        this.monthlyData = monthlyData;
        this.weeklyData = weeklyData;
        this.weeklyDataMonth = weeklyDataMonth;
    }
    
    public List<MonthlyData> getMonthlyData() { return monthlyData; }
    public Map<Integer, WeeklyData> getWeeklyData() { return weeklyData; }
    public String getWeeklyDataMonth() { return weeklyDataMonth; }
    public void setWeeklyDataMonth(String weeklyDataMonth) { this.weeklyDataMonth = weeklyDataMonth; }
}
