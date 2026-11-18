package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.model.Invoice;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecurringTransactionService {

    public static class RecurringPattern {
        public String partyName;
        public String type; // INFLOW or OUTFLOW
        public int dayOfMonth;
        public double averageAmount;
        public int occurrences;
        public double confidence; // Percentage 0-100

        public RecurringPattern(String partyName, String type, int dayOfMonth, 
                              double averageAmount, int occurrences, double confidence) {
            this.partyName = partyName;
            this.type = type;
            this.dayOfMonth = dayOfMonth;
            this.averageAmount = averageAmount;
            this.occurrences = occurrences;
            this.confidence = confidence;
        }
    }

    /**
     * Identifies recurring transaction patterns in the invoice list.
     * A pattern is defined as transactions from the same party on the same day of month
     * with similar amounts (within 20% variance).
     */
    public List<RecurringPattern> identifyRecurringPatterns(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        // Group invoices by party and day of month
        Map<String, List<Invoice>> patternGroups = new HashMap<>();
        
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceDate() == null) continue;
            
            int dayOfMonth = invoice.getInvoiceDate().getDayOfMonth();
            String key = invoice.getCustomerName() + "_" + dayOfMonth;
            
            patternGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(invoice);
        }

        // Analyze patterns
        List<RecurringPattern> patterns = new ArrayList<>();
        
        for (Map.Entry<String, List<Invoice>> entry : patternGroups.entrySet()) {
            List<Invoice> group = entry.getValue();
            
            // Only consider patterns with at least 2 occurrences
            if (group.size() < 2) continue;
            
            // Check if they're from the same month (recurring monthly)
            Set<YearMonth> months = group.stream()
                .map(inv -> YearMonth.from(inv.getInvoiceDate()))
                .collect(Collectors.toSet());
            
            // Pattern must span at least 2 different months
            if (months.size() < 2) continue;
            
            // Calculate statistics
            double avgAmount = group.stream()
                .mapToDouble(Invoice::getInvoiceValue)
                .average()
                .orElse(0);
            
            // Check amount consistency (20% variance threshold)
            double minAmount = avgAmount * 0.8;
            double maxAmount = avgAmount * 1.2;
            
            long consistentCount = group.stream()
                .filter(inv -> inv.getInvoiceValue() >= minAmount && 
                              inv.getInvoiceValue() <= maxAmount)
                .count();
            
            double amountConsistency = (consistentCount * 100.0) / group.size();
            
            // Only include patterns with at least 70% amount consistency
            if (amountConsistency < 70) continue;
            
            String partyName = group.get(0).getCustomerName();
            String type = group.get(0).getType() == Invoice.InvoiceType.INFLOW ? "INFLOW" : "OUTFLOW";
            int dayOfMonth = group.get(0).getInvoiceDate().getDayOfMonth();
            
            // Confidence is based on consistency and number of occurrences
            // More occurrences = higher confidence
            // More consistent amounts = higher confidence
            double occurrenceConfidence = Math.min(100, (group.size() - 1) * 20);
            double confidence = (amountConsistency + occurrenceConfidence) / 2;
            
            // Apply sign to amount based on type
            double signedAmount = type.equals("INFLOW") ? avgAmount : -avgAmount;
            
            RecurringPattern pattern = new RecurringPattern(
                partyName,
                type,
                dayOfMonth,
                signedAmount,
                group.size(),
                confidence
            );
            
            patterns.add(pattern);
        }

        // Sort by confidence descending
        patterns.sort((a, b) -> Double.compare(b.confidence, a.confidence));
        
        return patterns;
    }
}
