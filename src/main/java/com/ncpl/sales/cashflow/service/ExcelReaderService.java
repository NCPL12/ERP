package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.model.Invoice;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class ExcelReaderService {
    
    private static final String[] INFLOW_SHEET_NAMES = {
        "Inflow", "inflow", "Receivables", "receivables", "Sales", "sales", "Revenue", "revenue", "Invoices", "invoices"
    };
    private static final String[] OUTFLOW_SHEET_NAMES = {
        "Outflow", "outflow", "Payables", "payables", "Purchases", "purchases", "Expenses", "expenses", "Bills", "bills"
    };

    public List<Invoice> readExcelFile(String filePath) throws IOException {
        List<Invoice> invoices = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet inflowSheet = findSheet(workbook, INFLOW_SHEET_NAMES);
            Sheet outflowSheet = findSheet(workbook, OUTFLOW_SHEET_NAMES);
            
            if (inflowSheet == null || outflowSheet == null) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet == inflowSheet || sheet == outflowSheet) {
                        continue;
                    }
                    if (!isInvoiceSheet(sheet)) {
                        continue;
                    }
                    String sheetName = sheet.getSheetName().toLowerCase(Locale.ENGLISH);
                    if (inflowSheet == null && isInflowSheetName(sheetName)) {
                        inflowSheet = sheet;
                        continue;
                    }
                    if (outflowSheet == null && isOutflowSheetName(sheetName)) {
                        outflowSheet = sheet;
                        continue;
                    }
                    if (inflowSheet == null) {
                        inflowSheet = sheet;
                        continue;
                    }
                    if (outflowSheet == null) {
                        outflowSheet = sheet;
                    }
                }
            }
            
            if (inflowSheet != null) {
                invoices.addAll(readSheet(inflowSheet, Invoice.InvoiceType.INFLOW));
            }
            if (outflowSheet != null) {
                invoices.addAll(readSheet(outflowSheet, Invoice.InvoiceType.OUTFLOW));
            }
        }
        
        if (invoices.isEmpty()) {
            throw new IOException("No valid invoice data found in Excel file. Expected sheets: Inflow/Outflow (or inflow/outflow/Receivables/Payables)");
        }

        return invoices;
    }
    
    private Sheet findSheet(Workbook workbook, String[] sheetNames) {
        for (String name : sheetNames) {
            Sheet sheet = workbook.getSheet(name);
            if (sheet != null) {
                return sheet;
            }
        }
        return null;
    }

    private boolean isInflowSheetName(String sheetName) {
        return sheetName.contains("inflow") || sheetName.contains("receiv") || sheetName.contains("sale") || sheetName.contains("revenue") || sheetName.contains("invoice");
    }

    private boolean isOutflowSheetName(String sheetName) {
        return sheetName.contains("outflow") || sheetName.contains("payable") || sheetName.contains("purchase") || sheetName.contains("expense") || sheetName.contains("bill");
    }

    private boolean isInvoiceSheet(Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) {
            return false;
        }
        boolean hasInvoice = false;
        boolean hasCustomer = false;
        boolean hasAmount = false;

        for (int i = 0; i < header.getLastCellNum(); i++) {
            Cell cell = header.getCell(i);
            String text = getCellValueAsString(cell).toLowerCase(Locale.ENGLISH);
            if (text.contains("invoice")) {
                hasInvoice = true;
            }
            if (text.contains("customer") || text.contains("vendor")) {
                hasCustomer = true;
            }
            if (text.contains("amount") || text.contains("value") || text.contains("total")) {
                hasAmount = true;
            }
        }
        return hasInvoice && hasCustomer && hasAmount;
    }

    private List<Invoice> readSheet(Sheet sheet, Invoice.InvoiceType type) {
        List<Invoice> invoices = new ArrayList<>();
        double totalValue = 0.0;
        int skippedRows = 0;
        int emptyCustomerName = 0;
        int invalidDate = 0;
        int zeroOrNegativeValue = 0;
        
        System.out.println("\n=== Processing " + type + " sheet ===");
        System.out.println("Total rows in sheet: " + sheet.getLastRowNum());
        
        // Skip header row
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                System.out.println("Skipping empty row: " + i);
                skippedRows++;
                continue;
            }
            
            try {
                String customerName = getCellValueAsString(row.getCell(0));
                String invoiceNumber = getCellValueAsString(row.getCell(1));
                LocalDate invoiceDate = getCellValueAsDate(row.getCell(2));
                LocalDate dueDate = getCellValueAsDate(row.getCell(3));
                double invoiceValue = getCellValueAsDouble(row.getCell(4));
                
                // Log the row being processed with all values
                System.out.printf("\nRow %d - Customer: %s, Invoice: %s, Date: %s, Due: %s, Value: %,.2f%n",
                    i, customerName, invoiceNumber, invoiceDate, dueDate, invoiceValue);
                
                // Validate customer name
                if (customerName == null || customerName.trim().isEmpty()) {
                    System.out.println("Skipping row " + i + ": Empty customer name");
                    emptyCustomerName++;
                    skippedRows++;
                    continue;
                }
                
                // Validate invoice value
                if (invoiceValue <= 0) {
                    System.out.println("Warning: Row " + i + " has invalid invoice value: " + invoiceValue);
                    zeroOrNegativeValue++;
                }
                
                // Validate dates
                if (invoiceDate == null || dueDate == null) {
                    System.out.println("Warning: Row " + i + " has missing date(s). Invoice: " + invoiceDate + ", Due: " + dueDate);
                    invalidDate++;
                }
                
                Invoice invoice = new Invoice(customerName, invoiceNumber, 
                                          invoiceDate, dueDate, invoiceValue, type);
                invoices.add(invoice);
                totalValue += invoiceValue;
                
                // Log every 10 rows to track progress
                if (i % 10 == 0) {
                    System.out.printf("Processed %d rows. Current total: %,.2f%n", i, totalValue);
                }
                
            } catch (Exception e) {
                System.err.println("Error reading row " + i + ": " + e.getMessage());
                e.printStackTrace();
                skippedRows++;
            }
        }
        
        System.out.println("\n=== " + type + " Summary ===");
        System.out.println("Total invoices processed: " + invoices.size());
        System.out.println("Skipped rows: " + skippedRows);
        System.out.println("  - Empty customer name: " + emptyCustomerName);
        System.out.println("  - Invalid dates: " + invalidDate);
        System.out.println("  - Zero/negative values: " + zeroOrNegativeValue);
        System.out.printf("Total %s value: %,.2f%n", type, totalValue);
        
        // Log the sum of all invoice values for verification
        double sumOfInvoices = invoices.stream().mapToDouble(Invoice::getInvoiceValue).sum();
        System.out.printf("Sum of all invoice values: %,.2f%n", sumOfInvoices);
        
        // Log a warning if the sum doesn't match the running total
        if (Math.abs(sumOfInvoices - totalValue) > 0.01) {
            System.err.printf("WARNING: Sum of invoice values (%,.2f) does not match running total (%,.2f)%n", 
                sumOfInvoices, totalValue);
        }
        
        return invoices;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            System.out.println("Cell is null, returning 0.0");
            return 0.0;
        }
        
        try {
            double value = 0.0;
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                    // Check if it's a date and handle accordingly
                    if (DateUtil.isCellDateFormatted(cell)) {
                        System.out.println("Warning: Found date in numeric cell where number expected");
                        return 0.0;
                    }
                    value = cell.getNumericCellValue();
                    break;
                    
                case Cell.CELL_TYPE_STRING:
                    String strValue = cell.getStringCellValue().trim();
                    System.out.println("Original string value: " + strValue);

                    if (strValue.isEmpty()) {
                        System.out.println("Empty string, returning 0.0");
                        return 0.0;
                    }

                    try {
                        // Handle currency symbols and other non-numeric characters
                        strValue = strValue
                            .replaceAll("[^\\d.,-]", "")  // Remove all non-numeric except .,-
                            .replaceAll("(?<=\\d),(?=\\d)", "")  // Remove commas between digits
                            .replace(" ", "");  // Remove any remaining spaces

                        // Handle Indian number format (e.g., 1,00,000.00)
                        if (strValue.matches(".*,\\d{3},.*")) {  // If there are multiple commas
                            strValue = strValue.replaceAll(",", "");
                        } else if (strValue.matches(".*,\\d{2}$")) {  // If comma is used as decimal
                            strValue = strValue.replace(".", "").replace(",", ".");
                        }

                        value = Double.parseDouble(strValue);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing number from string: " + strValue);
                        return 0.0;
                    }
                    break;
                    
                case Cell.CELL_TYPE_FORMULA:
                    try {
                        switch (cell.getCachedFormulaResultType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                                value = cell.getNumericCellValue();
                                break;
                            case Cell.CELL_TYPE_STRING:
                                String formulaStr = cell.getStringCellValue();
                                System.out.println("Formula string: " + formulaStr);
                                // Try to evaluate the formula
                                try {
                                    value = Double.parseDouble(formulaStr);
                                } catch (NumberFormatException e) {
                                    System.err.println("Could not parse formula result as number: " + formulaStr);
                                    return 0.0;
                                }
                                break;
                            default:
                                System.out.println("Unsupported formula result type");
                                return 0.0;
                        }
                    } catch (Exception e) {
                        System.err.println("Error evaluating formula: " + e.getMessage());
                        return 0.0;
                    }
                    break;
                    
                default:
                    System.out.println("Unsupported cell type: " + cell.getCellType());
                    return 0.0;
            }

            System.out.printf("Successfully parsed value: %,.2f%n", value);
            return value;

        } catch (Exception e) {
            System.err.println("Error getting cell value: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    
    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;

        try {
            // Handle FORMULA cells by evaluating them first
            int cellType = cell.getCellType();
            if (cellType == Cell.CELL_TYPE_FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }

            if (cellType == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            if (cellType == Cell.CELL_TYPE_STRING) {
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.isEmpty()) {
                    return null;
                }

                // Common date patterns
                DateTimeFormatter[] patterns = {
                    DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)
                };

                for (DateTimeFormatter pattern : patterns) {
                    try {
                        return LocalDate.parse(dateStr, pattern);
                    } catch (DateTimeParseException ignore) {
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing date from cell: " + e.getMessage());
        }

        return null;
    }
}
