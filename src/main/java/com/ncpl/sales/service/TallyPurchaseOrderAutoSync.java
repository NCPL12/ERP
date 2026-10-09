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
import java.util.stream.Collectors;

/** Automatic ERP-to-Tally PO export. No emails or writes to the ERP database. */
@Service
public class TallyPurchaseOrderAutoSync {
    private static final Logger log = LoggerFactory.getLogger(TallyPurchaseOrderAutoSync.class);
    private final PurchaseRepo purchaseRepo;
    private final TallyPurchaseOrderService exporter;
    @Value("${tally.po.sync.enabled:false}") private boolean enabled;
    @Value("${tally.po.sync.from-date:}") private String fromDate = "";
    @Value("${tally.po.sync.include-previously-exported:false}") private boolean includePreviouslyExported;
    private volatile TallyPurchaseOrderService.ExportResult lastResult;
    private volatile LocalDateTime lastCompleted;
    private volatile String lastError;
    public TallyPurchaseOrderAutoSync(PurchaseRepo purchaseRepo, TallyPurchaseOrderService exporter) {
        this.purchaseRepo = purchaseRepo; this.exporter = exporter;
    }
    @Scheduled(initialDelayString="${tally.po.sync.initial-delay-ms:30000}",
            fixedDelayString="${tally.po.sync.interval-ms:300000}")
    @Transactional(readOnly=true)
    public void synchronize() {
        if (!enabled || exporter.isExportRunning()) return;
        try {
            if (fromDate.trim().isEmpty()) throw new IllegalStateException("Automatic PO sync needs a configured starting date.");
            java.time.LocalDate startingDate = java.time.LocalDate.parse(fromDate);
            java.util.Set<String> previous = includePreviouslyExported ? exporter.previouslyExportedNumbers() : java.util.Collections.emptySet();
            List<PurchaseOrder> orders = purchaseRepo.findAllPO().stream()
                    .filter(po -> previous.contains(po.getPoNumber()) || (po.getCreated() != null && !java.time.Instant.ofEpochMilli(po.getCreated().getTime())
                            .atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalDate().isBefore(startingDate)))
                    .filter(po -> po.getPoNumber() != null && !po.getPoNumber().startsWith("CODEX-")
                            && !po.getPoNumber().startsWith("ERP-PO-TEST")
                            && !po.getPoNumber().startsWith("ERP-PO-VERIFY"))
                    .collect(Collectors.toList());
            lastResult = exporter.exportChangedOrders(orders);
            lastCompleted = LocalDateTime.now(); lastError = null;
            log.info("Automatic Tally PO sync: total={}, exported={}, unchanged/skipped={}, failed={}",
                    lastResult.getTotal(), lastResult.getImported(), lastResult.getSkipped(), lastResult.getFailed());
            for (TallyPurchaseOrderService.ExportLine line : lastResult.getRecords())
                if ("FAILED".equals(line.getStatus())) log.warn("Automatic PO sync failed: {}: {}", line.getPoNumber(), line.getMessage());
        } catch (Exception error) {
            lastError = error.getMessage();
            log.warn("Automatic Tally PO sync could not complete; will retry next cycle: {}", lastError);
        }
    }
    public boolean isEnabled() { return enabled; }
    public boolean isRunning() { return exporter.isExportRunning(); }
    public TallyPurchaseOrderService.ExportResult getLastResult() { return lastResult; }
    public LocalDateTime getLastCompleted() { return lastCompleted; }
    public String getLastError() { return lastError; }
}
