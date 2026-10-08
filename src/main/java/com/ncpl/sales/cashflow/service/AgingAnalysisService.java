package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.model.Invoice;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgingAnalysisService {
    
    private static final int BUCKET_INTERVAL = 30;
    private static final int MAX_BUCKET_END = 720;
    private static final List<BucketBlueprint> BUCKET_BLUEPRINT = buildBucketBlueprint();

    private static List<BucketBlueprint> buildBucketBlueprint() {
        List<BucketBlueprint> blueprint = new ArrayList<>();
        int start = 0;
        int end = 30;
        blueprint.add(new BucketBlueprint(start, end, formatBucketLabel(start, end)));
        start = end + 1;
        while (start <= MAX_BUCKET_END) {
            end = Math.min(start + BUCKET_INTERVAL - 1, MAX_BUCKET_END);
            blueprint.add(new BucketBlueprint(start, end, formatBucketLabel(start, end)));
            start = end + 1;
        }
        blueprint.add(new BucketBlueprint(MAX_BUCKET_END + 1, Long.MAX_VALUE,
                ">" + MAX_BUCKET_END + " Days"));
        return blueprint;
    }

    private static String formatBucketLabel(long start, long end) {
        if (end == Long.MAX_VALUE) {
            return ">" + MAX_BUCKET_END + " Days";
        }
        return start + "-" + end + " Days";
    }
    
    public AgingAnalysisDTO analyzeAging(MultipartFile file) throws IOException {
        LocalDate today = LocalDate.now();
        
        List<InvoiceRecord> receivables = parseSheet(file, "Inflow");
        List<InvoiceRecord> payables = parseSheet(file, "Outflow");
        
        return buildResult(receivables, payables, today);
    }

    public AgingAnalysisDTO analyzeAging(List<Invoice> invoices) {
        LocalDate today = LocalDate.now();
        System.out.println("=== AGING SERVICE DEBUG ===");
        System.out.println("Input invoices: " + (invoices != null ? invoices.size() : "NULL"));
        
        if (invoices == null) {
            System.out.println("Invoices is NULL, returning empty result");
            return buildResult(Collections.emptyList(), Collections.emptyList(), today);
        }

        System.out.println("Processing receivables...");
        List<InvoiceRecord> receivables = invoices.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.INFLOW)
                .peek(inv -> System.out.printf("Receivable: %s, Due: %s%n", inv.getInvoiceNumber(), inv.getDueDate()))
                .map(this::toInvoiceRecord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println("Processing payables...");
        List<InvoiceRecord> payables = invoices.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.OUTFLOW)
                .peek(inv -> System.out.printf("Payable: %s, Due: %s%n", inv.getInvoiceNumber(), inv.getDueDate()))
                .map(this::toInvoiceRecord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println("Final receivables: " + receivables.size());
        System.out.println("Final payables: " + payables.size());
        
        AgingAnalysisDTO result = buildResult(receivables, payables, today);
        System.out.println("Build result completed");
        return result;
    }

    public byte[] generateDetailedReport(AgingAnalysisDTO data) throws IOException {
        if (data == null) {
            return new byte[0];
        }
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Create aging receivable report in Excel format
            createAgingReceivableReport(workbook, data.getDetails());
            // Create summary sheet
            createSummarySheet(workbook, data);
            // Create bucket sheets
            createBucketSheet(workbook, "Receivables Buckets", data.getReceivableRanges());
            createBucketSheet(workbook, "Payables Buckets", data.getPayableRanges());
            // Create detailed invoice sheet (legacy format)
            createDetailsSheet(workbook, data.getDetails());
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
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
                    
                    // Try to read branch and GST from additional columns if available
                    String branch = getCellValueAsString(row.getCell(5));
                    String gstNumber = getCellValueAsString(row.getCell(6));
                    
                    // If branch/GST not in separate columns, try to extract from customer name
                    if (branch == null || branch.isEmpty()) {
                        branch = extractBranchFromName(name);
                    }
                    
                    if (name != null && amount != null && dueDate != null) {
                        records.add(new InvoiceRecord(name, invoiceNo, invoiceDate, dueDate, amount, branch, gstNumber));
                    }
                } catch (Exception e) {
                    // Skip invalid rows
                }
            }
        }
        
        return records;
    }
    
    private AgingAnalysisDTO buildResult(List<InvoiceRecord> receivables, List<InvoiceRecord> payables, LocalDate today) {
        AgingBuckets receivablesAging = calculateAgingBuckets(receivables, today);
        AgingBuckets payablesAging = calculateAgingBuckets(payables, today);
        List<AgingBucketRange> receivableRanges = calculateExtendedBuckets(receivables, today);
        List<AgingBucketRange> payableRanges = calculateExtendedBuckets(payables, today);

        List<AgingDetail> details = new ArrayList<>();
        for (InvoiceRecord inv : receivables) {
            long daysOverdue = ChronoUnit.DAYS.between(inv.dueDate, today);
            if (daysOverdue >= 0) {
                details.add(new AgingDetail(
                        inv.name,
                        inv.invoiceNo,
                        inv.invoiceDate,
                        inv.dueDate,
                        inv.amount,
                        daysOverdue,
                        getBucket(daysOverdue),
                        inv.branch,
                        inv.gstNumber
                ));
            }
        }
        details.sort((a, b) -> Long.compare(b.daysOverdue, a.daysOverdue));

        return new AgingAnalysisDTO(receivablesAging, payablesAging, details, receivableRanges, payableRanges);
    }

    private void createBucketSheet(Workbook workbook, String sheetName, List<AgingBucketRange> ranges) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Bucket");
        header.createCell(1).setCellValue("Amount");
        header.createCell(2).setCellValue("Invoices");

        int rowIdx = 1;
        if (ranges != null) {
            for (AgingBucketRange range : ranges) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(range.getLabel());
                row.createCell(1).setCellValue(range.getAmount().doubleValue());
                row.createCell(2).setCellValue(range.getCount());
            }
        }

        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSummarySheet(Workbook workbook, AgingAnalysisDTO data) {
        Sheet sheet = workbook.createSheet("Summary");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        int rowIdx = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Aging Analysis Report");
        titleCell.setCellStyle(headerStyle);
        
        // Generated date
        rowIdx++;
        Row dateRow = sheet.createRow(rowIdx++);
        dateRow.createCell(0).setCellValue("Generated on:");
        dateRow.createCell(1).setCellValue(LocalDate.now().toString());
        
        // Receivables summary
        rowIdx++;
        Row recHeader = sheet.createRow(rowIdx++);
        Cell recHeaderCell = recHeader.createCell(0);
        recHeaderCell.setCellValue("RECEIVABLES SUMMARY");
        recHeaderCell.setCellStyle(headerStyle);
        
        if (data.getReceivables() != null) {
            AgingBuckets rec = data.getReceivables();
            createBucketSummaryRows(sheet, rec, rowIdx);
            rowIdx += 6;
        }
        
        // Payables summary
        rowIdx++;
        Row payHeader = sheet.createRow(rowIdx++);
        Cell payHeaderCell = payHeader.createCell(0);
        payHeaderCell.setCellValue("PAYABLES SUMMARY");
        payHeaderCell.setCellStyle(headerStyle);
        
        if (data.getPayables() != null) {
            AgingBuckets pay = data.getPayables();
            createBucketSummaryRows(sheet, pay, rowIdx);
        }
        
        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createBucketSummaryRows(Sheet sheet, AgingBuckets buckets, int startRow) {
        int rowIdx = startRow;
        
        Row r1 = sheet.createRow(rowIdx++);
        r1.createCell(0).setCellValue("Current (Not Due)");
        r1.createCell(1).setCellValue(buckets.getCurrent().doubleValue());
        r1.createCell(2).setCellValue(buckets.getCurrentCount() + " invoices");
        
        Row r2 = sheet.createRow(rowIdx++);
        r2.createCell(0).setCellValue("0-30 Days");
        r2.createCell(1).setCellValue(buckets.getDays030().doubleValue());
        r2.createCell(2).setCellValue(buckets.getDays030Count() + " invoices");
        
        Row r3 = sheet.createRow(rowIdx++);
        r3.createCell(0).setCellValue("31-60 Days");
        r3.createCell(1).setCellValue(buckets.getDays3160().doubleValue());
        r3.createCell(2).setCellValue(buckets.getDays3160Count() + " invoices");
        
        Row r4 = sheet.createRow(rowIdx++);
        r4.createCell(0).setCellValue("61-90 Days");
        r4.createCell(1).setCellValue(buckets.getDays6190().doubleValue());
        r4.createCell(2).setCellValue(buckets.getDays6190Count() + " invoices");
        
        Row r5 = sheet.createRow(rowIdx++);
        r5.createCell(0).setCellValue("90+ Days");
        r5.createCell(1).setCellValue(buckets.getDays90plus().doubleValue());
        r5.createCell(2).setCellValue(buckets.getDays90plusCount() + " invoices");
    }

    private void createAgingReceivableReport(Workbook workbook, List<AgingDetail> details) {
        Sheet sheet = workbook.createSheet("Aging Receivable Report");
        LocalDate reportDate = LocalDate.now();
        
        // Create styles
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerStyle.setBorderTop(CellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
        headerStyle.setBorderRight(CellStyle.BORDER_THIN);
        
        CellStyle numberStyle = workbook.createCellStyle();
        DataFormat numberFormat = workbook.createDataFormat();
        // Indian number format with comma separators
        numberStyle.setDataFormat(numberFormat.getFormat("#,##0"));
        
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        
        int rowIdx = 0;
        
        // Title row
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Neptune Controls Pvt Ltd-Aging Receivable Report");
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        // Header row
        rowIdx++;
        Row header = sheet.createRow(rowIdx++);
        String[] headers = {
            "Name of Customer", "GST Number", "Invoice No", 
            "Date (dd/mm/yyyy)", "Invoice Value", "Ageing Days", "Date",
            "0-30", "30-60", "60-90", "90-120", "120-180", "180-360", 
            "360-720", "More than 720 Days", "Total", "Balance"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        BigDecimal[] bucketTotals = new BigDecimal[8]; // 8 age buckets
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (int i = 0; i < bucketTotals.length; i++) {
            bucketTotals[i] = BigDecimal.ZERO;
        }
        
        if (details != null && !details.isEmpty()) {
            for (AgingDetail detail : details) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;
                
                // Name of Customer
                row.createCell(colIdx++).setCellValue(detail.getName() != null ? detail.getName() : "");
                
                // GST Number
                row.createCell(colIdx++).setCellValue(detail.getGstNumber() != null ? detail.getGstNumber() : "");
                
                // Invoice No
                row.createCell(colIdx++).setCellValue(detail.getInvoice() != null ? detail.getInvoice() : "");
                
                // Date (dd/mm/yyyy) - Invoice Date
                Cell dateCell = row.createCell(colIdx++);
                if (detail.getInvoiceDate() != null) {
                    dateCell.setCellValue(java.sql.Date.valueOf(detail.getInvoiceDate()));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    dateCell.setCellValue("");
                }
                
                // Invoice Value
                Cell valueCell = row.createCell(colIdx++);
                BigDecimal amount = detail.getAmount() != null ? detail.getAmount() : BigDecimal.ZERO;
                valueCell.setCellValue(amount.doubleValue());
                valueCell.setCellStyle(numberStyle);
                
                // Ageing Days
                row.createCell(colIdx++).setCellValue(detail.getDaysOverdue());
                
                // Date - Report Date
                Cell reportDateCell = row.createCell(colIdx++);
                reportDateCell.setCellValue(java.sql.Date.valueOf(reportDate));
                reportDateCell.setCellStyle(dateStyle);
                
                // Age buckets - place amount in appropriate bucket
                long days = detail.getDaysOverdue();
                int bucketIndex = getBucketIndex(days);
                
                // Initialize all bucket cells
                for (int i = 0; i < 8; i++) {
                    Cell bucketCell = row.createCell(colIdx++);
                    if (i == bucketIndex) {
                        bucketCell.setCellValue(amount.doubleValue());
                        bucketCell.setCellStyle(numberStyle);
                        bucketTotals[i] = bucketTotals[i].add(amount);
                    } else {
                        bucketCell.setCellValue("");
                    }
                }
                
                // Total
                Cell totalCell = row.createCell(colIdx++);
                totalCell.setCellValue(amount.doubleValue());
                totalCell.setCellStyle(numberStyle);
                grandTotal = grandTotal.add(amount);
                
                // Balance (payment column - empty for now)
                row.createCell(colIdx++).setCellValue("-");
            }
        }
        
        // Totals row
        rowIdx++;
        Row totalRow = sheet.createRow(rowIdx++);
        int colIdx = 0;
        
        // Empty cells for first 7 columns (removed Branch column)
        for (int i = 0; i < 7; i++) {
            totalRow.createCell(colIdx++).setCellValue("");
        }
        
        // Total for each bucket
        for (int i = 0; i < 8; i++) {
            Cell totalCell = totalRow.createCell(colIdx++);
            totalCell.setCellValue(bucketTotals[i].doubleValue());
            totalCell.setCellStyle(numberStyle);
        }
        
        // Grand Total
        Cell grandTotalCell = totalRow.createCell(colIdx++);
        grandTotalCell.setCellValue(grandTotal.doubleValue());
        grandTotalCell.setCellStyle(numberStyle);
        
        // Balance column
        totalRow.createCell(colIdx++).setCellValue("0");
        
        // Auto-size columns and set minimum width to prevent hash values
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // Set minimum width to prevent hash values (column width in 1/256th of a character width)
            if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
            // Also set maximum width to prevent extremely wide columns
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }
    
    private int getBucketIndex(long days) {
        // Age buckets: 0-30, 30-60, 60-90, 90-120, 120-180, 180-360, 360-720, More than 720 Days
        // Boundaries are inclusive on the lower end, exclusive on the upper end (except last bucket)
        if (days >= 0 && days <= 30) return 0;      // 0-30
        if (days > 30 && days <= 60) return 1;      // 30-60
        if (days > 60 && days <= 90) return 2;      // 60-90
        if (days > 90 && days <= 120) return 3;     // 90-120
        if (days > 120 && days <= 180) return 4;    // 120-180
        if (days > 180 && days <= 360) return 5;    // 180-360
        if (days > 360 && days <= 720) return 6;    // 360-720
        return 7;                                    // More than 720 Days
    }
    
    private void createDetailsSheet(Workbook workbook, List<AgingDetail> details) {
        Sheet sheet = workbook.createSheet("Overdue Invoices Details");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        
        // Create header row
        Row header = sheet.createRow(0);
        Cell h0 = header.createCell(0);
        h0.setCellValue("Customer / Vendor");
        h0.setCellStyle(headerStyle);
        
        Cell h1 = header.createCell(1);
        h1.setCellValue("Invoice Number");
        h1.setCellStyle(headerStyle);
        
        Cell h2 = header.createCell(2);
        h2.setCellValue("Due Date");
        h2.setCellStyle(headerStyle);
        
        Cell h3 = header.createCell(3);
        h3.setCellValue("Amount");
        h3.setCellStyle(headerStyle);
        
        Cell h4 = header.createCell(4);
        h4.setCellValue("Days Overdue");
        h4.setCellStyle(headerStyle);
        
        Cell h5 = header.createCell(5);
        h5.setCellValue("Aging Bucket");
        h5.setCellStyle(headerStyle);

        int rowIdx = 1;
        if (details != null && !details.isEmpty()) {
            for (AgingDetail detail : details) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(detail.getName() != null ? detail.getName() : "N/A");
                row.createCell(1).setCellValue(detail.getInvoice() != null ? detail.getInvoice() : "N/A");
                row.createCell(2).setCellValue(detail.getDueDate() != null ? detail.getDueDate().toString() : "N/A");
                row.createCell(3).setCellValue(detail.getAmount() != null ? detail.getAmount().doubleValue() : 0.0);
                row.createCell(4).setCellValue(detail.getDaysOverdue());
                row.createCell(5).setCellValue(detail.getBucket() != null ? detail.getBucket() : "N/A");
            }
        } else {
            // Add a message if no details available
            Row emptyRow = sheet.createRow(rowIdx);
            Cell emptyCell = emptyRow.createCell(0);
            emptyCell.setCellValue("No overdue invoices found");
        }

        // Auto-size all columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private AgingBuckets calculateAgingBuckets(List<InvoiceRecord> invoices, LocalDate today) {
        BigDecimal current = BigDecimal.ZERO;
        BigDecimal days030 = BigDecimal.ZERO;
        BigDecimal days3160 = BigDecimal.ZERO;
        BigDecimal days6190 = BigDecimal.ZERO;
        BigDecimal days90plus = BigDecimal.ZERO;
        
        int currentCount = 0;
        int days030Count = 0;
        int days3160Count = 0;
        int days6190Count = 0;
        int days90plusCount = 0;
        
        for (InvoiceRecord inv : invoices) {
            long daysOverdue = ChronoUnit.DAYS.between(inv.dueDate, today);
            
            if (daysOverdue < 0) {
                current = current.add(inv.amount);
                currentCount++;
            } else if (daysOverdue <= 30) {
                days030 = days030.add(inv.amount);
                days030Count++;
            } else if (daysOverdue <= 60) {
                days3160 = days3160.add(inv.amount);
                days3160Count++;
            } else if (daysOverdue <= 90) {
                days6190 = days6190.add(inv.amount);
                days6190Count++;
            } else {
                days90plus = days90plus.add(inv.amount);
                days90plusCount++;
            }
        }
        
        return new AgingBuckets(current, days030, days3160, days6190, days90plus,
                              currentCount, days030Count, days3160Count, days6190Count, days90plusCount);
    }

    private List<AgingBucketRange> calculateExtendedBuckets(List<InvoiceRecord> invoices, LocalDate today) {
        List<AgingBucketRange> ranges = BUCKET_BLUEPRINT.stream()
                .map(bp -> new AgingBucketRange(bp.label, bp.minDays, bp.maxDays))
                .collect(Collectors.toList());

        for (InvoiceRecord inv : invoices) {
            if (inv.dueDate == null) {
                continue;
            }
            long daysOverdue = ChronoUnit.DAYS.between(inv.dueDate, today);
            if (daysOverdue < 0) {
                continue;
            }
            AgingBucketRange range = findRange(ranges, daysOverdue);
            if (range != null) {
                range.add(inv.amount);
            }
        }

        return ranges;
    }

    private AgingBucketRange findRange(List<AgingBucketRange> ranges, long days) {
        for (AgingBucketRange range : ranges) {
            if (days >= range.getMinDays() && days <= range.getMaxDays()) {
                return range;
            }
        }
        return ranges.isEmpty() ? null : ranges.get(ranges.size() - 1);
    }
    
    private String getBucket(long days) {
        if (days < 0) {
            return "Current";
        }
        for (BucketBlueprint blueprint : BUCKET_BLUEPRINT) {
            if (days >= blueprint.minDays && days <= blueprint.maxDays) {
                return blueprint.label;
            }
        }
        return ">" + MAX_BUCKET_END + " Days";
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

    private String extractBranchFromName(String name) {
        if (name == null) return null;
        
        // Common branch patterns: "CUSTOMER - BANGALORE", "CUSTOMER-BLR", etc.
        String[] commonBranches = {"Bangalore", "BANGALORE", "BLR", "Mangalore", "MANGALORE", "MLR", 
                                   "Mumbai", "MUMBAI", "Delhi", "DELHI", "Chennai", "CHENNAI", 
                                   "Hyderabad", "HYDERABAD", "Pune", "PUNE"};
        
        for (String branch : commonBranches) {
            if (name.contains(branch)) {
                return branch;
            }
        }
        
        // Try to extract from pattern like "CUSTOMER - BRANCH"
        if (name.contains("-")) {
            String[] parts = name.split("-", 2);
            if (parts.length == 2) {
                String potentialBranch = parts[1].trim();
                if (potentialBranch.length() > 0 && potentialBranch.length() < 50) {
                    return potentialBranch;
                }
            }
        }
        
        return null;
    }
    
    private InvoiceRecord toInvoiceRecord(Invoice invoice) {
        if (invoice == null || invoice.getDueDate() == null) {
            return null;
        }
        // Try to extract branch from customer name if available
        String customerName = invoice.getCustomerName();
        String branch = extractBranchFromName(customerName);
        String gstNumber = null;
        
        return new InvoiceRecord(
                customerName,
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                BigDecimal.valueOf(invoice.getInvoiceValue()),
                branch,
                gstNumber
        );
    }

    private static class InvoiceRecord {
        String name;
        String invoiceNo;
        LocalDate invoiceDate;
        LocalDate dueDate;
        BigDecimal amount;
        String branch;
        String gstNumber;
        
        InvoiceRecord(String name, String invoiceNo, LocalDate invoiceDate, 
                     LocalDate dueDate, BigDecimal amount) {
            this(name, invoiceNo, invoiceDate, dueDate, amount, null, null);
        }
        
        InvoiceRecord(String name, String invoiceNo, LocalDate invoiceDate, 
                     LocalDate dueDate, BigDecimal amount, String branch, String gstNumber) {
            this.name = name;
            this.invoiceNo = invoiceNo;
            this.invoiceDate = invoiceDate;
            this.dueDate = dueDate;
            this.amount = amount;
            this.branch = branch;
            this.gstNumber = gstNumber;
        }
    }
    
    private static class BucketBlueprint {
        final long minDays;
        final long maxDays;
        final String label;
        
        BucketBlueprint(long minDays, long maxDays, String label) {
            this.minDays = minDays;
            this.maxDays = maxDays;
            this.label = label;
        }
    }
    
    // DTO Classes
    public static class AgingAnalysisDTO {
        private AgingBuckets receivables;
        private AgingBuckets payables;
        private List<AgingDetail> details;
        private List<AgingBucketRange> receivableRanges;
        private List<AgingBucketRange> payableRanges;
        
        public AgingAnalysisDTO(AgingBuckets receivables, AgingBuckets payables, 
                              List<AgingDetail> details,
                              List<AgingBucketRange> receivableRanges,
                              List<AgingBucketRange> payableRanges) {
            this.receivables = receivables;
            this.payables = payables;
            this.details = details;
            this.receivableRanges = receivableRanges;
            this.payableRanges = payableRanges;
        }
        
        public AgingBuckets getReceivables() { return receivables; }
        public AgingBuckets getPayables() { return payables; }
        public List<AgingDetail> getDetails() { return details; }
        public List<AgingBucketRange> getReceivableRanges() { return receivableRanges; }
        public List<AgingBucketRange> getPayableRanges() { return payableRanges; }
    }
    
    public static class AgingBuckets {
        private BigDecimal current;
        private BigDecimal days030;
        private BigDecimal days3160;
        private BigDecimal days6190;
        private BigDecimal days90plus;
        private int currentCount;
        private int days030Count;
        private int days3160Count;
        private int days6190Count;
        private int days90plusCount;
        
        public AgingBuckets(BigDecimal current, BigDecimal days030, BigDecimal days3160,
                          BigDecimal days6190, BigDecimal days90plus,
                          int currentCount, int days030Count, int days3160Count,
                          int days6190Count, int days90plusCount) {
            this.current = current;
            this.days030 = days030;
            this.days3160 = days3160;
            this.days6190 = days6190;
            this.days90plus = days90plus;
            this.currentCount = currentCount;
            this.days030Count = days030Count;
            this.days3160Count = days3160Count;
            this.days6190Count = days6190Count;
            this.days90plusCount = days90plusCount;
        }
        
        public BigDecimal getCurrent() { return current; }
        public BigDecimal getDays030() { return days030; }
        public BigDecimal getDays3160() { return days3160; }
        public BigDecimal getDays6190() { return days6190; }
        public BigDecimal getDays90plus() { return days90plus; }
        public int getCurrentCount() { return currentCount; }
        public int getDays030Count() { return days030Count; }
        public int getDays3160Count() { return days3160Count; }
        public int getDays6190Count() { return days6190Count; }
        public int getDays90plusCount() { return days90plusCount; }
    }
    
    public static class AgingBucketRange {
        private final String label;
        private final long minDays;
        private final long maxDays;
        private BigDecimal amount = BigDecimal.ZERO;
        private int count;
        
        public AgingBucketRange(String label, long minDays, long maxDays) {
            this.label = label;
            this.minDays = minDays;
            this.maxDays = maxDays;
        }
        
        public String getLabel() { return label; }
        public long getMinDays() { return minDays; }
        public long getMaxDays() { return maxDays; }
        public BigDecimal getAmount() { return amount; }
        public int getCount() { return count; }
        
        void add(BigDecimal value) {
            this.amount = this.amount.add(value);
            this.count++;
        }
    }
    
    public static class AgingDetail {
        private String name;
        private String invoice;
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private BigDecimal amount;
        private long daysOverdue;
        private String bucket;
        private String branch;
        private String gstNumber;
        
        public AgingDetail(String name, String invoice, LocalDate invoiceDate, LocalDate dueDate,
                         BigDecimal amount, long daysOverdue, String bucket, String branch, String gstNumber) {
            this.name = name;
            this.invoice = invoice;
            this.invoiceDate = invoiceDate;
            this.dueDate = dueDate;
            this.amount = amount;
            this.daysOverdue = daysOverdue;
            this.bucket = bucket;
            this.branch = branch;
            this.gstNumber = gstNumber;
        }
        
        public String getName() { return name; }
        public String getInvoice() { return invoice; }
        public LocalDate getInvoiceDate() { return invoiceDate; }
        public LocalDate getDueDate() { return dueDate; }
        public BigDecimal getAmount() { return amount; }
        public long getDaysOverdue() { return daysOverdue; }
        public String getBucket() { return bucket; }
        public String getBranch() { return branch; }
        public String getGstNumber() { return gstNumber; }
    }
}
