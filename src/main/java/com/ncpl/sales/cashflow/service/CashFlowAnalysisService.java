package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.dto.OverdueInvoiceDTO;
import com.ncpl.sales.cashflow.dto.OverviewAnalysisDTO;
import com.ncpl.sales.cashflow.dto.UpcomingInvoiceDTO;
import com.ncpl.sales.cashflow.model.CashFlowSummary;
import com.ncpl.sales.cashflow.model.Invoice;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CashFlowAnalysisService {

    private static final int UPCOMING_WINDOW_DAYS = 30;
    
    public CashFlowSummary analyzeCashFlow(List<Invoice> invoices) {
        System.out.println("\n=== Analyzing Cash Flow ===");
        System.out.println("Total invoices received: " + invoices.size());
        
        // Log all invoice types and their counts
        Map<Invoice.InvoiceType, Long> typeCounts = invoices.stream()
                .collect(Collectors.groupingBy(Invoice::getType, Collectors.counting()));
        typeCounts.forEach((type, count) -> 
            System.out.printf("Invoices of type %s: %d%n", type, count));
        
        // Separate inflows and outflows with detailed logging
        List<Invoice> inflows = invoices.stream()
                .filter(i -> {
                    boolean isInflow = i.getType() == Invoice.InvoiceType.INFLOW;
                    if (!isInflow) {
                        System.out.printf("Skipping non-INFLOW invoice: %s (Type: %s, Value: %,.2f)%n", 
                            i.getInvoiceNumber(), i.getType(), i.getInvoiceValue());
                    }
                    return isInflow;
                })
                .collect(Collectors.toList());
        
        List<Invoice> outflows = invoices.stream()
                .filter(i -> {
                    boolean isOutflow = i.getType() == Invoice.InvoiceType.OUTFLOW;
                    if (!isOutflow) {
                        System.out.printf("Skipping non-OUTFLOW invoice: %s (Type: %s, Value: %,.2f)%n", 
                            i.getInvoiceNumber(), i.getType(), i.getInvoiceValue());
                    }
                    return isOutflow;
                })
                .collect(Collectors.toList());
        
        // Log summary before filtering
        System.out.println("\n=== Before Filtering ===");
        System.out.printf("Total INFLOW invoices: %d, Total value: %,.2f%n", 
            inflows.size(), inflows.stream().mapToDouble(Invoice::getInvoiceValue).sum());
        System.out.printf("Total OUTFLOW invoices: %d, Total value: %,.2f%n", 
            outflows.size(), outflows.stream().mapToDouble(Invoice::getInvoiceValue).sum());
        
        // Basic statistics
        double totalInflow = inflows.stream()
            .peek(i -> System.out.printf("INFLOW - %s: %,.2f%n", i.getInvoiceNumber(), i.getInvoiceValue()))
            .mapToDouble(Invoice::getInvoiceValue)
            .sum();
            
        double totalOutflow = outflows.stream()
            .peek(i -> System.out.printf("OUTFLOW - %s: %,.2f%n", i.getInvoiceNumber(), i.getInvoiceValue()))
            .mapToDouble(Invoice::getInvoiceValue)
            .sum();
        
        System.out.println("\n=== After Processing ===");
        System.out.printf("Total INFLOW: %,.2f from %d invoices%n", totalInflow, inflows.size());
        System.out.printf("Total OUTFLOW: %,.2f from %d invoices%n", totalOutflow, outflows.size());
        
        CashFlowSummary summary = new CashFlowSummary();
        
        summary.setTotalInflow(totalInflow);
        summary.setTotalOutflow(totalOutflow);
        summary.setNetCashFlow(totalInflow - totalOutflow);
        summary.setTotalInflowInvoices(inflows.size());
        summary.setTotalOutflowInvoices(outflows.size());
        
        // Overdue analysis
        List<Invoice> overdueInflows = inflows.stream()
                .filter(Invoice::isOverdue)
                .collect(Collectors.toList());
        List<Invoice> overdueOutflows = outflows.stream()
                .filter(Invoice::isOverdue)
                .collect(Collectors.toList());
        
        summary.setOverdueInflowCount(overdueInflows.size());
        summary.setOverdueOutflowCount(overdueOutflows.size());
        summary.setOverdueInflowAmount(
                overdueInflows.stream().mapToDouble(Invoice::getInvoiceValue).sum());
        summary.setOverdueOutflowAmount(
                overdueOutflows.stream().mapToDouble(Invoice::getInvoiceValue).sum());
        
        // Monthly analysis
        summary.setMonthlyInflow(getMonthlyData(inflows));
        summary.setMonthlyOutflow(getMonthlyData(outflows));
        
        // Top customers/suppliers
        summary.setTopCustomers(getTopEntities(inflows, 10));
        summary.setTopSuppliers(getTopEntities(outflows, 10));
        
        // Aging analysis (default to RECEIVABLES_FULL mode)
        AgingSummary agingSummary = getAgingSummary(inflows, Invoice.AgingMode.RECEIVABLES_FULL);
        summary.setAgingBuckets(agingSummary.getBuckets());
        summary.setTotalOverdueAmount(agingSummary.getTotalOverdueAmount());
        summary.setOverdueInvoiceCount(agingSummary.getOverdueInvoiceCount());
        summary.setAverageDaysOverdue(agingSummary.getAverageDaysOverdue());
        summary.setMaxDaysOverdue(agingSummary.getMaxDaysOverdue());
        
        return summary;
    }
    
    private Map<String, Double> getMonthlyData(List<Invoice> invoices) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        return invoices.stream()
                .filter(i -> i.getInvoiceDate() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getInvoiceDate().format(formatter),
                        TreeMap::new,
                        Collectors.summingDouble(Invoice::getInvoiceValue)
                ));
    }
    
    private Map<String, Double> getTopEntities(List<Invoice> invoices, int limit) {
        Map<String, EntityAccumulator> accumulatorMap = new LinkedHashMap<>();

        for (Invoice invoice : invoices) {
            String key = normalizeNameForKey(invoice.getCustomerName());
            if (key.codePoints().allMatch(Character::isWhitespace)) {
                continue;
            }
            accumulatorMap
                    .computeIfAbsent(key, k -> new EntityAccumulator(normalizeDisplayName(invoice.getCustomerName())))
                    .add(invoice.getInvoiceValue());
        }

        return accumulatorMap.values().stream()
                .sorted(Comparator.comparingDouble(EntityAccumulator::getAmount).reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        EntityAccumulator::getDisplayName,
                        EntityAccumulator::getAmount,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    public AgingSummary getAgingSummary(List<Invoice> invoices, Invoice.AgingMode mode) {
        Map<String, Double> buckets = new LinkedHashMap<>();
        // Initialize all possible buckets to ensure consistent ordering
        String[] bucketNames = {
            "Not Due", "0-30 days", "31-60 days", "61-90 days", ">90 days"
        };
        for (String bucket : bucketNames) {
            buckets.put(bucket, 0.0);
        }
        
        double totalOverdueAmount = 0;
        int overdueCount = 0;
        long totalDaysOverdue = 0;
        long maxDaysOverdue = 0;
        
        for (Invoice invoice : invoices) {
            String bucket = invoice.getAgingBucket(mode);
            double amount = invoice.getInvoiceValue();
            
            // Update bucket amount
            buckets.put(bucket, buckets.get(bucket) + amount);
            
            // Update KPIs for overdue invoices
            if (invoice.isOverdue()) {
                long daysOverdue = invoice.getDaysOverdue();
                totalOverdueAmount += amount;
                totalDaysOverdue += daysOverdue;
                overdueCount++;
                maxDaysOverdue = Math.max(maxDaysOverdue, daysOverdue);
            }
        }
        
        // Calculate average days overdue (only for overdue invoices)
        double averageDaysOverdue = overdueCount > 0 ? (double) totalDaysOverdue / overdueCount : 0;
        
        // Remove Not Due bucket if in OVERDUE_ONLY mode
        if (mode == Invoice.AgingMode.OVERDUE_ONLY) {
            buckets.remove("Not Due");
        }
        
        return new AgingSummary(
            buckets,
            totalOverdueAmount,
            overdueCount,
            averageDaysOverdue,
            maxDaysOverdue
        );
    }
    
    public static class AgingSummary {
        private final Map<String, Double> buckets;
        private final double totalOverdueAmount;
        private final int overdueInvoiceCount;
        private final double averageDaysOverdue;
        private final long maxDaysOverdue;
        
        public AgingSummary(Map<String, Double> buckets, double totalOverdueAmount, 
                           int overdueInvoiceCount, double averageDaysOverdue, 
                           long maxDaysOverdue) {
            this.buckets = new LinkedHashMap<>(buckets);
            this.totalOverdueAmount = totalOverdueAmount;
            this.overdueInvoiceCount = overdueInvoiceCount;
            this.averageDaysOverdue = averageDaysOverdue;
            this.maxDaysOverdue = maxDaysOverdue;
        }
        
        public Map<String, Double> getBuckets() { return buckets; }
        public double getTotalOverdueAmount() { return totalOverdueAmount; }
        public int getOverdueInvoiceCount() { return overdueInvoiceCount; }
        public double getAverageDaysOverdue() { return averageDaysOverdue; }
        public long getMaxDaysOverdue() { return maxDaysOverdue; }
    }
    
    /**
     * Analyzes invoices for the Overview page with detailed overdue information
     */
    public OverviewAnalysisDTO analyzeForOverview(List<Invoice> invoices) {
        LocalDate currentDate = LocalDate.now();
        
        // Separate inflows and outflows
        List<Invoice> inflows = invoices.stream()
                .filter(i -> i.getType() == Invoice.InvoiceType.INFLOW)
                .collect(Collectors.toList());
        
        List<Invoice> outflows = invoices.stream()
                .filter(i -> i.getType() == Invoice.InvoiceType.OUTFLOW)
                .collect(Collectors.toList());
        
        // 1. Active Customers (only from INFLOW invoices)
        int activeCustomers = (int) inflows.stream()
                .map(inv -> normalizeNameForKey(inv.getCustomerName()))
                .filter(name -> !name.codePoints().allMatch(Character::isWhitespace))
                .distinct()
                .count();
        
        // 2. Total Inflow
        BigDecimal totalInflow = inflows.stream()
                .map(i -> BigDecimal.valueOf(i.getInvoiceValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 3. Total Outflow
        BigDecimal totalOutflow = outflows.stream()
                .map(i -> BigDecimal.valueOf(i.getInvoiceValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 4. Net Cashflow
        BigDecimal netCashflow = totalInflow.subtract(totalOutflow);
        
        // 5. Overdue Receivables
        List<OverdueInvoiceDTO> overdueReceivables = inflows.stream()
                .filter(Invoice::isOverdue)
                .map(i -> createOverdueDTO(i, currentDate))
                .sorted(Comparator.comparing(OverdueInvoiceDTO::getDaysOverdue).reversed())
                .collect(Collectors.toList());
        
        // 6. Overdue Payables
        List<OverdueInvoiceDTO> overduePayables = outflows.stream()
                .filter(Invoice::isOverdue)
                .map(i -> createOverdueDTO(i, currentDate))
                .sorted(Comparator.comparing(OverdueInvoiceDTO::getDaysOverdue).reversed())
                .collect(Collectors.toList());

        // 7. Upcoming due receivables/payables (next 30 days)
        LocalDate dueWindowEnd = currentDate.plusDays(UPCOMING_WINDOW_DAYS);

        List<UpcomingInvoiceDTO> dueReceivables = inflows.stream()
                .filter(inv -> inv.getDueDate() != null && !inv.isOverdue())
                .filter(inv -> !inv.getDueDate().isAfter(dueWindowEnd))
                .map(inv -> createUpcomingDTO(inv, currentDate))
                .sorted(Comparator.comparingLong(UpcomingInvoiceDTO::getDaysUntilDue))
                .collect(Collectors.toList());

        List<UpcomingInvoiceDTO> duePayables = outflows.stream()
                .filter(inv -> inv.getDueDate() != null && !inv.isOverdue())
                .filter(inv -> !inv.getDueDate().isAfter(dueWindowEnd))
                .map(inv -> createUpcomingDTO(inv, currentDate))
                .sorted(Comparator.comparingLong(UpcomingInvoiceDTO::getDaysUntilDue))
                .collect(Collectors.toList());
        
        // 7. Total Overdue Count
        int totalOverdueCount = overdueReceivables.size() + overduePayables.size();
        
        // 8. Collection Success Rate
        long totalInvoiceCount = invoices.size();
        long notOverdueCount = invoices.stream()
                .filter(i -> !i.isOverdue())
                .count();
        double collectionRate = totalInvoiceCount > 0 ? 
                (notOverdueCount * 100.0) / totalInvoiceCount : 100.0;
        
        OverviewAnalysisDTO overview = new OverviewAnalysisDTO(
                activeCustomers,
                totalInflow,
                totalOutflow,
                netCashflow,
                totalOverdueCount,
                collectionRate,
                overdueReceivables,
                overduePayables,
                dueReceivables,
                duePayables
        );
        
        return overview;
    }
    
    /**
     * Creates an OverdueInvoiceDTO from an Invoice
     */
    private OverdueInvoiceDTO createOverdueDTO(Invoice invoice, LocalDate currentDate) {
        long daysOverdue = ChronoUnit.DAYS.between(invoice.getDueDate(), currentDate);
        return new OverdueInvoiceDTO(
                invoice.getInvoiceNumber(),
                normalizeDisplayName(invoice.getCustomerName()),
                BigDecimal.valueOf(invoice.getInvoiceValue()),
                invoice.getDueDate(),
                daysOverdue
        );
    }

    private UpcomingInvoiceDTO createUpcomingDTO(Invoice invoice, LocalDate currentDate) {
        long daysUntilDue = ChronoUnit.DAYS.between(currentDate, invoice.getDueDate());
        return new UpcomingInvoiceDTO(
                invoice.getInvoiceNumber(),
                normalizeDisplayName(invoice.getCustomerName()),
                BigDecimal.valueOf(invoice.getInvoiceValue()),
                invoice.getDueDate(),
                Math.max(0, daysUntilDue)
        );
    }

    private String normalizeNameForKey(String name) {
        if (name == null) {
            return "";
        }
        return name.trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String name) {
        if (name == null || name.codePoints().allMatch(Character::isWhitespace)) {
            return "Unnamed";
        }
        return name.trim().replaceAll("\\s+", " ");
    }

    private static class EntityAccumulator {
        private final String displayName;
        private double amount;

        EntityAccumulator(String displayName) {
            this.displayName = displayName;
        }

        void add(double delta) {
            this.amount += delta;
        }

        String getDisplayName() {
            return displayName;
        }

        double getAmount() {
            return amount;
        }
    }
}
