package com.ncpl.sales.cashflow.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the operational invoice pipeline by joining ERP GRN/PO/SO records and
 * matching the GRN's supplier invoice number to a live Tally Purchase voucher.
 */
@Service
public class InvoicePipelineService {
    public static final String ERP_VENDOR_INVOICE_MISSING = "ERP_VENDOR_INVOICE_MISSING";
    public static final String VENDOR_NOT_BOOKED = "VENDOR_NOT_BOOKED";
    public static final String CLIENT_INVOICE_MISSING = "CLIENT_INVOICE_MISSING";
    public static final String COMPLETE = "COMPLETE";
    public static final String REVIEW_REQUIRED = "REVIEW_REQUIRED";

    private final JdbcTemplate jdbcTemplate;
    private final TallyLiveService tallyLiveService;
    private final Map<String, CachedPipeline> cache = new ConcurrentHashMap<>();
    private final Object refreshLock = new Object();
    private static final long CACHE_TTL_MILLIS = 10L * 60L * 1000L;

    public InvoicePipelineService(JdbcTemplate jdbcTemplate, TallyLiveService tallyLiveService) {
        this.jdbcTemplate = jdbcTemplate;
        this.tallyLiveService = tallyLiveService;
    }

    public Map<String, Object> load(LocalDate from, LocalDate to) throws Exception {
        String key = cacheKey(from, to);
        CachedPipeline cached = cache.get(key);
        if (cached != null && System.currentTimeMillis() - cached.createdAt < CACHE_TTL_MILLIS) {
            return cached.data;
        }
        return refresh(from, to, cached);
    }

    public void invalidateCache() {
        cache.clear();
    }

    @Scheduled(initialDelay = 15000L, fixedDelay = 300000L)
    public void warmDefaultPipeline() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(89);
        try {
            refresh(from, to, cache.get(cacheKey(from, to)));
        } catch (Exception ignored) {
            // The last successful snapshot remains available when Tally is temporarily unavailable.
        }
    }

    private Map<String, Object> refresh(LocalDate from, LocalDate to, CachedPipeline stale) throws Exception {
        synchronized (refreshLock) {
            String key = cacheKey(from, to);
            CachedPipeline current = cache.get(key);
            if (current != null && current != stale
                    && System.currentTimeMillis() - current.createdAt < CACHE_TTL_MILLIS) {
                return current.data;
            }
            try {
                Map<String, Object> data = build(from, to);
                cache.put(key, new CachedPipeline(data, System.currentTimeMillis()));
                return data;
            } catch (Exception error) {
                if (stale != null) return stale.data;
                throw error;
            }
        }
    }

    private String cacheKey(LocalDate from, LocalDate to) {
        return String.valueOf(from) + "|" + String.valueOf(to);
    }

    private Map<String, Object> build(LocalDate from, LocalDate to) throws Exception {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("From date must be on or before To date.");
        }

        List<Map<String, Object>> erpRows = jdbcTemplate.queryForList(
                "SELECT g.grn_id, DATE(g.created) grn_date, g.invoice_no vendor_invoice_no, " +
                "DATE(g.invoice_date) vendor_invoice_date, g.po_number, vp.party_name vendor_name, " +
                "si.sales_order_id so_number, cp.party_name client_name, " +
                "GROUP_CONCAT(DISTINCT inv.invoice_id ORDER BY inv.invoice_id SEPARATOR ',') client_invoice_numbers, " +
                "COALESCE((SELECT SUM(gi.amount) FROM tbl_grn_items gi WHERE gi.grn_id=g.grn_id),0) received_value " +
                "FROM tbl_grn g " +
                "LEFT JOIN tbl_purchase_order po ON po.po_number=g.po_number " +
                "LEFT JOIN tbl_party vp ON vp.id=po.party_id " +
                "LEFT JOIN tbl_purchase_items pi ON pi.po_number=g.po_number " +
                "LEFT JOIN tbl_sales_item si ON si.id=pi.sales_item_id " +
                "LEFT JOIN tbl_sales_order so ON so.id=si.sales_order_id " +
                "LEFT JOIN tbl_party cp ON cp.id=so.party_id " +
                "LEFT JOIN tbl_invoice inv ON inv.so_number=si.sales_order_id " +
                "WHERE g.archive=0 AND COALESCE(DATE(g.invoice_date),DATE(g.created)) BETWEEN ? AND ? " +
                "GROUP BY g.grn_id,g.created,g.invoice_no,g.invoice_date,g.po_number,vp.party_name,si.sales_order_id,cp.party_name " +
                "ORDER BY vp.party_name,COALESCE(g.invoice_date,g.created),g.grn_id",
                Date.valueOf(from), Date.valueOf(to));

        List<TallyLiveService.PurchaseVoucherMatch> vouchers;
        boolean tallyAvailable = true;
        String tallyWarning = null;
        try {
            vouchers = tallyLiveService.fetchPurchaseVouchers(from.minusDays(10), to.plusDays(10));
        } catch (Exception error) {
            vouchers = new ArrayList<>();
            tallyAvailable = false;
            tallyWarning = "Tally is currently unavailable. ERP records are shown, and vendor-invoice matches are marked for review until Tally reconnects.";
        }
        Map<String, List<TallyLiveService.PurchaseVoucherMatch>> exactIndex = buildExactIndex(vouchers);

        List<TallyLiveService.SalesVoucherLink> salesVouchers = new ArrayList<>();
        boolean salesAvailable = tallyAvailable;
        if (tallyAvailable) {
            try { salesVouchers = tallyLiveService.fetchSalesVoucherLinks(); }
            catch (Exception error) {
                salesAvailable = false;
                tallyWarning = "Tally Sales lookup failed. Unverified client links are marked for review; existing ERP invoice links remain available.";
            }
        }
        Map<String, List<TallyLiveService.SalesVoucherLink>> salesIndex = buildSalesIndex(salesVouchers);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Map<String, Object> row : erpRows) {
            records.add(toPipelineRecord(row, vouchers, exactIndex, tallyAvailable, salesIndex, salesAvailable));
        }
        records.sort(Comparator
                .comparing((Map<String, Object> row) -> string(row.get("vendorName")), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(row -> (LocalDate) row.get("eventDate"))
                .thenComparing(row -> string(row.get("poNumber")), String.CASE_INSENSITIVE_ORDER));

        Map<LocalDate, Map<String, Object>> periodMap = new TreeMap<>();
        for (Map<String, Object> record : records) {
            LocalDate eventDate = (LocalDate) record.get("eventDate");
            LocalDate weekStart = eventDate.with(DayOfWeek.MONDAY);
            Map<String, Object> period = periodMap.computeIfAbsent(weekStart, key -> {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("weekStart", key.toString());
                value.put("weekEnd", key.plusDays(6).toString());
                value.put("erpVendorInvoiceMissing", 0);
                value.put("vendorNotBooked", 0);
                value.put("clientInvoiceMissing", 0);
                value.put("complete", 0);
                value.put("reviewRequired", 0);
                value.put("records", new ArrayList<Map<String, Object>>());
                return value;
            });
            String status = string(record.get("status"));
            if (ERP_VENDOR_INVOICE_MISSING.equals(status)) increment(period, "erpVendorInvoiceMissing");
            else if (VENDOR_NOT_BOOKED.equals(status)) increment(period, "vendorNotBooked");
            else if (CLIENT_INVOICE_MISSING.equals(status)) increment(period, "clientInvoiceMissing");
            else if (COMPLETE.equals(status)) increment(period, "complete");
            else increment(period, "reviewRequired");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> periodRecords = (List<Map<String, Object>>) period.get("records");
            periodRecords.add(record);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("from", from.toString());
        response.put("to", to.toString());
        response.put("tallyAvailable", tallyAvailable);
        response.put("tallyPurchaseVoucherCount", vouchers.size());
        response.put("tallySalesVoucherCount", salesVouchers.size());
        response.put("tallySalesAvailable", salesAvailable);
        response.put("tallySalesWithSoReferenceCount", salesVouchers.stream()
                .filter(v -> !salesOrderReferences(v.references).isEmpty()).count());
        response.put("records", records);
        response.put("periods", new ArrayList<>(periodMap.values()));
        response.put("vendorNotBookedCount", countStatuses(records, ERP_VENDOR_INVOICE_MISSING, VENDOR_NOT_BOOKED));
        response.put("erpVendorInvoiceMissingCount", countStatuses(records, ERP_VENDOR_INVOICE_MISSING));
        response.put("tallyVendorNotBookedCount", countStatuses(records, VENDOR_NOT_BOOKED));
        response.put("clientInvoiceMissingCount", countStatuses(records, CLIENT_INVOICE_MISSING));
        response.put("completeCount", countStatuses(records, COMPLETE));
        response.put("reviewRequiredCount", countStatuses(records, REVIEW_REQUIRED));
        response.put("note", tallyWarning != null ? tallyWarning
                : "Client links use ERP invoices or an exact ERP SO number in the Tally Sales Reference, Order No. or narration. Enter the full SO number (for example SO-BLR-ELT-43637-2026). Client name or amount alone never confirms a link. Linked means an invoice exists for the SO, not that the full SO is billed or paid.");
        response.put("dataRefreshedAt", LocalDateTime.now().toString());
        return response;
    }

    private static final class CachedPipeline {
        private final Map<String, Object> data;
        private final long createdAt;

        private CachedPipeline(Map<String, Object> data, long createdAt) {
            this.data = data;
            this.createdAt = createdAt;
        }
    }

    private Map<String, Object> toPipelineRecord(
            Map<String, Object> row,
            List<TallyLiveService.PurchaseVoucherMatch> vouchers,
            Map<String, List<TallyLiveService.PurchaseVoucherMatch>> exactIndex,
            boolean tallyAvailable,
            Map<String, List<TallyLiveService.SalesVoucherLink>> salesIndex, boolean salesAvailable) {
        String invoiceNumber = string(row.get("vendor_invoice_no"));
        String vendorName = string(row.get("vendor_name"));
        LocalDate grnDate = localDate(row.get("grn_date"));
        LocalDate invoiceDate = localDate(row.get("vendor_invoice_date"));
        if (invoiceDate == null) invoiceDate = grnDate;
        String normalizedInvoice = normalize(invoiceNumber);
        List<TallyLiveService.PurchaseVoucherMatch> exactCandidates =
                normalizedInvoice.isEmpty() ? new ArrayList<>() : exactIndex.getOrDefault(normalizedInvoice, new ArrayList<>());

        TallyLiveService.PurchaseVoucherMatch match = bestCandidate(exactCandidates, vendorName, invoiceDate);
        String matchConfidence = match == null ? "NONE" : exactCandidates.size() == 1 ? "EXACT" : "EXACT_DISAMBIGUATED";
        TallyLiveService.PurchaseVoucherMatch possible = null;
        if (match == null && !normalizedInvoice.isEmpty()) {
            possible = findPossible(vouchers, normalizedInvoice, vendorName, invoiceDate);
        }

        List<String> clientInvoices = splitValues(string(row.get("client_invoice_numbers")));
        List<String> erpClientInvoices = new ArrayList<>(clientInvoices);
        List<TallyLiveService.SalesVoucherLink> clientVouchers = salesIndex.getOrDefault(
                normalize(string(row.get("so_number"))), new ArrayList<>());
        for (TallyLiveService.SalesVoucherLink voucher : clientVouchers) {
            if (!clientInvoices.contains(voucher.voucherNumber)) clientInvoices.add(voucher.voucherNumber);
        }
        String status;
        String statusLabel;
        if (normalizedInvoice.isEmpty()) {
            status = ERP_VENDOR_INVOICE_MISSING;
            statusLabel = "Vendor invoice number missing in ERP GRN";
        } else if (!tallyAvailable) {
            status = REVIEW_REQUIRED;
            statusLabel = "Tally unavailable - vendor invoice match pending";
        } else if (match == null && possible != null) {
            status = REVIEW_REQUIRED;
            statusLabel = "Possible Tally match - review required";
            match = possible;
            matchConfidence = "POSSIBLE";
        } else if (match == null) {
            status = VENDOR_NOT_BOOKED;
            statusLabel = "Vendor invoice not booked in Tally";
        } else if (clientInvoices.isEmpty() && !salesAvailable) {
            status = REVIEW_REQUIRED;
            statusLabel = "Tally Sales lookup unavailable - client link pending";
        } else if (clientInvoices.isEmpty()) {
            status = CLIENT_INVOICE_MISSING;
            statusLabel = "No client invoice linked to this ERP SO";
        } else {
            status = COMPLETE;
            statusLabel = "Vendor and client invoices linked";
        }

        LocalDate eventDate = VENDOR_NOT_BOOKED.equals(status) || ERP_VENDOR_INVOICE_MISSING.equals(status)
                ? (grnDate == null ? invoiceDate : grnDate)
                : (match != null && match.voucherDate() != null ? match.voucherDate() : invoiceDate);
        if (eventDate == null) eventDate = LocalDate.now();
        long pendingDays = COMPLETE.equals(status) ? 0L : Math.max(0L, ChronoUnit.DAYS.between(eventDate, LocalDate.now()));

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("vendorName", vendorName);
        record.put("clientName", string(row.get("client_name")));
        record.put("grnNumber", string(row.get("grn_id")));
        record.put("grnDate", grnDate);
        record.put("poNumber", string(row.get("po_number")));
        record.put("soNumber", string(row.get("so_number")));
        record.put("vendorInvoiceNumber", invoiceNumber);
        record.put("vendorInvoiceDate", invoiceDate);
        record.put("tallyVoucherNumber", match == null ? "" : match.voucherNumber());
        record.put("tallyVoucherDate", match == null ? null : match.voucherDate());
        record.put("tallyMasterId", match == null ? "" : match.masterId());
        record.put("clientInvoiceNumbers", clientInvoices);
        record.put("erpClientInvoiceNumbers", erpClientInvoices);
        record.put("tallyClientVouchers", clientVouchers);
        record.put("clientLinkSource", clientVouchers.isEmpty()
                ? (erpClientInvoices.isEmpty() ? "NONE" : "ERP_SO")
                : (erpClientInvoices.isEmpty() ? "TALLY_SO_REFERENCE" : "ERP_AND_TALLY_SO"));
        record.put("receivedValue", decimal(row.get("received_value")));
        record.put("eventDate", eventDate);
        record.put("pendingDays", pendingDays);
        record.put("status", status);
        record.put("statusLabel", statusLabel);
        record.put("matchConfidence", matchConfidence);
        return record;
    }

    private Map<String, List<TallyLiveService.SalesVoucherLink>> buildSalesIndex(
            List<TallyLiveService.SalesVoucherLink> vouchers) {
        Map<String, List<TallyLiveService.SalesVoucherLink>> index = new HashMap<>();
        for (TallyLiveService.SalesVoucherLink voucher : vouchers) {
            for (String so : salesOrderReferences(voucher.references)) {
                List<TallyLiveService.SalesVoucherLink> links = index.computeIfAbsent(so, k -> new ArrayList<>());
                if (!links.contains(voucher)) links.add(voucher);
            }
        }
        return index;
    }

    // Full, bounded identifiers only: SO ... 123 must never match SO ... 1234.
    static List<String> salesOrderReferences(List<String> references) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<![A-Z0-9-])SO[-/ ]+[A-Z0-9]+[-/ ]+[A-Z0-9]+[-/ ]+[0-9]+[-/ ]+[0-9]{4}(?![A-Z0-9-])", Pattern.CASE_INSENSITIVE);
        for (String reference : references) {
            if (reference == null) continue;
            Matcher matcher = pattern.matcher(reference);
            while (matcher.find()) {
                String key = matcher.group().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
                if (!result.contains(key)) result.add(key);
            }
        }
        return result;
    }

    private Map<String, List<TallyLiveService.PurchaseVoucherMatch>> buildExactIndex(
            List<TallyLiveService.PurchaseVoucherMatch> vouchers) {
        Map<String, List<TallyLiveService.PurchaseVoucherMatch>> result = new HashMap<>();
        for (TallyLiveService.PurchaseVoucherMatch voucher : vouchers) {
            addIndex(result, voucher.voucherNumber(), voucher);
            addIndex(result, voucher.reference(), voucher);
            for (String reference : voucher.billReferences()) addIndex(result, reference, voucher);
        }
        return result;
    }

    private void addIndex(Map<String, List<TallyLiveService.PurchaseVoucherMatch>> index,
                          String value, TallyLiveService.PurchaseVoucherMatch voucher) {
        String key = normalize(value);
        if (key.isEmpty()) return;
        List<TallyLiveService.PurchaseVoucherMatch> values = index.computeIfAbsent(key, ignored -> new ArrayList<>());
        if (!values.contains(voucher)) values.add(voucher);
    }

    private TallyLiveService.PurchaseVoucherMatch bestCandidate(
            List<TallyLiveService.PurchaseVoucherMatch> candidates, String vendor, LocalDate invoiceDate) {
        TallyLiveService.PurchaseVoucherMatch best = null;
        int bestScore = -1;
        for (TallyLiveService.PurchaseVoucherMatch candidate : candidates) {
            int score = candidateScore(candidate, vendor, invoiceDate);
            if (score > bestScore) { best = candidate; bestScore = score; }
        }
        return best;
    }

    private TallyLiveService.PurchaseVoucherMatch findPossible(
            List<TallyLiveService.PurchaseVoucherMatch> vouchers, String invoice,
            String vendor, LocalDate invoiceDate) {
        TallyLiveService.PurchaseVoucherMatch result = null;
        int bestScore = 0;
        for (TallyLiveService.PurchaseVoucherMatch voucher : vouchers) {
            String candidate = normalize(firstNonBlank(voucher.voucherNumber(), voucher.reference()));
            if (candidate.isEmpty()) continue;
            boolean closeNumber = editDistanceAtMostOne(invoice, candidate)
                    || (Math.abs(invoice.length() - candidate.length()) <= 1
                    && (invoice.contains(candidate) || candidate.contains(invoice)));
            if (!closeNumber) continue;
            int score = candidateScore(voucher, vendor, invoiceDate);
            if (score >= 3 && score > bestScore) { result = voucher; bestScore = score; }
        }
        return result;
    }

    private int candidateScore(TallyLiveService.PurchaseVoucherMatch voucher, String vendor, LocalDate invoiceDate) {
        int score = 0;
        String wantedVendor = normalize(vendor);
        String tallyVendor = normalize(voucher.partyName());
        if (!wantedVendor.isEmpty() && wantedVendor.equals(tallyVendor)) score += 5;
        else if (!wantedVendor.isEmpty() && !tallyVendor.isEmpty()
                && (wantedVendor.contains(tallyVendor) || tallyVendor.contains(wantedVendor))) score += 3;
        if (invoiceDate != null && voucher.voucherDate() != null) {
            long difference = Math.abs(ChronoUnit.DAYS.between(invoiceDate, voucher.voucherDate()));
            if (difference == 0) score += 3;
            else if (difference <= 7) score += 1;
        }
        return score;
    }

    private boolean editDistanceAtMostOne(String left, String right) {
        if (left.equals(right)) return true;
        if (Math.abs(left.length() - right.length()) > 1) return false;
        String shorter = left.length() <= right.length() ? left : right;
        String longer = left.length() <= right.length() ? right : left;
        int i = 0, j = 0, edits = 0;
        while (i < shorter.length() && j < longer.length()) {
            if (shorter.charAt(i) == longer.charAt(j)) { i++; j++; continue; }
            if (++edits > 1) return false;
            if (shorter.length() == longer.length()) { i++; j++; } else { j++; }
        }
        return edits + (j < longer.length() ? 1 : 0) <= 1;
    }

    private void increment(Map<String, Object> period, String key) {
        period.put(key, ((Number) period.get(key)).intValue() + 1);
    }

    private long countStatuses(List<Map<String, Object>> records, String... statuses) {
        long count = 0;
        for (Map<String, Object> row : records) {
            String status = string(row.get("status"));
            for (String wanted : statuses) {
                if (wanted.equals(status)) { count++; break; }
            }
        }
        return count;
    }

    private List<String> splitValues(String values) {
        List<String> result = new ArrayList<>();
        if (values == null || values.trim().isEmpty()) return result;
        for (String value : values.split(",")) if (!value.trim().isEmpty()) result.add(value.trim());
        return result;
    }

    private LocalDate localDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date) return ((Date) value).toLocalDate();
        if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime().toLocalDate();
        return LocalDate.parse(value.toString());
    }

    private BigDecimal decimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return new BigDecimal(value.toString());
    }

    private String normalize(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private String string(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.trim().isEmpty() ? first : second;
    }
}
