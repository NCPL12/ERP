package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.entity.FinanceCashPlanEntryEntity;
import com.ncpl.sales.cashflow.entity.FinanceCashPlanSettingsEntity;
import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import com.ncpl.sales.cashflow.entity.RecurringTransactionEntity;
import com.ncpl.sales.cashflow.repository.FinanceCashPlanEntryRepository;
import com.ncpl.sales.cashflow.repository.FinanceCashPlanSettingsRepository;
import com.ncpl.sales.cashflow.repository.InvoiceRepository;
import com.ncpl.sales.cashflow.repository.RecurringTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FinanceCashPlanService {
    public static final String SALES_PROJECTION = "SALES_PROJECTION";
    public static final String CUSTOMER_ADVANCE = "CUSTOMER_ADVANCE";
    public static final String ONE_TIME_EXPENSE = "ONE_TIME_EXPENSE";
    public static final String MANUAL_CASH_IN = "MANUAL_CASH_IN";
    public static final String MANUAL_CASH_OUT = "MANUAL_CASH_OUT";
    private static final Set<String> ENTRY_TYPES = new HashSet<>(Arrays.asList(
            SALES_PROJECTION, CUSTOMER_ADVANCE, ONE_TIME_EXPENSE, MANUAL_CASH_IN, MANUAL_CASH_OUT));

    private final InvoiceRepository invoiceRepository;
    private final FinanceCashPlanEntryRepository entryRepository;
    private final FinanceCashPlanSettingsRepository settingsRepository;
    private final RecurringTransactionRepository recurringRepository;

    public FinanceCashPlanService(InvoiceRepository invoiceRepository,
                                  FinanceCashPlanEntryRepository entryRepository,
                                  FinanceCashPlanSettingsRepository settingsRepository,
                                  RecurringTransactionRepository recurringRepository) {
        this.invoiceRepository = invoiceRepository;
        this.entryRepository = entryRepository;
        this.settingsRepository = settingsRepository;
        this.recurringRepository = recurringRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buildPlan(YearMonth startMonth, int monthCount) {
        if (monthCount < 1 || monthCount > 12) throw new IllegalArgumentException("Months must be between 1 and 12.");
        LocalDate from = startMonth.atDay(1);
        LocalDate to = startMonth.plusMonths(monthCount - 1L).atEndOfMonth();
        List<MonthBucket> buckets = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) buckets.add(new MonthBucket(startMonth.plusMonths(i)));

        List<InvoiceEntity> tallyBills = invoiceRepository.findBySource("TALLY");
        int missingCashDate = 0;
        int overdueMovedToFirstMonth = 0;
        for (InvoiceEntity bill : tallyBills) {
            LocalDate cashDate = bill.getExpectedCashDate() != null ? bill.getExpectedCashDate() : bill.getDueDate();
            if (cashDate == null || bill.getInvoiceValue() == null) {
                missingCashDate++;
                continue;
            }
            if (cashDate.isBefore(from)) {
                cashDate = from;
                overdueMovedToFirstMonth++;
            }
            MonthBucket bucket = bucketFor(buckets, cashDate);
            if (bucket == null) continue;
            if (bill.getInvoiceType() == InvoiceEntity.InvoiceType.INFLOW) {
                bucket.tallyReceivables = bucket.tallyReceivables.add(bill.getInvoiceValue());
                bucket.receivableCount++;
            } else if (bill.getInvoiceType() == InvoiceEntity.InvoiceType.OUTFLOW) {
                bucket.tallyPayables = bucket.tallyPayables.add(bill.getInvoiceValue());
                bucket.payableCount++;
            }
        }

        List<FinanceCashPlanEntryEntity> entries = entryRepository
                .findByActiveTrueAndPlannedDateBetweenOrderByPlannedDateAsc(from, to);
        int manualEntryCount = 0;
        for (FinanceCashPlanEntryEntity entry : entries) {
            MonthBucket bucket = bucketFor(buckets, entry.getPlannedDate());
            if (bucket == null) continue;
            if (MANUAL_CASH_IN.equals(entry.getEntryType())) {
                bucket.manualCashIn = bucket.manualCashIn.add(safe(entry.getAmount()));
                manualEntryCount++;
            } else if (MANUAL_CASH_OUT.equals(entry.getEntryType())) {
                bucket.manualCashOut = bucket.manualCashOut.add(safe(entry.getAmount()));
                manualEntryCount++;
            }
        }
        List<Map<String, Object>> periods = new ArrayList<>();
        for (MonthBucket bucket : buckets) {
            BigDecimal totalCashIn = bucket.tallyReceivables.add(bucket.manualCashIn);
            BigDecimal totalCashOut = bucket.tallyPayables.add(bucket.manualCashOut);
            periods.add(bucket.toMap(totalCashIn, totalCashOut, totalCashIn.subtract(totalCashOut)));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("from", from.toString());
        response.put("to", to.toString());
        response.put("monthCount", monthCount);
        response.put("periods", periods);
        response.put("missingCashDateCount", missingCashDate);
        response.put("overdueMovedToFirstMonthCount", overdueMovedToFirstMonth);
        response.put("manualEntryCount", manualEntryCount);
        return response;
    }

    @Transactional
    public void saveManualValues(Map<YearMonth, BigDecimal[]> values) {
        if (values == null || values.isEmpty() || values.size() > 12)
            throw new IllegalArgumentException("Provide between 1 and 12 monthly values.");

        YearMonth first = Collections.min(values.keySet());
        YearMonth last = Collections.max(values.keySet());
        List<FinanceCashPlanEntryEntity> existing = entryRepository
                .findByActiveTrueAndPlannedDateBetweenOrderByPlannedDateAsc(first.atDay(1), last.atEndOfMonth());

        for (Map.Entry<YearMonth, BigDecimal[]> item : values.entrySet()) {
            BigDecimal[] amounts = item.getValue();
            if (amounts == null || amounts.length != 2)
                throw new IllegalArgumentException("Both manual cash in and manual cash out are required.");
            saveManualValue(existing, item.getKey(), MANUAL_CASH_IN, safe(amounts[0]), "Monthly manual cash in");
            saveManualValue(existing, item.getKey(), MANUAL_CASH_OUT, safe(amounts[1]), "Monthly manual cash out");
        }
    }

    private void saveManualValue(List<FinanceCashPlanEntryEntity> existing, YearMonth month, String type,
                                 BigDecimal amount, String label) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Manual amounts cannot be negative.");

        List<FinanceCashPlanEntryEntity> matches = new ArrayList<>();
        for (FinanceCashPlanEntryEntity entry : existing) {
            if (type.equals(entry.getEntryType()) && YearMonth.from(entry.getPlannedDate()).equals(month)) matches.add(entry);
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            for (FinanceCashPlanEntryEntity entry : matches) {
                entry.setActive(false);
                entryRepository.save(entry);
            }
            return;
        }

        FinanceCashPlanEntryEntity target;
        if (matches.isEmpty()) {
            target = new FinanceCashPlanEntryEntity();
            target.setEntryType(type);
            target.setActive(true);
            existing.add(target);
        } else {
            target = matches.get(0);
            for (int i = 1; i < matches.size(); i++) {
                matches.get(i).setActive(false);
                entryRepository.save(matches.get(i));
            }
        }
        target.setPartyName(label);
        target.setDescription(label);
        target.setPlannedDate(month.atDay(1));
        target.setAmount(amount);
        target.setConfidencePercent(100);
        target.setNotes(null);
        entryRepository.save(target);
    }

    @Transactional(readOnly = true)
    public List<FinanceCashPlanEntryEntity> getEntries() {
        return entryRepository.findByActiveTrueOrderByPlannedDateAsc();
    }

    @Transactional
    public FinanceCashPlanEntryEntity createEntry(FinanceCashPlanEntryEntity entry) {
        entry.setId(null);
        entry.setActive(true);
        validateEntry(entry);
        return entryRepository.save(entry);
    }

    @Transactional
    public FinanceCashPlanEntryEntity updateEntry(Long id, FinanceCashPlanEntryEntity input) {
        FinanceCashPlanEntryEntity entry = entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Finance plan entry not found."));
        entry.setEntryType(input.getEntryType());
        entry.setPartyName(input.getPartyName());
        entry.setDescription(input.getDescription());
        entry.setPlannedDate(input.getPlannedDate());
        entry.setAmount(input.getAmount());
        entry.setConfidencePercent(input.getConfidencePercent());
        entry.setNotes(input.getNotes());
        validateEntry(entry);
        return entryRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long id) {
        FinanceCashPlanEntryEntity entry = entryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Finance plan entry not found."));
        entry.setActive(false);
        entryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInvoiceSchedule(YearMonth startMonth, int monthCount) {
        LocalDate from = startMonth.atDay(1);
        LocalDate to = startMonth.plusMonths(monthCount - 1L).atEndOfMonth();
        List<Map<String, Object>> result = new ArrayList<>();
        for (InvoiceEntity invoice : invoiceRepository.findBySource("TALLY")) {
            LocalDate effective = invoice.getExpectedCashDate() != null ? invoice.getExpectedCashDate() : invoice.getDueDate();
            if (effective != null && effective.isAfter(to)) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", invoice.getId());
            row.put("party", invoice.getCustomerName());
            row.put("invoiceNumber", invoice.getInvoiceNumber());
            row.put("invoiceDate", invoice.getInvoiceDate());
            row.put("dueDate", invoice.getDueDate());
            row.put("expectedCashDate", invoice.getExpectedCashDate());
            row.put("effectiveCashDate", effective == null || effective.isBefore(from) ? from : effective);
            row.put("amount", invoice.getInvoiceValue());
            row.put("direction", invoice.getInvoiceType());
            row.put("remarks", invoice.getCashflowRemarks());
            row.put("overdue", effective != null && effective.isBefore(from));
            result.add(row);
        }
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                LocalDate leftDate = (LocalDate) left.get("effectiveCashDate");
                LocalDate rightDate = (LocalDate) right.get("effectiveCashDate");
                if (leftDate == null && rightDate == null) return 0;
                if (leftDate == null) return 1;
                if (rightDate == null) return -1;
                return leftDate.compareTo(rightDate);
            }
        });
        return result;
    }

    @Transactional
    public InvoiceEntity updateInvoiceSchedule(Long id, LocalDate expectedCashDate, String remarks) {
        InvoiceEntity invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tally invoice not found."));
        if (!"TALLY".equals(invoice.getSource())) throw new IllegalArgumentException("Only Tally invoices can be scheduled here.");
        invoice.setExpectedCashDate(expectedCashDate);
        invoice.setCashflowRemarks(trimToNull(remarks));
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public FinanceCashPlanSettingsEntity getSettings() {
        return settingsRepository.findById(1L).orElseGet(() -> {
            FinanceCashPlanSettingsEntity settings = new FinanceCashPlanSettingsEntity();
            settings.setId(1L);
            settings.setOpeningBalance(BigDecimal.ZERO);
            settings.setMinimumCashReserve(BigDecimal.ZERO);
            return settings;
        });
    }

    @Transactional
    public FinanceCashPlanSettingsEntity saveSettings(FinanceCashPlanSettingsEntity input) {
        FinanceCashPlanSettingsEntity settings = settingsRepository.findById(1L).orElseGet(() -> {
            FinanceCashPlanSettingsEntity value = new FinanceCashPlanSettingsEntity();
            value.setId(1L);
            return value;
        });
        settings.setOpeningBalance(safe(input.getOpeningBalance()));
        settings.setMinimumCashReserve(safe(input.getMinimumCashReserve()));
        settings.setBalanceAsOf(input.getBalanceAsOf());
        return settingsRepository.save(settings);
    }

    private void validateEntry(FinanceCashPlanEntryEntity entry) {
        if (entry.getEntryType() == null || !ENTRY_TYPES.contains(entry.getEntryType()))
            throw new IllegalArgumentException("Choose a valid entry type.");
        if (trimToNull(entry.getPartyName()) == null) throw new IllegalArgumentException("Party or expense name is required.");
        entry.setPartyName(entry.getPartyName().trim());
        if (entry.getPlannedDate() == null) throw new IllegalArgumentException("Planned date is required.");
        if (entry.getAmount() == null || entry.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
        int confidence = entry.getConfidencePercent() == null ? 100 : entry.getConfidencePercent();
        if (confidence < 0 || confidence > 100) throw new IllegalArgumentException("Confidence must be between 0 and 100.");
        entry.setConfidencePercent(confidence);
        entry.setDescription(trimToNull(entry.getDescription()));
        entry.setNotes(trimToNull(entry.getNotes()));
    }

    private MonthBucket bucketFor(List<MonthBucket> buckets, LocalDate date) {
        if (date == null) return null;
        YearMonth month = YearMonth.from(date);
        for (MonthBucket bucket : buckets) if (bucket.month.equals(month)) return bucket;
        return null;
    }

    private LocalDate firstOccurrenceOnOrAfter(RecurringTransactionEntity transaction, LocalDate from) {
        LocalDate date = transaction.getNextDueDate();
        int guard = 0;
        while (date.isBefore(from) && guard++ < 1000) date = nextOccurrence(transaction, date);
        return date;
    }

    private LocalDate nextOccurrence(RecurringTransactionEntity transaction, LocalDate date) {
        String type = transaction.getType() == null ? "MONTHLY" : transaction.getType().toUpperCase(Locale.ROOT);
        if ("WEEKLY".equals(type)) return date.plusWeeks(1);
        if ("QUARTERLY".equals(type)) return safeDay(date.plusMonths(3), transaction.getDayOfMonth());
        if ("YEARLY".equals(type)) return safeDay(date.plusYears(1), transaction.getDayOfMonth());
        return safeDay(date.plusMonths(1), transaction.getDayOfMonth());
    }

    private LocalDate safeDay(LocalDate date, Integer requestedDay) {
        int day = requestedDay == null ? date.getDayOfMonth() : requestedDay;
        return date.withDayOfMonth(Math.min(Math.max(day, 1), YearMonth.from(date).lengthOfMonth()));
    }

    private BigDecimal safe(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private static final class MonthBucket {
        private final YearMonth month;
        private BigDecimal tallyReceivables = BigDecimal.ZERO;
        private BigDecimal manualCashIn = BigDecimal.ZERO;
        private BigDecimal tallyPayables = BigDecimal.ZERO;
        private BigDecimal manualCashOut = BigDecimal.ZERO;
        private int receivableCount;
        private int payableCount;

        private MonthBucket(YearMonth month) { this.month = month; }

        private Map<String, Object> toMap(BigDecimal totalCashIn, BigDecimal totalCashOut, BigDecimal net) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("month", month.toString());
            result.put("label", month.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)));
            result.put("tallyCashIn", tallyReceivables);
            result.put("manualCashIn", manualCashIn);
            result.put("totalCashIn", totalCashIn);
            result.put("tallyCashOut", tallyPayables);
            result.put("manualCashOut", manualCashOut);
            result.put("totalCashOut", totalCashOut);
            result.put("netFlow", net);
            result.put("receivableCount", receivableCount);
            result.put("payableCount", payableCount);
            return result;
        }
    }
}
