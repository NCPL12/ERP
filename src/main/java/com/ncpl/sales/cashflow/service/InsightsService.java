package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.dto.ExecutiveSummaryDTO;
import com.ncpl.sales.cashflow.dto.ExecutiveSummaryDTO.ActionItem;
import com.ncpl.sales.cashflow.dto.ExecutiveSummaryDTO.CalendarEvent;
import com.ncpl.sales.cashflow.dto.ReconciliationDTO;
import com.ncpl.sales.cashflow.dto.ReconciliationDTO.InvoiceSelectionDTO;
import com.ncpl.sales.cashflow.dto.TopPerformanceDTO;
import com.ncpl.sales.cashflow.dto.TopPerformanceDTO.EntityPerformance;
import com.ncpl.sales.cashflow.dto.TopPerformanceDTO.TrendPoint;
import com.ncpl.sales.cashflow.model.Invoice;
import com.ncpl.sales.cashflow.util.InvoiceFilterUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    public TopPerformanceDTO buildTopPerformance(List<Invoice> invoices, YearMonth month, Integer week) {
        List<Invoice> filtered = InvoiceFilterUtil.filterByMonthAndWeek(invoices, month, week);

        Map<String, EntityAccumulator> customerMap = new TreeMap<>();
        Map<String, EntityAccumulator> vendorMap = new TreeMap<>();

        for (Invoice invoice : filtered) {
            double value = invoice.getInvoiceValue();
            if (invoice.getType() == Invoice.InvoiceType.INFLOW) {
                customerMap.computeIfAbsent(safeName(invoice.getCustomerName()), k -> new EntityAccumulator())
                        .add(value);
            } else {
                vendorMap.computeIfAbsent(safeName(invoice.getCustomerName()), k -> new EntityAccumulator())
                        .add(value);
            }
        }

        double totalInflow = customerMap.values().stream().mapToDouble(EntityAccumulator::getAmount).sum();
        double totalOutflow = vendorMap.values().stream().mapToDouble(EntityAccumulator::getAmount).sum();

        TopPerformanceDTO dto = new TopPerformanceDTO();
        dto.setMonth(month);
        dto.setWeek(week);
        dto.setTotalInflow(totalInflow);
        dto.setTotalOutflow(totalOutflow);
        dto.setTopCustomers(toEntityPerformance(customerMap, totalInflow));
        dto.setTopVendors(toEntityPerformance(vendorMap, totalOutflow));
        dto.setTrend(buildTrend(filtered));
        return dto;
    }

    public ReconciliationDTO buildReconciliation(List<Invoice> invoices, YearMonth month, Integer week) {
        List<Invoice> filtered = InvoiceFilterUtil.filterByMonthAndWeek(invoices, month, week);
        return buildReconciliationFromFiltered(filtered, month, week, null, null);
    }

    public ReconciliationDTO buildReconciliation(List<Invoice> invoices, LocalDate fromDate, LocalDate toDate) {
        List<Invoice> filtered = InvoiceFilterUtil.filterByDueDateRange(invoices, fromDate, toDate);
        return buildReconciliationFromFiltered(filtered, null, null, fromDate, toDate);
    }

    private ReconciliationDTO buildReconciliationFromFiltered(List<Invoice> filtered,
                                                              YearMonth month, Integer week,
                                                              LocalDate fromDate, LocalDate toDate) {
        List<InvoiceSelectionDTO> receivables = filtered.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.INFLOW)
                .map(this::toSelection)
                .sorted(Comparator.comparing(InvoiceSelectionDTO::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        List<InvoiceSelectionDTO> payables = filtered.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.OUTFLOW)
                .map(this::toSelection)
                .sorted(Comparator.comparing(InvoiceSelectionDTO::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        ReconciliationDTO dto = new ReconciliationDTO();
        dto.setMonth(month);
        dto.setWeek(week);
        dto.setReceivables(receivables);
        dto.setPayables(payables);
        dto.setReceivablesTotal(receivables.stream().mapToDouble(InvoiceSelectionDTO::getAmount).sum());
        dto.setPayablesTotal(payables.stream().mapToDouble(InvoiceSelectionDTO::getAmount).sum());
        return dto;
    }

    public ExecutiveSummaryDTO buildExecutiveSummary(List<Invoice> invoices, YearMonth month, Integer week) {
        List<Invoice> filtered = InvoiceFilterUtil.filterByMonthAndWeek(invoices, month, week);

        double totalInflow = filtered.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.INFLOW)
                .mapToDouble(Invoice::getInvoiceValue)
                .sum();

        double totalOutflow = filtered.stream()
                .filter(inv -> inv.getType() == Invoice.InvoiceType.OUTFLOW)
                .mapToDouble(Invoice::getInvoiceValue)
                .sum();

        LocalDate today = LocalDate.now();

        List<Invoice> overdue = filtered.stream()
                .filter(inv -> inv.getDueDate() != null && inv.getDueDate().isBefore(today))
                .collect(Collectors.toList());

        List<Invoice> upcoming = filtered.stream()
                .filter(inv -> inv.getDueDate() != null
                        && !inv.getDueDate().isBefore(today)
                        && !inv.getDueDate().isAfter(today.plusDays(14)))
                .collect(Collectors.toList());

        ExecutiveSummaryDTO dto = new ExecutiveSummaryDTO();
        dto.setMonth(month);
        dto.setWeek(week);
        dto.setTotalInflow(totalInflow);
        dto.setTotalOutflow(totalOutflow);
        dto.setNetCashFlow(totalInflow - totalOutflow);
        dto.setOverdueAmount(overdue.stream().mapToDouble(Invoice::getInvoiceValue).sum());
        dto.setOverdueCount(overdue.size());
        dto.setUpcomingAmount(upcoming.stream().mapToDouble(Invoice::getInvoiceValue).sum());
        dto.setUpcomingCount(upcoming.size());
        dto.setHighlights(buildHighlights(dto, overdue, upcoming));
        dto.setActionItems(buildActionItems(overdue, upcoming, today));
        dto.setCalendarEvents(buildCalendarEvents(filtered));
        return dto;
    }

    private List<EntityPerformance> toEntityPerformance(Map<String, EntityAccumulator> map, double total) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, EntityAccumulator>comparingByValue(
                        Comparator.comparingDouble(EntityAccumulator::getAmount)).reversed())
                .limit(10)
                .map(entry -> new EntityPerformance(
                        entry.getKey(),
                        entry.getValue().getAmount(),
                        total > 0 ? entry.getValue().getAmount() / total : 0,
                        entry.getValue().getCount()))
                .collect(Collectors.toList());
    }

    private List<TrendPoint> buildTrend(List<Invoice> invoices) {
        TreeMap<LocalDate, TrendPoint> points = new TreeMap<>();

        invoices.forEach(invoice -> {
            LocalDate date = invoice.getInvoiceDate();
            if (date == null) {
                return;
            }
            TrendPoint point = points.computeIfAbsent(date, d -> new TrendPoint(d, 0, 0));
            if (invoice.getType() == Invoice.InvoiceType.INFLOW) {
                point.setInflow(point.getInflow() + invoice.getInvoiceValue());
            } else {
                point.setOutflow(point.getOutflow() + invoice.getInvoiceValue());
            }
        });

        return new ArrayList<>(points.values());
    }

    private InvoiceSelectionDTO toSelection(Invoice invoice) {
        InvoiceSelectionDTO dto = new InvoiceSelectionDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setName(safeName(invoice.getCustomerName()));
        dto.setAmount(invoice.getInvoiceValue());
        dto.setDueDate(invoice.getDueDate());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setStatus(buildStatus(invoice));
        dto.setReconciled(invoice.isReconciled());
        return dto;
    }

    private String buildStatus(Invoice invoice) {
        LocalDate dueDate = invoice.getDueDate();
        if (dueDate == null) {
            return "No due date";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        if (days < 0) {
            return "Overdue " + Math.abs(days) + " days";
        }
        if (days == 0) {
            return "Due today";
        }
        if (days <= 7) {
            return "Due in " + days + " days";
        }
        return "Due in " + days + " days";
    }

    private List<String> buildHighlights(ExecutiveSummaryDTO dto, List<Invoice> overdue, List<Invoice> upcoming) {
        List<String> highlights = new ArrayList<>();

        highlights.add(String.format(Locale.ENGLISH, "Net cash flow for the period is ₹%,.2f", dto.getNetCashFlow()));

        if (!overdue.isEmpty()) {
            double largest = overdue.stream().mapToDouble(Invoice::getInvoiceValue).max().orElse(0);
            highlights.add(String.format(Locale.ENGLISH,
                    "%d invoices are overdue totalling ₹%,.2f (largest single overdue: ₹%,.2f)",
                    overdue.size(), dto.getOverdueAmount(), largest));
        } else {
            highlights.add("No invoices are overdue for the selected period.");
        }

        if (!upcoming.isEmpty()) {
            highlights.add(String.format(Locale.ENGLISH,
                    "%d invoices fall due in the next 14 days (₹%,.2f)", upcoming.size(), dto.getUpcomingAmount()));
        }
        return highlights;
    }

    private List<ActionItem> buildActionItems(List<Invoice> overdue, List<Invoice> upcoming, LocalDate today) {
        List<ActionItem> actions = new ArrayList<>();

        overdue.stream()
                .sorted(Comparator.comparing(Invoice::getDueDate))
                .limit(5)
                .forEach(inv -> {
                    ActionItem item = new ActionItem();
                    item.setTitle("Follow up: " + safeName(inv.getCustomerName()));
                    item.setDescription("Invoice " + inv.getInvoiceNumber() + " is overdue by "
                            + ChronoUnit.DAYS.between(inv.getDueDate(), today) + " days.");
                    item.setDueDate(today.plusDays(1));
                    item.setPriority("HIGH");
                    actions.add(item);
                });

        upcoming.stream()
                .sorted(Comparator.comparing(Invoice::getDueDate))
                .limit(5)
                .forEach(inv -> {
                    ActionItem item = new ActionItem();
                    item.setTitle("Prepare: " + safeName(inv.getCustomerName()));
                    item.setDescription("Invoice " + inv.getInvoiceNumber() + " is due on "
                            + inv.getDueDate() + ".");
                    item.setDueDate(inv.getDueDate().minusDays(1));
                    item.setPriority("MEDIUM");
                    actions.add(item);
                });

        return actions;
    }

    private List<CalendarEvent> buildCalendarEvents(List<Invoice> invoices) {
        return invoices.stream()
                .filter(inv -> inv.getDueDate() != null)
                .sorted(Comparator.comparing(Invoice::getDueDate))
                .limit(50)
                .map(inv -> {
                    CalendarEvent event = new CalendarEvent();
                    event.setDate(inv.getDueDate());
                    event.setLabel(safeName(inv.getCustomerName()) + " • " + formatCurrency(inv.getInvoiceValue()));
                    event.setType(inv.getType() == Invoice.InvoiceType.INFLOW ? "RECEIVABLE" : "PAYABLE");
                    return event;
                })
                .collect(Collectors.toList());
    }

    private String safeName(String name) {
        return name == null || name.codePoints().allMatch(Character::isWhitespace) ? "Unnamed" : name.trim();
    }

    private String formatCurrency(double value) {
        return String.format(Locale.ENGLISH, "₹%,.2f", value);
    }

    private static class EntityAccumulator {
        private double amount;
        private int count;

        void add(double value) {
            amount += value;
            count++;
        }

        double getAmount() {
            return amount;
        }

        int getCount() {
            return count;
        }
    }
}

