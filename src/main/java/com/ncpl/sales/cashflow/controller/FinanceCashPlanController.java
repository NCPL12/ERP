package com.ncpl.sales.cashflow.controller;

import com.ncpl.sales.cashflow.entity.FinanceCashPlanEntryEntity;
import com.ncpl.sales.cashflow.entity.FinanceCashPlanSettingsEntity;
import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import com.ncpl.sales.cashflow.service.FinanceCashPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cashflow-analyzer/api/cashflow/finance-plan")
public class FinanceCashPlanController {
    private final FinanceCashPlanService service;

    public FinanceCashPlanController(FinanceCashPlanService service) { this.service = service; }

    @GetMapping("/summary")
    public ResponseEntity<?> summary(@RequestParam(required = false) String startMonth,
                                     @RequestParam(defaultValue = "3") int months) {
        try {
            return ResponseEntity.ok(service.buildPlan(parseMonth(startMonth), months));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/manual-values")
    public ResponseEntity<?> updateManualValues(@RequestBody ManualValuesRequest request) {
        try {
            if (request.getMonths() == null) throw new IllegalArgumentException("Monthly values are required.");
            Map<YearMonth, BigDecimal[]> values = new LinkedHashMap<>();
            for (ManualMonthValue item : request.getMonths()) {
                if (item.getMonth() == null || item.getMonth().trim().isEmpty())
                    throw new IllegalArgumentException("A month is required for every manual value.");
                YearMonth month = YearMonth.parse(item.getMonth());
                if (values.containsKey(month)) throw new IllegalArgumentException("Each month can appear only once.");
                values.put(month, new BigDecimal[]{zeroIfNull(item.getManualCashIn()), zeroIfNull(item.getManualCashOut())});
            }
            service.saveManualValues(values);
            return ResponseEntity.ok(success("Manual monthly values saved.", null));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/entries")
    public ResponseEntity<?> entries() {
        Map<String, Object> response = new LinkedHashMap<>();
        List<FinanceCashPlanEntryEntity> entries = service.getEntries();
        response.put("success", true);
        response.put("entries", entries);
        response.put("count", entries.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/entries")
    public ResponseEntity<?> createEntry(@RequestBody FinanceCashPlanEntryEntity entry) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(success("Finance plan entry created.", service.createEntry(entry)));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/entries/{id}")
    public ResponseEntity<?> updateEntry(@PathVariable Long id, @RequestBody FinanceCashPlanEntryEntity entry) {
        try {
            return ResponseEntity.ok(success("Finance plan entry updated.", service.updateEntry(id, entry)));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        try {
            service.deleteEntry(id);
            return ResponseEntity.ok(success("Finance plan entry removed.", null));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/invoice-schedule")
    public ResponseEntity<?> invoiceSchedule(@RequestParam(required = false) String startMonth,
                                             @RequestParam(defaultValue = "3") int months) {
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            List<Map<String, Object>> invoices = service.getInvoiceSchedule(parseMonth(startMonth), months);
            response.put("success", true);
            response.put("invoices", invoices);
            response.put("count", invoices.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @PutMapping("/invoice-schedule/{id}")
    public ResponseEntity<?> updateInvoiceSchedule(@PathVariable Long id, @RequestBody InvoiceScheduleRequest request) {
        try {
            InvoiceEntity invoice = service.updateInvoiceSchedule(id, request.getExpectedCashDate(), request.getRemarks());
            return ResponseEntity.ok(success("Expected cash date saved.", invoice));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<?> settings() { return ResponseEntity.ok(service.getSettings()); }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody FinanceCashPlanSettingsEntity settings) {
        try {
            return ResponseEntity.ok(success("Cash-plan settings saved.", service.saveSettings(settings)));
        } catch (Exception e) {
            return badRequest(e);
        }
    }

    private YearMonth parseMonth(String value) {
        return value == null || value.trim().isEmpty() ? YearMonth.now() : YearMonth.parse(value);
    }

    private BigDecimal zeroIfNull(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        if (data != null) response.put("data", data);
        return response;
    }

    private ResponseEntity<Map<String, Object>> badRequest(Exception e) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage() == null ? "Unable to process the request." : e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    public static class InvoiceScheduleRequest {
        private LocalDate expectedCashDate;
        private String remarks;
        public LocalDate getExpectedCashDate() { return expectedCashDate; }
        public void setExpectedCashDate(LocalDate expectedCashDate) { this.expectedCashDate = expectedCashDate; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    public static class ManualValuesRequest {
        private List<ManualMonthValue> months = new ArrayList<>();
        public List<ManualMonthValue> getMonths() { return months; }
        public void setMonths(List<ManualMonthValue> months) { this.months = months; }
    }

    public static class ManualMonthValue {
        private String month;
        private BigDecimal manualCashIn;
        private BigDecimal manualCashOut;
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public BigDecimal getManualCashIn() { return manualCashIn; }
        public void setManualCashIn(BigDecimal manualCashIn) { this.manualCashIn = manualCashIn; }
        public BigDecimal getManualCashOut() { return manualCashOut; }
        public void setManualCashOut(BigDecimal manualCashOut) { this.manualCashOut = manualCashOut; }
    }
}
