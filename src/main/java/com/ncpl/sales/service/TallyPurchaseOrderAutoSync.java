package com.ncpl.sales.service;

import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.repository.PurchaseRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Automatic ERP-to-Tally PO export. No emails or writes to the ERP database. */
@Service
public class TallyPurchaseOrderAutoSync {
    private static final Logger log = LoggerFactory.getLogger(TallyPurchaseOrderAutoSync.class);
    private final PurchaseRepo purchaseRepo;
    private final TallyPurchaseOrderService exporter;
    @Value("${tally.po.sync.enabled:false}") private boolean enabled;
    @Value("${tally.po.sync.from-date:}") private String fromDate = "";
    @Value("${tally.po.sync.time-zone:Asia/Kolkata}") private String timeZone = "Asia/Kolkata";
    @Value("${tally.po.sync.include-previously-exported:true}") private boolean includePreviouslyExported;
    private volatile TallyPurchaseOrderService.ExportResult lastResult;
    private volatile LocalDateTime lastCompleted;
    private volatile String lastError;
    @Value("${tally.po.sync.interval-ms:86400000}") private long intervalMs = 86400000;
    public long getIntervalMs() { return intervalMs; }
    public List<Map<String,String>> getRecords() { return exporter.getAutoSyncRecords(); }
    public TallyPurchaseOrderAutoSync(PurchaseRepo purchaseRepo, TallyPurchaseOrderService exporter) {
        this.purchaseRepo = purchaseRepo; this.exporter = exporter;
    }
    @Scheduled(initialDelayString="${tally.po.sync.initial-delay-ms:30000}",
            fixedDelayString="${tally.po.sync.interval-ms:86400000}")
    @Transactional(readOnly=true)
    public void synchronize() {
        if (!enabled || exporter.isExportRunning()) return;
        if (intervalMs < 60000) { lastError="Automatic PO sync interval must be at least 60 seconds."; return; }
        try {
            exporter.requireConfiguredCompany();
            if (fromDate.trim().isEmpty()) throw new IllegalStateException("Automatic PO sync needs a configured starting date.");
            java.time.LocalDate startingDate = java.time.LocalDate.parse(fromDate);
            java.time.ZoneId zone=java.time.ZoneId.of(timeZone);
            Set<String> previous = includePreviouslyExported ? exporter.previouslyExportedNumbers() : java.util.Collections.emptySet();
            java.time.LocalDate today=java.time.LocalDate.now(zone);
            java.util.Map<String,PurchaseOrder> candidates=new java.util.LinkedHashMap<>();
            for (PurchaseOrder po:purchaseRepo.findActiveOrdersForTally(
                    java.sql.Timestamp.from(startingDate.atStartOfDay(zone).toInstant()),
                    java.sql.Timestamp.from(today.plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1))))
                if (po != null && po.getPoNumber() != null) candidates.put(po.getPoNumber(),po);
            if (!previous.isEmpty()) {
                Set<String> found = new java.util.HashSet<>();
                for (PurchaseOrder po : purchaseRepo.findAllById(previous)) {
                    if (po == null || po.getPoNumber() == null) continue;
                    found.add(po.getPoNumber());
                    if (po.isArchive()) exporter.recordAutoSyncStatus(po.getPoNumber(), "CANCELLATION_REVIEW",
                            "PO is archived in ERP. Review whether its Tally voucher should be cancelled; no Tally deletion was made.");
                    else candidates.put(po.getPoNumber(), po);
                }
                for (String number : previous) if (!found.contains(number))
                    exporter.recordAutoSyncStatus(number, "CANCELLATION_REVIEW",
                            "Previously exported PO is no longer present in ERP. Confirm whether it was deleted before any action in Tally; no Tally deletion was made.");
            }
            List<PurchaseOrder> orders = candidates.values().stream()
                    .filter(po -> !po.isArchive() && po.getCreated() != null && !po.getCreated().toInstant().atZone(zone).toLocalDate().isAfter(today))
                    .filter(po -> previous.contains(po.getPoNumber()) || (po.getCreated() != null && !java.time.Instant.ofEpochMilli(po.getCreated().getTime())
                            .atZone(zone).toLocalDate().isBefore(startingDate)))
                    .filter(po -> po.getPoNumber() != null)
                    .filter(po -> {
                        String number=po.getPoNumber().trim().toUpperCase(java.util.Locale.ENGLISH);
                        return !number.startsWith("CODEX-") && !number.startsWith("ERP-PO-TEST")
                                && !number.startsWith("ERP-PO-VERIFY") && !number.startsWith("ERP-PO-ACCESS");
                    }).collect(Collectors.toList());
            lastResult = exporter.exportChangedOrders(orders);
            lastCompleted = LocalDateTime.now(java.time.ZoneId.of(timeZone)); lastError = null;
            log.info("Automatic Tally PO sync: total={}, exported={}, unchanged/skipped={}, failed={}",
                    lastResult.getTotal(), lastResult.getImported(), lastResult.getSkipped(), lastResult.getFailed());
            for (TallyPurchaseOrderService.ExportLine line : lastResult.getRecords()) {
                String status = "FAILED".equals(line.getStatus()) ? "NEEDS_REVIEW" : line.getStatus();
                if ("NEEDS_REVIEW".equals(status) || "CANCELLATION_REVIEW".equals(status)
                        || "FAILED".equals(status) || "UNCERTAIN".equals(status))
                    exporter.recordAutoSyncStatus(line.getPoNumber(), status, line.getMessage());
                if ("FAILED".equals(line.getStatus())) log.warn("Automatic PO sync failed: {}: {}", line.getPoNumber(), line.getMessage());
            }
        } catch (Exception error) {
            lastError = error.getMessage();
            log.warn("Automatic Tally PO sync could not complete; will retry next cycle: {}", lastError);
        }
    }
    public String getConfigurationError() {
        if (!enabled) return null;
        try { exporter.requireConfiguredCompany(); }
        catch (IllegalStateException error) { return error.getMessage(); }
        try { java.time.LocalDate.parse(fromDate.trim()); java.time.ZoneId.of(timeZone); return null; }
        catch (Exception error) { return "Automatic PO sync needs tally.po.sync.from-date (yyyy-MM-dd) and a valid tally.po.sync.time-zone."; }
    }
    public boolean isConfigured() { return getConfigurationError() == null; }
    public boolean isEnabled() { return enabled; }
    public boolean isRunning() { return exporter.isExportRunning(); }
    public TallyPurchaseOrderService.ExportResult getLastResult() { return lastResult; }
    public LocalDateTime getLastCompleted() { return lastCompleted; }
    public String getLastError() { return lastError; }
}
