package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.model.Invoice;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses Tally 6.2 Edit Log exports (XML format) to extract new/modified invoices.
 * 
 * Tally 6.2 Edit Log format (XML structure):
 * - VOUCHERs contain: Party Name, Voucher Number, Voucher Date, Amount, Due Date
 * - Modified On timestamp allows incremental sync
 * 
 * Usage:
 * TallyEditLogParser parser = new TallyEditLogParser();
 * List<Invoice> newInvoices = parser.parseEditLog(
 *     Paths.get("C:\\Tally\\EditLog.txt"),
 *     LocalDateTime.now().minusDays(1)
 * );
 */
public class TallyEditLogParser {
    private static final Logger logger = LoggerFactory.getLogger(TallyEditLogParser.class);
    private static final DateTimeFormatter TALLY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    /**
     * Parse Tally 6.2 Edit Log XML file and extract invoices.
     * 
     * @param editLogPath Path to Tally's exported XML file
     * @param lastSyncTime Last sync timestamp (for future incremental support)
     * @return List of Invoice objects
     * @throws IOException if file cannot be read
     */
    public List<Invoice> parseEditLog(Path editLogPath, LocalDateTime lastSyncTime) throws IOException {
        System.out.println("Tally Parser: Reading file from path: " + editLogPath.toAbsolutePath());
        List<Invoice> invoices = new ArrayList<>();
        
        // Use BufferedReader to read line by line instead of loading entire file
        try (BufferedReader reader = Files.newBufferedReader(editLogPath, StandardCharsets.UTF_16LE)) {
            StringBuilder voucherBlock = new StringBuilder();
            String line;
            boolean inVoucher = false;
            int voucherCount = 0;
            int relevantCount = 0;
            
            System.out.println("Tally Parser: Starting line-by-line parsing");
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<VOUCHER ")) {
                    inVoucher = true;
                    voucherBlock.setLength(0); // Reset
                    voucherBlock.append(line).append("\n");
                } else if (inVoucher) {
                    voucherBlock.append(line).append("\n");
                    if (line.contains("</VOUCHER>")) {
                        inVoucher = false;
                        voucherCount++;
                        
                        // Debug: print first few vouchers
                        if (voucherCount <= 3) {
                            System.out.println("Tally Parser: Found voucher " + voucherCount + ", length: " + voucherBlock.length());
                            System.out.println("Tally Parser: Voucher content start: " + voucherBlock.substring(0, Math.min(200, voucherBlock.length())));
                            String vchType = extractValue(voucherBlock.toString(), "VCHTYPE=\"([^\"]+)\"");
                            System.out.println("Tally Parser: Voucher " + voucherCount + " type: " + vchType);
                        }
                        
                        try {
                            Invoice invoice = parseVoucher(voucherBlock.toString());
                            if (invoice != null) {
                                invoices.add(invoice);
                                relevantCount++;
                            }
                        } catch (Exception e) {
                            // Log error but continue with next voucher
                            System.err.println("Failed to parse VOUCHER #" + voucherCount + ": " + e.getMessage());
                        }
                    }
                }
            }
            
            System.out.println("Tally Parser: Found " + voucherCount + " total vouchers, " + relevantCount + " relevant invoices");
        }
        
        return invoices;
    }

    /**
     * Parse a single VOUCHER XML block to extract invoice data.
     */
    private Invoice parseVoucher(String voucherXml) {
        try {
            // Extract VCHTYPE (voucher type)
            String vchType = extractValue(voucherXml, "VCHTYPE=\"([^\"]+)\"");
            
            // Only process Sales and Purchase vouchers (skip payments, journals, etc.)
            if (!isRelevantVoucherType(vchType)) {
                logger.debug("Skipping voucher type: {}", vchType);
                return null;
            }
            
            // Extract key fields
            String voucherNo = extractXmlTagValue(voucherXml, "VOUCHERNUMBER");
            if (voucherNo == null || voucherNo.isEmpty()) {
                return null;
            }
            
            String partyName = extractXmlTagValue(voucherXml, "PARTYNAME");
            if (partyName == null || partyName.isEmpty()) {
                partyName = "Unknown Party";
            }
            
            String dateStr = extractXmlTagValue(voucherXml, "DATE");
            LocalDate voucherDate = dateStr != null ? parseTallyDate(dateStr) : LocalDate.now();
            logger.info("Processing voucher date: {}", dateStr);
            
            // Extract due date or default to 30 days from invoice date
            String dueDateStr = extractXmlTagValue(voucherXml, "DUEDATE");
            LocalDate dueDate = dueDateStr != null ? parseTallyDate(dueDateStr) : voucherDate.plusDays(30);
            
            // Extract amount from ledger entries (look for positive amounts in Sales)
            double amount = extractAmount(voucherXml);
            if (amount <= 0) {
                return null; // Skip zero/negative amounts
            }
            
            // Determine invoice type
            Invoice.InvoiceType type = isOutflow(vchType) 
                ? Invoice.InvoiceType.OUTFLOW 
                : Invoice.InvoiceType.INFLOW;
            
            return new Invoice(
                partyName,
                voucherNo,
                voucherDate,
                dueDate,
                amount,
                type
            );
            
        } catch (Exception e) {
            System.err.println("Error parsing voucher: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if voucher type is relevant (Sales/Purchase invoices, not payments/journals).
     */
    private boolean isRelevantVoucherType(String vchType) {
        if (vchType == null) return false;
        String lower = vchType.toLowerCase();

        // include all invoice/ledger voucher types including payment/receipt/journal etc.
        return lower.contains("sales")
            || lower.contains("purchase")
            || lower.contains("invoice")
            || lower.contains("note")
            || lower.contains("order")
            || lower.contains("b-payment")
            || lower.contains("b-receipt")
            || lower.contains("payment")
            || lower.contains("receipt")
            || lower.contains("journal")
            || lower.contains("contra")
            || lower.contains("c-payment")
            || lower.contains("m-purchase")
            || lower.contains("stock journal")
            || lower.contains("gst journal")
            || lower.contains("credit note")
            || lower.contains("debit note")
            || lower.contains("delivery note")
            || lower.contains("receipt note");
    }

    /**
     * Check if voucher is outflow (purchases/expenses).
     */
    private boolean isOutflow(String vchType) {
        if (vchType == null) return false;
        String lower = vchType.toLowerCase();
        return lower.contains("purchase") || lower.contains("expense") 
            || lower.contains("debit note");
    }

    /**
     * Extract value from XML tag using regex.
     * Example: extractValue(xml, "VCHTYPE=\"([^\"]+)\"") returns the voucher type
     */
    private String extractValue(String xml, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(xml);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            // Silently ignore pattern errors
        }
        return null;
    }

    /**
     * Extract value from XML tag name.
     * Example: extractXmlTagValue(xml, "VOUCHERNUMBER") extracts <VOUCHERNUMBER>value</VOUCHERNUMBER>
     */
    private String extractXmlTagValue(String xml, String tagName) {
        try {
            Pattern pattern = Pattern.compile("<" + tagName + ">([^<]*)</" + tagName + ">");
            Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            // Silently ignore errors
        }
        return null;
    }

    /**
     * Extract amount from ALLLEDGERENTRIES.LIST blocks.
     * For Sales invoices, look for positive amounts (customer receivables).
     */
    private double extractAmount(String voucherXml) {
        try {
            // Find all ledger entry blocks
            Pattern ledgerPattern = Pattern.compile("<ALLLEDGERENTRIES\\.LIST>(.*?)</ALLLEDGERENTRIES\\.LIST>", Pattern.DOTALL);
            Matcher ledgerMatcher = ledgerPattern.matcher(voucherXml);
            
            double maxPositiveAmount = 0;
            double maxNegativeAmount = 0;
            
            while (ledgerMatcher.find()) {
                String ledgerBlock = ledgerMatcher.group(1);
                
                // Extract amount from this ledger entry
                String amountStr = extractXmlTagValue(ledgerBlock, "AMOUNT");
                if (amountStr != null) {
                    try {
                        double amount = Double.parseDouble(amountStr.replaceAll("[^0-9.-]", ""));
                        if (amount > 0) {
                            maxPositiveAmount = Math.max(maxPositiveAmount, amount);
                        } else if (amount < 0) {
                            maxNegativeAmount = Math.min(maxNegativeAmount, amount); // More negative
                        }
                    } catch (NumberFormatException e) {
                        // Skip invalid amounts
                    }
                }
            }
            
            // For Sales invoices, return the positive amount (customer receivable)
            // For Purchase invoices, return the absolute value of negative amount (vendor payable)
            if (maxPositiveAmount > 0) {
                return maxPositiveAmount;
            } else if (maxNegativeAmount < 0) {
                return Math.abs(maxNegativeAmount);
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting amount: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Parse date from Tally format (YYYYMMDD or dd-MMM-yyyy).
     */
    private LocalDate parseTallyDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        try {
            // Try YYYYMMDD format first (Tally's internal format)
            if (dateStr.length() == 8 && dateStr.matches("\\d{8}")) {
                int year = Integer.parseInt(dateStr.substring(0, 4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int day = Integer.parseInt(dateStr.substring(6, 8));
                return LocalDate.of(year, month, day);
            }
            // Try dd-MMM-yyyy format
            return LocalDate.parse(dateStr, TALLY_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr, ISO_DATE_FORMAT);
            } catch (DateTimeParseException e2) {
                return LocalDate.now();
            }
        }
    }

    /**
     * Parse date from Tally format (dd-MMM-yyyy).
     */
    private LocalDate parseDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr, TALLY_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr, ISO_DATE_FORMAT);
            } catch (DateTimeParseException e2) {
                return LocalDate.now();
            }
        }
    }

    /**
     * Get invoices of a specific type from the parsed list.
     */
    public List<Invoice> filterByType(List<Invoice> invoices, Invoice.InvoiceType type) {
        return invoices.stream()
                .filter(inv -> inv.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Get invoices from a specific party.
     */
    public List<Invoice> filterByParty(List<Invoice> invoices, String partyName) {
        return invoices.stream()
                .filter(inv -> inv.getCustomerName().equalsIgnoreCase(partyName))
                .collect(Collectors.toList());
    }
}
