package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.model.Invoice;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerVendorAnalysisService {
    
    /**
     * Analyze ALL invoices (includes both paid and unpaid, future due dates)
     * Used by: Due Date Tracker
     */
    public CustomerVendorAnalysisDTO analyze(MultipartFile file) throws IOException {
        LocalDate today = LocalDate.now();
        
        List<InvoiceRecord> receivables = parseSheet(file, "Inflow");
        List<InvoiceRecord> payables = parseSheet(file, "Outflow");
        
        System.out.println("\n========== DUE DATE TRACKER (ALL INVOICES) ==========");
        System.out.println("📊 Total receivables: " + receivables.size());
        System.out.println("📊 Total payables: " + payables.size());
        System.out.println("=====================================================\n");
        
        List<WeeklyData> weeklyCustomers = calculateWeeklyData(receivables, today);
        List<WeeklyData> weeklyVendors = calculateWeeklyData(payables, today);
        
        List<MonthlyData> monthlyCustomers = calculateMonthlyData(receivables);
        List<MonthlyData> monthlyVendors = calculateMonthlyData(payables);
        
        return new CustomerVendorAnalysisDTO(weeklyCustomers, weeklyVendors, monthlyCustomers, monthlyVendors);
    }
    
    /**
     * Analyze ONLY OVERDUE invoices (due date < today)
     * Used by: Overdue Payments
     */
    public CustomerVendorAnalysisDTO analyzeOverdueOnly(MultipartFile file) throws IOException {
        LocalDate today = LocalDate.now();
        
        List<InvoiceRecord> receivables = parseSheet(file, "Inflow");
        List<InvoiceRecord> payables = parseSheet(file, "Outflow");
        
        // Filter only overdue invoices (due date < today)
        List<InvoiceRecord> overdueReceivables = receivables.stream()
            .filter(inv -> inv.dueDate != null && inv.dueDate.isBefore(today))
            .collect(Collectors.toList());
            
        List<InvoiceRecord> overduePayables = payables.stream()
            .filter(inv -> inv.dueDate != null && inv.dueDate.isBefore(today))
            .collect(Collectors.toList());
        
        System.out.println("\n========== OVERDUE PAYMENTS (OVERDUE ONLY) ==========");
        System.out.println("📅 Today's date: " + today);
        System.out.println("📊 Total receivables: " + receivables.size());
        System.out.println("⚠️  Overdue receivables: " + overdueReceivables.size());
        System.out.println("📊 Total payables: " + payables.size());
        System.out.println("⚠️  Overdue payables: " + overduePayables.size());
        System.out.println("=====================================================\n");
        
        List<WeeklyData> weeklyCustomers = calculateWeeklyData(overdueReceivables, today);
        List<WeeklyData> weeklyVendors = calculateWeeklyData(overduePayables, today);
        
        List<MonthlyData> monthlyCustomers = calculateMonthlyData(overdueReceivables);
        List<MonthlyData> monthlyVendors = calculateMonthlyData(overduePayables);
        
        return new CustomerVendorAnalysisDTO(weeklyCustomers, weeklyVendors, monthlyCustomers, monthlyVendors);
    }

    /**
     * Analyze invoices already loaded in memory (used when filtering session data).
     */
    public CustomerVendorAnalysisDTO analyze(List<Invoice> invoices) {
        return analyze(invoices, false);
    }

    /**
     * Analyze only overdue invoices from an in-memory list.
     */
    public CustomerVendorAnalysisDTO analyzeOverdueOnly(List<Invoice> invoices) {
        return analyze(invoices, true);
    }

    private CustomerVendorAnalysisDTO analyze(List<Invoice> invoices, boolean overdueOnly) {
        if (invoices == null) {
            return new CustomerVendorAnalysisDTO(Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList());
        }

        LocalDate today = LocalDate.now();

        List<InvoiceRecord> receivables = invoices.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.INFLOW)
                .map(this::toInvoiceRecord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<InvoiceRecord> payables = invoices.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.OUTFLOW)
                .map(this::toInvoiceRecord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (overdueOnly) {
            receivables = receivables.stream()
                    .filter(inv -> inv.dueDate != null && inv.dueDate.isBefore(today))
                    .collect(Collectors.toList());

            payables = payables.stream()
                    .filter(inv -> inv.dueDate != null && inv.dueDate.isBefore(today))
                    .collect(Collectors.toList());
        }

        List<WeeklyData> weeklyCustomers = calculateWeeklyData(receivables, today);
        List<WeeklyData> weeklyVendors = calculateWeeklyData(payables, today);

        List<MonthlyData> monthlyCustomers = calculateMonthlyData(receivables);
        List<MonthlyData> monthlyVendors = calculateMonthlyData(payables);

        return new CustomerVendorAnalysisDTO(weeklyCustomers, weeklyVendors, monthlyCustomers, monthlyVendors);
    }
    
    public List<com.ncpl.sales.cashflow.entity.InvoiceEntity> parseToEntities(MultipartFile file) throws IOException {
        List<com.ncpl.sales.cashflow.entity.InvoiceEntity> entities = new ArrayList<>();
        
        // Parse inflow (receivables)
        List<InvoiceRecord> inflow = parseSheet(file, "Inflow");
        for (InvoiceRecord rec : inflow) {
            entities.add(mapRecordToEntity(rec, com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.INFLOW));
        }
        
        // Parse outflow (payables)
        List<InvoiceRecord> outflow = parseSheet(file, "Outflow");
        for (InvoiceRecord rec : outflow) {
            entities.add(mapRecordToEntity(rec, com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.OUTFLOW));
        }
        
        return entities;
    }

    private com.ncpl.sales.cashflow.entity.InvoiceEntity mapRecordToEntity(InvoiceRecord rec, com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType type) {
        com.ncpl.sales.cashflow.entity.InvoiceEntity entity = new com.ncpl.sales.cashflow.entity.InvoiceEntity();
        entity.setCustomerName(rec.name);
        entity.setInvoiceNumber(rec.invoiceNo);
        entity.setInvoiceDate(rec.invoiceDate);
        entity.setDueDate(rec.dueDate);
        entity.setInvoiceValue(rec.amount);
        entity.setInvoiceType(type);
        entity.setSource("EXCEL");
        entity.setReconciled(false);
        return entity;
    }

    private List<InvoiceRecord> parseSheet(MultipartFile file, String sheetName) throws IOException {
        List<InvoiceRecord> records = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(sheetName);
            
            if (sheet == null) {
                System.err.println("Sheet not found: " + sheetName);
                return records;
            }
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    String name = getCellValueAsString(row.getCell(0));
                    String invoiceNo = getCellValueAsString(row.getCell(1));
                    LocalDate invoiceDate = getCellValueAsDate(row.getCell(2));
                    LocalDate dueDate = getCellValueAsDate(row.getCell(3));
                    BigDecimal amount = getCellValueAsBigDecimal(row.getCell(4));
                    
                    if (name != null && amount != null && dueDate != null) {
                        records.add(new InvoiceRecord(name, invoiceNo, invoiceDate, dueDate, amount));
                    }
                } catch (Exception e) {
                    // Skip invalid rows
                }
            }
        }
        
        return records;
    }
    
    /**
     * FIXED: Calculate weekly data dynamically based on actual invoice date range
     * Instead of hardcoding October 2025, this now adapts to whatever dates are in the dataset
     */
    private List<WeeklyData> calculateWeeklyData(List<InvoiceRecord> invoices, LocalDate today) {
        List<WeeklyData> weeklyData = new ArrayList<>();
        
        if (invoices.isEmpty()) {
            // Return 4 empty weeks if no data
            for (int i = 0; i < 4; i++) {
                weeklyData.add(new WeeklyData("Week " + (i + 1), BigDecimal.ZERO, new ArrayList<>()));
            }
            return weeklyData;
        }
        
        // Find the earliest and latest due dates in the dataset
        LocalDate earliestDate = invoices.stream()
            .map(inv -> inv.dueDate)
            .filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.of(2025, 10, 1));
        
        LocalDate latestDate = invoices.stream()
            .map(inv -> inv.dueDate)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.of(2025, 10, 31));
        
        // Calculate 4 equal week periods spanning the date range
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(earliestDate, latestDate) + 1;
        long daysPerWeek = Math.max(7, totalDays / 4);
        
        System.out.println("📅 Date range: " + earliestDate + " to " + latestDate + " (" + totalDays + " days)");
        System.out.println("📊 Days per week: " + daysPerWeek);
        
        for (int weekNum = 0; weekNum < 4; weekNum++) {
            LocalDate weekStart = earliestDate.plusDays(weekNum * daysPerWeek);
            LocalDate weekEnd = (weekNum == 3) ? latestDate : weekStart.plusDays(daysPerWeek - 1);
            
            List<InvoiceDetail> weekInvoices = invoices.stream()
                .filter(inv -> !inv.dueDate.isBefore(weekStart) && !inv.dueDate.isAfter(weekEnd))
                .map(inv -> new InvoiceDetail(
                    inv.name,
                    inv.invoiceNo,
                    inv.dueDate,
                    inv.amount
                ))
                .collect(Collectors.toList());
            
            BigDecimal totalAmount = weekInvoices.stream()
                .map(InvoiceDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String weekLabel = formatWeekLabel(weekNum + 1, weekStart, weekEnd);
            
            System.out.println("  " + weekLabel + ": " + weekInvoices.size() + " invoices, ₹" + totalAmount);
            
            weeklyData.add(new WeeklyData(weekLabel, totalAmount, weekInvoices));
        }
        
        return weeklyData;
    }
    
    /**
     * Format week label to handle cross-month date ranges
     */
    private String formatWeekLabel(int weekNum, LocalDate start, LocalDate end) {
        String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        
        if (start.getMonth() == end.getMonth()) {
            // Same month: "Week 1 (OCT 1-7)"
            return String.format("Week %d (%s %d-%d)", 
                weekNum, 
                months[start.getMonthValue() - 1], 
                start.getDayOfMonth(), 
                end.getDayOfMonth());
        } else {
            // Different months: "Week 1 (SEP 25 - OCT 1)"
            return String.format("Week %d (%s %d - %s %d)", 
                weekNum,
                months[start.getMonthValue() - 1], 
                start.getDayOfMonth(),
                months[end.getMonthValue() - 1], 
                end.getDayOfMonth());
        }
    }
    
    private List<MonthlyData> calculateMonthlyData(List<InvoiceRecord> invoices) {
        LocalDate today = LocalDate.now();
        List<MonthlyData> monthlyData = new ArrayList<>();
        
        for (int i = 11; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            
            BigDecimal totalAmount = invoices.stream()
                .filter(inv -> inv.invoiceDate != null && 
                              YearMonth.from(inv.invoiceDate).equals(month))
                .map(inv -> inv.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            long count = invoices.stream()
                .filter(inv -> inv.invoiceDate != null && 
                              YearMonth.from(inv.invoiceDate).equals(month))
                .count();
            
            String monthLabel = month.getMonth().toString().substring(0, 3) + " " + month.getYear();
            
            monthlyData.add(new MonthlyData(monthLabel, totalAmount, (int) count));
        }
        
        return monthlyData;
    }
    
    // Helper methods
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        try {
            int cellType = cell.getCellType();
            if (cellType == Cell.CELL_TYPE_FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            
            if (cellType == Cell.CELL_TYPE_STRING) return cell.getStringCellValue();
            if (cellType == Cell.CELL_TYPE_NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        
        try {
            int cellType = cell.getCellType();
            if (cellType == Cell.CELL_TYPE_FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            
            if (cellType == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;
        
        try {
            int cellType = cell.getCellType();
            if (cellType == Cell.CELL_TYPE_FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            
            if (cellType == Cell.CELL_TYPE_NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    // Inner classes

    private InvoiceRecord toInvoiceRecord(Invoice invoice) {
        if (invoice == null || invoice.getDueDate() == null || invoice.getInvoiceValue() == 0) {
            return null;
        }
        BigDecimal amount = BigDecimal.valueOf(invoice.getInvoiceValue());
        return new InvoiceRecord(
                invoice.getCustomerName(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                amount
        );
    }

    private static class InvoiceRecord {
        String name;
        String invoiceNo;
        LocalDate invoiceDate;
        LocalDate dueDate;
        BigDecimal amount;
        
        InvoiceRecord(String name, String invoiceNo, LocalDate invoiceDate, 
                     LocalDate dueDate, BigDecimal amount) {
            this.name = name;
            this.invoiceNo = invoiceNo;
            this.invoiceDate = invoiceDate;
            this.dueDate = dueDate;
            this.amount = amount;
        }
    }
    
    // DTO Classes
    public static class CustomerVendorAnalysisDTO {
        private List<WeeklyData> weeklyCustomers;
        private List<WeeklyData> weeklyVendors;
        private List<MonthlyData> monthlyCustomers;
        private List<MonthlyData> monthlyVendors;
        
        public CustomerVendorAnalysisDTO(List<WeeklyData> weeklyCustomers, 
                                        List<WeeklyData> weeklyVendors,
                                        List<MonthlyData> monthlyCustomers,
                                        List<MonthlyData> monthlyVendors) {
            this.weeklyCustomers = weeklyCustomers;
            this.weeklyVendors = weeklyVendors;
            this.monthlyCustomers = monthlyCustomers;
            this.monthlyVendors = monthlyVendors;
        }
        
        public List<WeeklyData> getWeeklyCustomers() { return weeklyCustomers; }
        public List<WeeklyData> getWeeklyVendors() { return weeklyVendors; }
        public List<MonthlyData> getMonthlyCustomers() { return monthlyCustomers; }
        public List<MonthlyData> getMonthlyVendors() { return monthlyVendors; }
    }
    
    public static class WeeklyData {
        private String week;
        private BigDecimal amount;
        private List<InvoiceDetail> invoices;
        private BigDecimal expectedAmount;
        
        public WeeklyData(String week, BigDecimal amount, List<InvoiceDetail> invoices) {
            this.week = week;
            this.amount = amount;
            this.invoices = invoices != null ? new ArrayList<>(invoices) : new ArrayList<>();
            
            this.expectedAmount = this.invoices.stream()
                .map(InvoiceDetail::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            this.amount = this.expectedAmount;
        }
        
        public String getWeek() { return week; }
        public BigDecimal getAmount() { return amount; }
        public List<InvoiceDetail> getInvoices() { return invoices; }
        public BigDecimal getExpectedAmount() { return expectedAmount; }
    }
    
    public static class MonthlyData {
        private String month;
        private BigDecimal amount;
        private int count;
        
        public MonthlyData(String month, BigDecimal amount, int count) {
            this.month = month;
            this.amount = amount;
            this.count = count;
        }
        
        public String getMonth() { return month; }
        public BigDecimal getAmount() { return amount; }
        public int getCount() { return count; }
    }
    
    public static class InvoiceDetail {
        private String name;
        private String invoiceNo;
        private LocalDate dueDate;
        private BigDecimal amount;
        
        public InvoiceDetail(String name, String invoiceNo, LocalDate dueDate, BigDecimal amount) {
            this.name = name;
            this.invoiceNo = invoiceNo;
            this.dueDate = dueDate;
            this.amount = amount;
        }
        
        public String getName() { return name; }
        public String getInvoiceNo() { return invoiceNo; }
        public LocalDate getDueDate() { return dueDate; }
        public BigDecimal getAmount() { return amount; }
    }
}
