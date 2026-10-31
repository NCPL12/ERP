package com.ncpl.sales.service;

import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.repository.PurchaseRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Automatic ERP-to-Tally PO export. No emails or writes to the ERP database. */
@Service
public class TallyPurchaseOrderAutoSync {
    private static final Logger log = LoggerFactory.getLogger(TallyPurchaseOrderAutoSync.class);
    private final PurchaseRepo purchaseRepo;
    private final TallyPurchaseOrderService exporter;
    @Value("${tally.po.sync.enabled:false}") private boolean enabled;
    @Value("${tally.po.sync.cron:0 15 0 * * *}") private String cron = "0 15 0 * * *";
    @Value("${tally.po.sync.time-zone:Asia/Kolkata}") private String timeZone = "Asia/Kolkata";
    private volatile TallyPurchaseOrderService.ExportResult lastResult;
    private volatile LocalDateTime lastCompleted;
    private volatile String lastError;
    public String getScheduleDescription() {
        try {
            String[] fields = cron.trim().split("\\s+");
            if (fields.length == 6 && "*".equals(fields[3]) && "*".equals(fields[4]) && "*".equals(fields[5])) {
                return LocalTime.of(Integer.parseInt(fields[2]), Integer.parseInt(fields[1]))
                        .format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)) + " daily (" + timeZone + ")";
            }
        } catch (RuntimeException ignored) { }
        return cron + " (" + timeZone + ")";
    }
    public List<Map<String,String>> getRecords() { return exporter.getAutoSyncRecords(); }
    public TallyPurchaseOrderAutoSync(PurchaseRepo purchaseRepo, TallyPurchaseOrderService exporter) {
        this.purchaseRepo = purchaseRepo; this.exporter = exporter;
    }
    @Scheduled(cron="${tally.po.sync.cron:0 15 0 * * *}",
            zone="${tally.po.sync.time-zone:Asia/Kolkata}")
    @Transactional(readOnly=true)
    public void synchronize() {
        if (!enabled || exporter.isExportRunning()) return;
        try {
            exporter.requireConfiguredCompany();
            ZoneId zone = ZoneId.of(timeZone);
            LocalDate runDate = LocalDate.now(zone).minusDays(1);
            java.time.Instant from = runDate.atStartOfDay(zone).toInstant();
            java.time.Instant toExclusive = runDate.plusDays(1).atStartOfDay(zone).toInstant();
            List<PurchaseOrder> orders = purchaseRepo.findActiveOrdersForTally(
                            java.sql.Timestamp.from(from), java.sql.Timestamp.from(toExclusive)).stream()
                    .filter(po -> po != null && po.getPoNumber() != null)
                    .filter(po -> {
                        String number=po.getPoNumber().trim().toUpperCase(java.util.Locale.ENGLISH);
                        return !number.startsWith("CODEX-") && !number.startsWith("ERP-PO-TEST")
                                && !number.startsWith("ERP-PO-VERIFY") && !number.startsWith("ERP-PO-ACCESS");
                    }).collect(Collectors.toList());
            lastResult = exporter.exportChangedOrders(orders);
            lastCompleted = LocalDateTime.now(java.time.ZoneId.of(timeZone)); lastError = null;
            log.info("Automatic Tally PO sync for {}: total={}, exported={}, unchanged/skipped={}, failed={}", runDate,
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
        try { ZoneId.of(timeZone); return null; }
        catch (Exception error) { return "Automatic PO sync needs a valid tally.po.sync.time-zone."; }
    }
    public boolean isConfigured() { return getConfigurationError() == null; }
    public boolean isEnabled() { return enabled; }
    public boolean isRunning() { return exporter.isExportRunning(); }
    public TallyPurchaseOrderService.ExportResult getLastResult() { return lastResult; }
    public LocalDateTime getLastCompleted() { return lastCompleted; }
    public String getLastError() { return lastError; }
}
