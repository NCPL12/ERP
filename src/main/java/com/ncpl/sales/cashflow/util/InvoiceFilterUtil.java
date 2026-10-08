package com.ncpl.sales.cashflow.util;

import com.ncpl.sales.cashflow.model.Invoice;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility methods for filtering invoices by temporal dimensions. This helper is shared across
 * multiple tabs so that the filtering behaviour (month/week) remains consistent everywhere.
 */
public final class InvoiceFilterUtil {

    private InvoiceFilterUtil() {
        // Utility class
    }

    /**
     * Filters the supplied invoices by invoice date month and (optionally) week-of-month.
     *
     * @param invoices   invoices to filter (null safe)
     * @param targetMonth month to filter by, {@code null} to keep all months
     * @param weekOfMonth optional 1-based week of month using the default locale's definition.
     *                    {@code null} keeps all weeks within the month.
     * @return filtered list (never {@code null})
     */
    public static List<Invoice> filterByMonthAndWeek(List<Invoice> invoices, YearMonth targetMonth, Integer weekOfMonth) {
        if (invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        return invoices.stream()
                .filter(Objects::nonNull)
                .filter(invoice -> {
                    LocalDate invoiceDate = invoice.getInvoiceDate();
                    if (invoiceDate == null) {
                        return false;
                    }

                    if (targetMonth != null && !YearMonth.from(invoiceDate).equals(targetMonth)) {
                        return false;
                    }

                    if (weekOfMonth != null) {
                        WeekFields weekFields = WeekFields.of(Locale.getDefault());
                        int invoiceWeek = invoiceDate.get(weekFields.weekOfMonth());
                        return invoiceWeek == weekOfMonth;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public static List<Invoice> filterByDueDate(List<Invoice> invoices, YearMonth targetMonth, Integer weekOfMonth) {
        if (invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        return invoices.stream()
                .filter(Objects::nonNull)
                .filter(invoice -> {
                    LocalDate dueDate = invoice.getDueDate();
                    if (dueDate == null) {
                        return false;
                    }

                    if (targetMonth != null && !YearMonth.from(dueDate).equals(targetMonth)) {
                        return false;
                    }

                    if (weekOfMonth != null) {
                        WeekFields weekFields = WeekFields.of(Locale.getDefault());
                        int invoiceWeek = dueDate.get(weekFields.weekOfMonth());
                        return invoiceWeek == weekOfMonth;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters invoices by date range (from date to date) based on invoice date.
     *
     * @param invoices invoices to filter (null safe)
     * @param fromDate start date (inclusive), null to ignore
     * @param toDate   end date (inclusive), null to ignore
     * @return filtered list (never null)
     */
    public static List<Invoice> filterByDateRange(List<Invoice> invoices, LocalDate fromDate, LocalDate toDate) {
        if (invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        return invoices.stream()
                .filter(Objects::nonNull)
                .filter(invoice -> {
                    LocalDate invoiceDate = invoice.getInvoiceDate();
                    if (invoiceDate == null) {
                        return false;
                    }

                    if (fromDate != null && invoiceDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && invoiceDate.isAfter(toDate)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters invoices by due date range (from date to date).
     *
     * @param invoices invoices to filter (null safe)
     * @param fromDate start date (inclusive), null to ignore
     * @param toDate   end date (inclusive), null to ignore
     * @return filtered list (never null)
     */
    public static List<Invoice> filterByDueDateRange(List<Invoice> invoices, LocalDate fromDate, LocalDate toDate) {
        if (invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        return invoices.stream()
                .filter(Objects::nonNull)
                .filter(invoice -> {
                    LocalDate dueDate = invoice.getDueDate();
                    if (dueDate == null) {
                        return false;
                    }

                    if (fromDate != null && dueDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && dueDate.isAfter(toDate)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }
}

