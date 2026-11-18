package com.ncpl.sales.cashflow.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TopPerformanceDTO {

    private YearMonth month;
    private Integer week;
    private double totalInflow;
    private double totalOutflow;
    private List<EntityPerformance> topCustomers = new ArrayList<>();
    private List<EntityPerformance> topVendors = new ArrayList<>();
    private List<TrendPoint> trend = new ArrayList<>();

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

    public double getTotalInflow() {
        return totalInflow;
    }

    public void setTotalInflow(double totalInflow) {
        this.totalInflow = totalInflow;
    }

    public double getTotalOutflow() {
        return totalOutflow;
    }

    public void setTotalOutflow(double totalOutflow) {
        this.totalOutflow = totalOutflow;
    }

    public List<EntityPerformance> getTopCustomers() {
        return topCustomers;
    }

    public void setTopCustomers(List<EntityPerformance> topCustomers) {
        this.topCustomers = (topCustomers != null ? topCustomers : new ArrayList<>());
    }

    public List<EntityPerformance> getTopVendors() {
        return topVendors;
    }

    public void setTopVendors(List<EntityPerformance> topVendors) {
        this.topVendors = (topVendors != null ? topVendors : new ArrayList<>());
    }

    public List<TrendPoint> getTrend() {
        return trend;
    }

    public void setTrend(List<TrendPoint> trend) {
        this.trend = (trend != null ? trend : new ArrayList<>());
    }

    public static class EntityPerformance {
        private String name;
        private double amount;
        private double share;
        private int invoiceCount;

        public EntityPerformance() {
        }

        public EntityPerformance(String name, double amount, double share, int invoiceCount) {
            this.name = name;
            this.amount = amount;
            this.share = share;
            this.invoiceCount = invoiceCount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getShare() {
            return share;
        }

        public void setShare(double share) {
            this.share = share;
        }

        public int getInvoiceCount() {
            return invoiceCount;
        }

        public void setInvoiceCount(int invoiceCount) {
            this.invoiceCount = invoiceCount;
        }
    }

    public static class TrendPoint {
        private LocalDate date;
        private double inflow;
        private double outflow;

        public TrendPoint() {
        }

        public TrendPoint(LocalDate date, double inflow, double outflow) {
            this.date = date;
            this.inflow = inflow;
            this.outflow = outflow;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public double getInflow() {
            return inflow;
        }

        public void setInflow(double inflow) {
            this.inflow = inflow;
        }

        public double getOutflow() {
            return outflow;
        }

        public void setOutflow(double outflow) {
            this.outflow = outflow;
        }
    }
}

