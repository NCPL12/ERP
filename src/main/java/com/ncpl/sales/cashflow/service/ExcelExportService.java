package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.dto.OverdueInvoiceDTO;
import com.ncpl.sales.cashflow.dto.TopPerformanceDTO;
import com.ncpl.sales.cashflow.dto.UpcomingInvoiceDTO;
import com.ncpl.sales.cashflow.model.Invoice;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public byte[] exportAdvances(List<TallyLiveService.AdvanceBalance> advances, String title) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Advances");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            String[] headers = {"Party", "Tally Ledger Group", "Classification", "Advance Reference",
                    "Reference Date", "Pending Reference Amount", "Party Advance Total",
                    "Net Party Ledger Balance", "Detection Basis"};
            Row header = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 3;
            for (TallyLiveService.AdvanceBalance advance : advances) {
                List<TallyLiveService.AdvanceReference> references = advance.advanceReferences();
                if (references == null || references.isEmpty()) references = com.ncpl.sales.cashflow.util.Java8Collections.list(
                        new TallyLiveService.AdvanceReference("", null, 0d));
                for (TallyLiveService.AdvanceReference reference : references) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(advance.partyName());
                    row.createCell(1).setCellValue(advance.ledgerGroup());
                    row.createCell(2).setCellValue(advance.type() == TallyLiveService.AdvanceType.CUSTOMER_ADVANCE_RECEIVED
                            ? "Customer advance received" : "Supplier advance paid");
                    row.createCell(3).setCellValue(reference.referenceNumber() == null ? "" : reference.referenceNumber());
                    setDateCell(row.createCell(4), reference.billDate(), dateStyle);
                    Cell referenceAmount = row.createCell(5);
                    referenceAmount.setCellValue(reference.amount());
                    referenceAmount.setCellStyle(numberStyle);
                    Cell partyTotal = row.createCell(6);
                    partyTotal.setCellValue(advance.amount());
                    partyTotal.setCellStyle(numberStyle);
                    Cell ledgerBalance = row.createCell(7);
                    ledgerBalance.setCellValue(advance.netLedgerBalance());
                    ledgerBalance.setCellStyle(numberStyle);
                    row.createCell(8).setCellValue(advance.detectionBasis());
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3200) sheet.setColumnWidth(i, 3200);
                if (sheet.getColumnWidth(i) > 14000) sheet.setColumnWidth(i, 14000);
            }
            sheet.createFreezePane(0, 3);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(2, Math.max(2, rowIndex - 1), 0, headers.length - 1));
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportOutstandingInvoices(List<Invoice> invoices, String title) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Outstanding Bills");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            Row header = sheet.createRow(2);
            String[] headers = {"Party Name", "Bill Number", "Bill Date", "Due Date", "Outstanding Amount",
                    "Status", "Days Overdue", "Days Until Due", "Aging Bucket", "Retention Amount (included in Outstanding)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 3;
            for (Invoice invoice : invoices) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(invoice.getCustomerName() != null ? invoice.getCustomerName() : "");
                row.createCell(1).setCellValue(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "");
                setDateCell(row.createCell(2), invoice.getInvoiceDate(), dateStyle);
                setDateCell(row.createCell(3), invoice.getDueDate(), dateStyle);
                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(invoice.getInvoiceValue());
                amountCell.setCellStyle(numberStyle);
                row.createCell(5).setCellValue(invoice.isOverdue() ? "Overdue" : "Current");
                row.createCell(6).setCellValue(invoice.getDaysOverdue());
                long daysUntilDue = invoice.getDaysUntilDue();
                if (daysUntilDue != Long.MAX_VALUE) row.createCell(7).setCellValue(daysUntilDue);
                else row.createCell(7).setCellValue("");
                row.createCell(8).setCellValue(invoice.getAgingBucket());
                Cell retentionCell = row.createCell(9);
                retentionCell.setCellValue(RetentionLedgerClassifier.isRetention(invoice.getCustomerName()) ? invoice.getInvoiceValue() : 0d);
                retentionCell.setCellStyle(numberStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) sheet.setColumnWidth(i, 3000);
                if (sheet.getColumnWidth(i) > 12000) sheet.setColumnWidth(i, 12000);
            }
            sheet.createFreezePane(0, 3);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /** Export the chart drill-down in the finance team's Receivable Format workbook layout. */
    public byte[] exportForecastInvoices(List<Invoice> receivables, List<Invoice> payables, String title) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            boolean created = false;
            if (receivables != null && !receivables.isEmpty()) {
                createForecastInvoiceSheet(workbook, "Receivables", receivables, title);
                created = true;
            }
            if (payables != null && !payables.isEmpty()) {
                createForecastInvoiceSheet(workbook, "Payables", payables, title);
                created = true;
            }
            if (!created) createForecastInvoiceSheet(workbook, "Receivables", java.util.Collections.emptyList(), title);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createForecastInvoiceSheet(Workbook workbook, String sheetName, List<Invoice> invoices, String title) {
        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title + " - " + sheetName);
        titleCell.setCellStyle(titleStyle);

        String[] headers = {"Name of Customer", "Invoice No", "Date (dd/mm/yyyy)", "Due Date",
                "Invoice Value", "Due", "Over Due", "Ageing Days"};
        Row header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 3;
        for (Invoice invoice : invoices) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(invoice.getCustomerName() == null ? "" : invoice.getCustomerName());
            row.createCell(1).setCellValue(invoice.getInvoiceNumber() == null ? "" : invoice.getInvoiceNumber());
            if (invoice.getInvoiceDate() != null) {
                Cell cell = row.createCell(2);
                cell.setCellValue(java.sql.Date.valueOf(invoice.getInvoiceDate()));
                cell.setCellStyle(dateStyle);
            }
            if (invoice.getDueDate() != null) {
                Cell cell = row.createCell(3);
                cell.setCellValue(java.sql.Date.valueOf(invoice.getDueDate()));
                cell.setCellStyle(dateStyle);
            }
            Cell valueCell = row.createCell(4);
            valueCell.setCellValue(invoice.getInvoiceValue());
            valueCell.setCellStyle(numberStyle);
            Cell dueCell = row.createCell(5);
            dueCell.setCellValue(invoice.isOverdue() ? 0d : invoice.getInvoiceValue());
            dueCell.setCellStyle(numberStyle);
            Cell overdueCell = row.createCell(6);
            overdueCell.setCellValue(invoice.isOverdue() ? invoice.getInvoiceValue() : 0d);
            overdueCell.setCellStyle(numberStyle);
            Long age = invoice.getBillAgeDays();
            row.createCell(7).setCellValue(age == null ? 0d : age.doubleValue());
        }
        sheet.createFreezePane(1, 3);
        if (rowIndex > 3) sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(2, rowIndex - 1, 0, headers.length - 1));
        int[] widths = {7600, 4700, 4400, 4400, 4300, 4300, 4300, 3600};
        for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);
    }

    public byte[] exportInvoicePipeline(List<Map<String, Object>> records, String title) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Invoice Pipeline");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            Cell titleCell = sheet.createRow(0).createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);

            String[] headers = {"Vendor", "Date", "GRN No.", "PO No.", "SO No.", "Client",
                    "Vendor Invoice", "Tally Voucher", "Client Invoice", "Received Value",
                    "Pending Days", "Status", "Match Confidence", "Client Link Source"};
            Row header = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowIndex = 3;
            for (Map<String, Object> record : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(value(record.get("vendorName")));
                setPipelineDate(row.createCell(1), record.get("eventDate"), dateStyle);
                row.createCell(2).setCellValue(value(record.get("grnNumber")));
                row.createCell(3).setCellValue(value(record.get("poNumber")));
                row.createCell(4).setCellValue(value(record.get("soNumber")));
                row.createCell(5).setCellValue(value(record.get("clientName")));
                row.createCell(6).setCellValue(value(record.get("vendorInvoiceNumber")));
                row.createCell(7).setCellValue(value(record.get("tallyVoucherNumber")));
                row.createCell(8).setCellValue(joinPipelineValues(record.get("clientInvoiceNumbers")));
                Cell amount = row.createCell(9);
                amount.setCellValue(number(record.get("receivedValue")));
                amount.setCellStyle(numberStyle);
                row.createCell(10).setCellValue(number(record.get("pendingDays")));
                row.createCell(11).setCellValue(value(record.get("statusLabel")));
                row.createCell(12).setCellValue(value(record.get("matchConfidence")));
                row.createCell(13).setCellValue(value(record.get("clientLinkSource")));
            }
            sheet.createFreezePane(2, 3);
            if (rowIndex > 3) sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(2, rowIndex - 1, 0, headers.length - 1));
            int[] widths = {6500, 3500, 4700, 6200, 6200, 6500, 4700, 4700, 5600, 4300, 3300, 7200, 3900, 5600};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void setPipelineDate(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) return;
        try {
            java.time.LocalDate date = value instanceof java.time.LocalDate
                    ? (java.time.LocalDate) value : java.time.LocalDate.parse(value.toString());
            cell.setCellValue(java.sql.Date.valueOf(date));
            cell.setCellStyle(dateStyle);
        } catch (Exception ignored) {
            cell.setCellValue(value.toString());
        }
    }

    private String joinPipelineValues(Object value) {
        if (value instanceof java.util.Collection) {
            return ((java.util.Collection<?>) value).stream().map(this::value)
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        return value(value);
    }

    private String value(Object value) { return value == null ? "" : value.toString(); }
    private double number(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return value == null ? 0d : Double.parseDouble(value.toString()); }
        catch (NumberFormatException ignored) { return 0d; }
    }

    public byte[] exportSiteReport(TallyLiveService.SiteProfitability report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            List<TallyLiveService.SiteAllocation> allRows = report.allocations();
            List<TallyLiveService.SiteAllocation> sales = allRows.stream()
                    .filter(row -> "REVENUE".equals(row.nature())).collect(java.util.stream.Collectors.toList());
            List<TallyLiveService.SiteAllocation> salary = allRows.stream()
                    .filter(row -> "STAFF_SALARY".equals(row.classification())
                            || "BACKEND_SALARY".equals(row.classification())).collect(java.util.stream.Collectors.toList());
            List<TallyLiveService.SiteAllocation> costs = allRows.stream()
                    .filter(row -> "EXPENSE".equals(row.nature()))
                    .filter(row -> !salary.contains(row)).collect(java.util.stream.Collectors.toList());

            Sheet pnl = workbook.createSheet("P&L");
            Row title = pnl.createRow(0);
            title.createCell(0).setCellValue(report.siteName());
            Row period = pnl.createRow(1);
            period.createCell(0).setCellValue("Period");
            period.createCell(1).setCellValue(report.fromDate() + " to " + report.toDate());
            String[] pnlHeaders = {"Particulars", "Amount"};
            Row pnlHeader = pnl.createRow(3);
            for (int i = 0; i < pnlHeaders.length; i++) {
                pnlHeader.createCell(i).setCellValue(pnlHeaders[i]);
                pnlHeader.getCell(i).setCellStyle(headerStyle);
            }
            int pnlRow = 4;
            pnlRow = addSiteSummaryRow(pnl, pnlRow, "Cost", report.debitTransactions(), numberStyle);
            pnlRow = addSiteSummaryRow(pnl, pnlRow, "Total Sales", report.creditTransactions(), numberStyle);
            pnlRow = addSiteSummaryRow(pnl, pnlRow, "Tally Cost Centre Closing Balance", report.closingBalance(), numberStyle);
            pnlRow++;
            pnlRow = addSiteSummaryRow(pnl, pnlRow, "Revenue", report.revenue(), numberStyle);
            for (TallyLiveService.SitePnlLine line : report.pnlLines()) {
                String group = line.particular().toUpperCase(java.util.Locale.ROOT);
                if (group.equals("PURCHASE ACCOUNTS") || group.equals("DIRECT EXPENSES")
                        || group.equals("INDIRECT EXPENSES")) {
                    pnlRow = addSiteSummaryRow(pnl, pnlRow, line.particular(), line.closingBalance(), numberStyle);
                }
            }
            pnlRow = addSiteSummaryRow(pnl, pnlRow, "Total Expense", report.expense(), numberStyle);
            pnlRow = addSiteSummaryRow(pnl, pnlRow, "Net Profit / (Loss)", report.profit(), numberStyle);
            Cell margin = pnl.createRow(pnlRow).createCell(0);
            margin.setCellValue("Profit Margin");
            Cell marginValue = pnl.getRow(pnlRow).createCell(1);
            marginValue.setCellValue(report.margin());
            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
            marginValue.setCellStyle(percentStyle);
            pnl.setColumnWidth(0, 9000);
            pnl.setColumnWidth(1, 5000);

            if (!costs.isEmpty()) addSiteTransactionSheet(workbook, "Costs", costs, headerStyle, numberStyle, dateStyle);
            if (!sales.isEmpty()) addSiteTransactionSheet(workbook, "Sales", sales, headerStyle, numberStyle, dateStyle);
            if (!salary.isEmpty()) addSiteTransactionSheet(workbook, "Salary", salary, headerStyle, numberStyle, dateStyle);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private int addSiteSummaryRow(Sheet sheet, int rowIndex, String label, double amount, CellStyle numberStyle) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        Cell amountCell = row.createCell(1);
        amountCell.setCellValue(amount);
        amountCell.setCellStyle(numberStyle);
        return rowIndex + 1;
    }

    private void addSiteTransactionSheet(Workbook workbook, String name,
            List<TallyLiveService.SiteAllocation> rows, CellStyle headerStyle,
            CellStyle numberStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet(name);
        String[] headers = {"Date", "Voucher", "Voucher Type", "Party / Vendor", "Ledger",
                "Cost Centre", "Classification", "Amount", "Narration"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            header.getCell(i).setCellStyle(headerStyle);
        }
        int rowIndex = 1;
        for (TallyLiveService.SiteAllocation allocation : rows) {
            Row row = sheet.createRow(rowIndex++);
            setDateCell(row.createCell(0), allocation.voucherDate(), dateStyle);
            row.createCell(1).setCellValue(firstAvailable(allocation.voucherNumber(),
                    firstAvailable(allocation.reference(), "")));
            row.createCell(2).setCellValue(firstAvailable(allocation.voucherType(), ""));
            row.createCell(3).setCellValue(firstAvailable(allocation.partyName(), ""));
            row.createCell(4).setCellValue(firstAvailable(allocation.ledgerName(), ""));
            row.createCell(5).setCellValue(firstAvailable(allocation.costCentreName(), ""));
            row.createCell(6).setCellValue(siteClassificationLabel(
                    allocation.classification(), firstAvailable(allocation.ledgerName(), allocation.costCentreName())));
            Cell amount = row.createCell(7);
            amount.setCellValue(allocation.amount());
            amount.setCellStyle(numberStyle);
            row.createCell(8).setCellValue(firstAvailable(allocation.narration(), ""));
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, headers.length - 1));
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < 3000) sheet.setColumnWidth(i, 3000);
            if (sheet.getColumnWidth(i) > 12000) sheet.setColumnWidth(i, 12000);
        }
    }

    private String firstAvailable(String value, String fallback) {
        return value == null || value.codePoints().allMatch(Character::isWhitespace) ? fallback : value;
    }

    private String siteClassificationLabel(String value, String tallyName) {
        if (value == null || value.codePoints().allMatch(Character::isWhitespace)) return firstAvailable(tallyName, "Other Expenses");
        switch (value) {
            case "BILLABLE": return "Billable Purchase";
            case "NON_BILLABLE": return "Non-Billable Purchase";
            case "TRANSPORTATION": return "Material Transportation";
            case "MATERIAL": return "Material";
            case "SITE_EXPENSES": return "Site Expenses";
            case "RENTAL": return "Rental Charges";
            case "CONTRACT_LABOUR": return "Contract Labour Charges";
            case "REPAIRS_MAINTENANCE": return "Repairs and Maintenance";
            case "INSURANCE": return "Insurance Charges";
            case "STAFF_SALARY": return "Site / Staff Salary";
            case "BACKEND_SALARY": return "Backend Salary";
            case "REVENUE": return "Revenue";
            default: return firstAvailable(tallyName, "Other Expenses");
        }
    }

    private void setDateCell(Cell cell, java.time.LocalDate value, CellStyle dateStyle) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        cell.setCellValue(java.sql.Date.valueOf(value));
        cell.setCellStyle(dateStyle);
    }

    public byte[] exportOverdueInvoices(List<OverdueInvoiceDTO> receivables, List<OverdueInvoiceDTO> payables, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Overdue Invoices");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            int rowIdx = 0;
            
            // Header row
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"Invoice Number", "Customer/Vendor Name", "Amount", "Due Date", "Days Overdue", "Severity"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            List<OverdueInvoiceDTO> data = "receivables".equals(type) ? receivables : 
                                          "payables".equals(type) ? payables : 
                                          combineLists(receivables, payables);
            
            for (OverdueInvoiceDTO invoice : data) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;
                
                row.createCell(colIdx++).setCellValue(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "");
                row.createCell(colIdx++).setCellValue(invoice.getCustomerName() != null ? invoice.getCustomerName() : "");
                
                Cell amountCell = row.createCell(colIdx++);
                if (invoice.getAmount() != null) {
                    amountCell.setCellValue(invoice.getAmount().doubleValue());
                    amountCell.setCellStyle(numberStyle);
                } else {
                    amountCell.setCellValue(0.0);
                    amountCell.setCellStyle(numberStyle);
                }
                
                Cell dateCell = row.createCell(colIdx++);
                if (invoice.getDueDate() != null) {
                    dateCell.setCellValue(java.sql.Date.valueOf(invoice.getDueDate()));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    dateCell.setCellValue("");
                }
                
                row.createCell(colIdx++).setCellValue(invoice.getDaysOverdue());
                row.createCell(colIdx++).setCellValue(invoice.getSeverity() != null ? invoice.getSeverity() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width to prevent hash values
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    public byte[] exportDueInvoices(List<UpcomingInvoiceDTO> receivables, List<UpcomingInvoiceDTO> payables, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Due Invoices");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            int rowIdx = 0;
            
            // Header row
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"Invoice Number", "Customer/Vendor Name", "Amount", "Due Date", "Days Until Due", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            List<UpcomingInvoiceDTO> data = "receivables".equals(type) ? receivables : 
                                            "payables".equals(type) ? payables : 
                                            combineDueLists(receivables, payables);
            
            for (UpcomingInvoiceDTO invoice : data) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;
                
                row.createCell(colIdx++).setCellValue(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "");
                row.createCell(colIdx++).setCellValue(invoice.getCustomerName() != null ? invoice.getCustomerName() : "");
                
                Cell amountCell = row.createCell(colIdx++);
                if (invoice.getAmount() != null) {
                    amountCell.setCellValue(invoice.getAmount().doubleValue());
                    amountCell.setCellStyle(numberStyle);
                } else {
                    amountCell.setCellValue(0.0);
                    amountCell.setCellStyle(numberStyle);
                }
                
                Cell dateCell = row.createCell(colIdx++);
                if (invoice.getDueDate() != null) {
                    dateCell.setCellValue(java.sql.Date.valueOf(invoice.getDueDate()));
                    dateCell.setCellStyle(dateStyle);
                } else {
                    dateCell.setCellValue("");
                }
                
                row.createCell(colIdx++).setCellValue(invoice.getDaysUntilDue());
                row.createCell(colIdx++).setCellValue(invoice.getStatus() != null ? invoice.getStatus() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width to prevent hash values
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    public byte[] exportTopPerformers(TopPerformanceDTO data, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Top Performers");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            int rowIdx = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("customers".equals(type) ? "Top Customers" : "Top Vendors");
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            rowIdx++; // Skip a row
            
            // Header row
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"Rank", "Name", "Amount", "Invoice Count"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            List<TopPerformanceDTO.EntityPerformance> performers = "customers".equals(type) ? 
                data.getTopCustomers() : data.getTopVendors();
            
            int rank = 1;
            for (TopPerformanceDTO.EntityPerformance performer : performers) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;
                
                row.createCell(colIdx++).setCellValue(rank++);
                row.createCell(colIdx++).setCellValue(performer.getName() != null ? performer.getName() : "");
                
                Cell amountCell = row.createCell(colIdx++);
                amountCell.setCellValue(performer.getAmount());
                amountCell.setCellStyle(numberStyle);
                
                row.createCell(colIdx++).setCellValue(performer.getInvoiceCount());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width to prevent hash values
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return style;
    }
    
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat numberFormat = workbook.createDataFormat();
        style.setDataFormat(numberFormat.getFormat("#,##0.00"));
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        return style;
    }
    
    public byte[] exportDebtorsCreditors(List<Map<String, Object>> debtors, List<Map<String, Object>> creditors, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Debtors & Creditors");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            int rowIdx = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("debtors".equals(type) ? "Highest Debtors" : "Highest Creditors");
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            
            rowIdx++; // Skip a row
            
            // Header row
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"Rank", "Name", "Outstanding Amount", "debtors".equals(type) ? "Days Overdue" : "Days Until Due"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            List<Map<String, Object>> data = "debtors".equals(type) ? debtors : creditors;
            
            int rank = 1;
            for (Map<String, Object> entity : data) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;
                
                row.createCell(colIdx++).setCellValue(rank++);
                row.createCell(colIdx++).setCellValue(entity.get("name") != null ? entity.get("name").toString() : "");
                
                Cell amountCell = row.createCell(colIdx++);
                Object amount = entity.get("amount");
                if (amount instanceof Number) {
                    amountCell.setCellValue(((Number) amount).doubleValue());
                    amountCell.setCellStyle(numberStyle);
                } else {
                    amountCell.setCellValue(0.0);
                    amountCell.setCellStyle(numberStyle);
                }
                
                Object days = "debtors".equals(type) ? entity.get("daysOverdue") : entity.get("daysUntilDue");
                row.createCell(colIdx++).setCellValue(days instanceof Number ? ((Number) days).intValue() : 0);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Set minimum width to prevent hash values
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private List<OverdueInvoiceDTO> combineLists(List<OverdueInvoiceDTO> list1, List<OverdueInvoiceDTO> list2) {
        List<OverdueInvoiceDTO> combined = new java.util.ArrayList<>();
        if (list1 != null) combined.addAll(list1);
        if (list2 != null) combined.addAll(list2);
        return combined;
    }
    
    private List<UpcomingInvoiceDTO> combineDueLists(List<UpcomingInvoiceDTO> list1, List<UpcomingInvoiceDTO> list2) {
        List<UpcomingInvoiceDTO> combined = new java.util.ArrayList<>();
        if (list1 != null) combined.addAll(list1);
        if (list2 != null) combined.addAll(list2);
        return combined;
    }

    public byte[] exportReconciliation(List<Map<String, String>> selectedReceivables,
                                     List<Map<String, String>> selectedPayables,
                                     String adjustedBankBalance,
                                     String exportDate) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            int summaryRowIdx = 0;

            // Title
            Row titleRow = summarySheet.createRow(summaryRowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reconciliation Export Summary");
            titleCell.setCellStyle(titleStyle);

            summaryRowIdx++; // Skip a row

            // Export date
            Row dateRow = summarySheet.createRow(summaryRowIdx++);
            dateRow.createCell(0).setCellValue("Export Date:");
            dateRow.createCell(1).setCellValue(exportDate);

            summaryRowIdx++; // Skip a row

            // Adjusted Bank Balance
            Row balanceRow = summarySheet.createRow(summaryRowIdx++);
            balanceRow.createCell(0).setCellValue("Adjusted Bank Balance:");
            Cell balanceCell = balanceRow.createCell(1);
            balanceCell.setCellValue(parseCurrencyValue(adjustedBankBalance));
            balanceCell.setCellStyle(numberStyle);

            summaryRowIdx++; // Skip a row

            // Selected items count
            Row countRow = summarySheet.createRow(summaryRowIdx++);
            countRow.createCell(0).setCellValue("Selected Receivables:");
            countRow.createCell(1).setCellValue(selectedReceivables != null ? selectedReceivables.size() : 0);

            Row countRow2 = summarySheet.createRow(summaryRowIdx++);
            countRow2.createCell(0).setCellValue("Selected Payables:");
            countRow2.createCell(1).setCellValue(selectedPayables != null ? selectedPayables.size() : 0);

            // Auto-size summary columns
            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            // Selected Receivables Sheet
            if (selectedReceivables != null && !selectedReceivables.isEmpty()) {
                Sheet receivablesSheet = workbook.createSheet("Selected Receivables");

                // Header row
                Row header = receivablesSheet.createRow(0);
                String[] headers = {"Invoice", "Name", "Amount", "Due Date", "Days Overdue"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Data rows
                int rowIdx = 1;
                for (Map<String, String> receivable : selectedReceivables) {
                    Row row = receivablesSheet.createRow(rowIdx++);
                    int colIdx = 0;

                    row.createCell(colIdx++).setCellValue(getStringValue(receivable.get("invoice")));
                    row.createCell(colIdx++).setCellValue(getStringValue(receivable.get("name")));

                    Cell amountCell = row.createCell(colIdx++);
                    amountCell.setCellValue(parseCurrencyValue(receivable.get("amount")));
                    amountCell.setCellStyle(numberStyle);

                    row.createCell(colIdx++).setCellValue(getStringValue(receivable.get("dueDate")));
                    row.createCell(colIdx++).setCellValue(getStringValue(receivable.get("daysOverdue")));
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    receivablesSheet.autoSizeColumn(i);
                    if (receivablesSheet.getColumnWidth(i) < 3000) {
                        receivablesSheet.setColumnWidth(i, 3000);
                    }
                }
            }

            // Selected Payables Sheet
            if (selectedPayables != null && !selectedPayables.isEmpty()) {
                Sheet payablesSheet = workbook.createSheet("Selected Payables");

                // Header row
                Row header = payablesSheet.createRow(0);
                String[] headers = {"Invoice", "Name", "Amount", "Due Date", "Days Overdue"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Data rows
                int rowIdx = 1;
                for (Map<String, String> payable : selectedPayables) {
                    Row row = payablesSheet.createRow(rowIdx++);
                    int colIdx = 0;

                    row.createCell(colIdx++).setCellValue(getStringValue(payable.get("invoice")));
                    row.createCell(colIdx++).setCellValue(getStringValue(payable.get("name")));

                    Cell amountCell = row.createCell(colIdx++);
                    amountCell.setCellValue(parseCurrencyValue(payable.get("amount")));
                    amountCell.setCellStyle(numberStyle);

                    row.createCell(colIdx++).setCellValue(getStringValue(payable.get("dueDate")));
                    row.createCell(colIdx++).setCellValue(getStringValue(payable.get("daysOverdue")));
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    payablesSheet.autoSizeColumn(i);
                    if (payablesSheet.getColumnWidth(i) < 3000) {
                        payablesSheet.setColumnWidth(i, 3000);
                    }
                }
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private double parseCurrencyValue(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        // Remove currency symbols and commas
        String cleanValue = value.replaceAll("[₹$,\\s]", "");
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getStringValue(String value) {
        return value != null ? value : "";
    }
}
