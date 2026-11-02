package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.dto.AnalyticsDTO;
import com.ncpl.sales.cashflow.dto.AnalyticsDTO.MonthlyData;
import com.ncpl.sales.cashflow.dto.AnalyticsDTO.WeeklyData;
import com.ncpl.sales.cashflow.model.Invoice;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class AnalyticsService {
    
    public AnalyticsDTO analyzeMonthlyAndWeekly(List<Invoice> invoices) {
        // Find most recent month with data
        YearMonth mostRecentMonth = invoices.stream()
                .map(Invoice::getInvoiceDate)
                .filter(Objects::nonNull)
                .map(YearMonth::from)
                .max(Comparator.naturalOrder())
                .orElse(YearMonth.now());
        
        // Calculate DAILY breakdown for the most recent month (instead of 12 months)
        List<MonthlyData> currentMonthDailyData = calculateCurrentMonthDailyData(invoices, mostRecentMonth);
        
        // Calculate weekly data for the same month
        Map<Integer, WeeklyData> weeklyData = calculateWeeklyData(invoices);
        
        String weeklyMonthLabel = mostRecentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        
        return new AnalyticsDTO(currentMonthDailyData, weeklyData, weeklyMonthLabel);
    }
    
    private List<MonthlyData> calculateMonthlyData(List<Invoice> invoices) {
        Map<YearMonth, MonthlySummary> monthlyMap = new TreeMap<>();
        
        // Get last 12 months
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            monthlyMap.put(yearMonth, new MonthlySummary());
        }
        
        // Aggregate invoices by month
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceDate() != null) {
                YearMonth invoiceMonth = YearMonth.from(invoice.getInvoiceDate());
                
                if (monthlyMap.containsKey(invoiceMonth)) {
                    MonthlySummary summary = monthlyMap.get(invoiceMonth);
                    
                    if (invoice.getType() == Invoice.InvoiceType.INFLOW) {
                        summary.inflow = summary.inflow.add(BigDecimal.valueOf(invoice.getInvoiceValue()));
                    } else {
                        summary.outflow = summary.outflow.add(BigDecimal.valueOf(invoice.getInvoiceValue()));
                    }
                }
            }
        }
        
        // Convert to MonthlyData list
        List<MonthlyData> result = new ArrayList<>();
        for (Map.Entry<YearMonth, MonthlySummary> entry : monthlyMap.entrySet()) {
            String monthLabel = entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy"));
            MonthlySummary summary = entry.getValue();
            result.add(new MonthlyData(monthLabel, summary.inflow, summary.outflow));
        }
        
        return result;
    }
    
    private Map<Integer, WeeklyData> calculateWeeklyData(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return initializeEmptyWeeks(YearMonth.now());
        }
        
        // Find the most recent month that has invoice data
        YearMonth mostRecentMonth = invoices.stream()
                .map(Invoice::getInvoiceDate)
                .filter(Objects::nonNull)
                .map(YearMonth::from)
                .max(Comparator.naturalOrder())
                .orElse(YearMonth.now());
        
        System.out.println("=== WEEKLY DATA CALCULATION ===");
        System.out.println("Most Recent Month with Data: " + mostRecentMonth);
        
        // Initialize 4 weeks for the most recent month
        Map<Integer, WeeklySummary> weeklyMap = initializeWeeksForMonth(mostRecentMonth);
        
        // Aggregate invoices by week for this month
        for (Invoice invoice : invoices) {
            LocalDate invoiceDate = invoice.getInvoiceDate();
            
            if (invoiceDate != null && YearMonth.from(invoiceDate).equals(mostRecentMonth)) {
                int dayOfMonth = invoiceDate.getDayOfMonth();
                int weekNumber = ((dayOfMonth - 1) / 7) + 1;
                weekNumber = Math.min(weekNumber, 4); // Cap at week 4
                
                WeeklySummary summary = weeklyMap.get(weekNumber);
                if (summary != null) {
                    if (invoice.getType() == Invoice.InvoiceType.INFLOW) {
                        summary.inflow = summary.inflow.add(BigDecimal.valueOf(invoice.getInvoiceValue()));
                    } else {
                        summary.outflow = summary.outflow.add(BigDecimal.valueOf(invoice.getInvoiceValue()));
                    }
                    summary.transactionCount++;
                }
            }
        }
        
        // Convert to WeeklyData map
        Map<Integer, WeeklyData> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, WeeklySummary> entry : weeklyMap.entrySet()) {
            int weekNum = entry.getKey();
            WeeklySummary summary = entry.getValue();
            result.put(weekNum, new WeeklyData(
                weekNum,
                summary.weekLabel,
                summary.inflow,
                summary.outflow,
                summary.transactionCount
            ));
        }
        
        // Debug output
        System.out.println("=== WEEKLY DATA RESULTS ===");
        result.forEach((week, data) -> {
            System.out.println(String.format("Week %d (%s): Inflow=₹%.2f, Count=%d", 
                week, data.getWeekLabel(), data.getInflow(), data.getTransactionCount()));
        });
        
        return result;
    }
    
    private Map<Integer, WeeklySummary> initializeWeeksForMonth(YearMonth yearMonth) {
        Map<Integer, WeeklySummary> weeklyMap = new LinkedHashMap<>();
        int daysInMonth = yearMonth.lengthOfMonth();
        
        for (int week = 1; week <= 4; week++) {
            int startDay = ((week - 1) * 7) + 1;
            int endDay = Math.min(week * 7, daysInMonth);
            
            String weekLabel = String.format("Week %d (%s %d-%d)", 
                week,
                yearMonth.getMonth().toString().substring(0, 3),
                startDay,
                endDay
            );
            
            weeklyMap.put(week, new WeeklySummary(weekLabel));
        }
        
        return weeklyMap;
    }
    
    private Map<Integer, WeeklyData> initializeEmptyWeeks(YearMonth yearMonth) {
        Map<Integer, WeeklySummary> weeklyMap = initializeWeeksForMonth(yearMonth);
        Map<Integer, WeeklyData> result = new LinkedHashMap<>();
        
        for (Map.Entry<Integer, WeeklySummary> entry : weeklyMap.entrySet()) {
            int weekNum = entry.getKey();
            WeeklySummary summary = entry.getValue();
            result.put(weekNum, new WeeklyData(
                weekNum,
                summary.weekLabel,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
            ));
        }
        
        return result;
    }
    
    private List<MonthlyData> calculateCurrentMonthDailyData(List<Invoice> invoices, YearMonth targetMonth) {
        System.out.println("=== CURRENT MONTH DAILY DATA ===");
        System.out.println("Target Month: " + targetMonth);
        
        Map<Integer, MonthlySummary> dailyMap = new TreeMap<>();
        int daysInMonth = targetMonth.lengthOfMonth();
        
        // Initialize all days of the month
        for (int day = 1; day <= daysInMonth; day++) {
            dailyMap.put(day, new MonthlySummary());
        }
        
        // Aggregate invoices by day
        for (Invoice invoice : invoices) {
            LocalDate invoiceDate = invoice.getInvoiceDate();
            if (invoiceDate != null && YearMonth.from(invoiceDate).equals(targetMonth)) {
                int dayOfMonth = invoiceDate.getDayOfMonth();
                MonthlySummary dayData = dailyMap.get(dayOfMonth);
                
                if (dayData != null) {
                    if (invoice.getType() == Invoice.InvoiceType.INFLOW) {
                        dayData.inflow = dayData.inflow.add(BigDecimal.valueOf(invoice.getInvoiceValue()));
                    } else {
                        dayData.outflow = dayData.outflow.add(BigDecimal.valueOf(invoice.getInvoiceValue()));
                    }
                }
            }
        }
        
        // Convert to MonthlyData list with day labels
        List<MonthlyData> result = new ArrayList<>();
        for (Map.Entry<Integer, MonthlySummary> entry : dailyMap.entrySet()) {
            int day = entry.getKey();
            String dayLabel = targetMonth.getMonth().toString().substring(0, 3) + " " + day;
            MonthlySummary summary = entry.getValue();
            result.add(new MonthlyData(dayLabel, summary.inflow, summary.outflow));
        }
        
        long daysWithData = result.stream()
                .filter(d -> d.getInflow().compareTo(BigDecimal.ZERO) > 0 || d.getOutflow().compareTo(BigDecimal.ZERO) > 0)
                .count();
        System.out.println("Total days with data: " + daysWithData);
        
        return result;
    }
    
    // Helper classes for aggregation
    private static class MonthlySummary {
        BigDecimal inflow = BigDecimal.ZERO;
        BigDecimal outflow = BigDecimal.ZERO;
    }
    
    private static class WeeklySummary {
        String weekLabel;
        BigDecimal inflow = BigDecimal.ZERO;
        BigDecimal outflow = BigDecimal.ZERO;
        int transactionCount = 0;
        
        WeeklySummary(String weekLabel) {
            this.weekLabel = weekLabel;
        }
    }
}
