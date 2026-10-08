package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.model.Invoice;
import com.ncpl.sales.cashflow.model.TallySyncMetadata;
import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import com.ncpl.sales.cashflow.entity.TallySyncStatusEntity;
import com.ncpl.sales.cashflow.repository.InvoiceRepository;
import com.ncpl.sales.cashflow.repository.TallySyncStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tally 6.2 Integration Service with Database Persistence
 */
@Service
public class TallySyncService {
    private static final Logger logger = LoggerFactory.getLogger(TallySyncService.class);

    @Value("${cashflow.tally.sync.enabled:false}")
    private boolean syncEnabled;

    @Value("${cashflow.tally.sync.force:false}")
    private boolean forceSync;

    private TallySyncMetadata lastSyncMetadata;
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);
    private final TallyLiveService tallyLiveService;
    private final InvoiceRepository invoiceRepository;
    private final TallySyncStatusRepository syncStatusRepository;

    public TallySyncService(TallyLiveService tallyLiveService, InvoiceRepository invoiceRepository,
                            TallySyncStatusRepository syncStatusRepository) {
        this.tallyLiveService = tallyLiveService;
        this.invoiceRepository = invoiceRepository;
        this.syncStatusRepository = syncStatusRepository;
    }

    @Scheduled(
            fixedDelayString = "${cashflow.tally.sync.interval-ms:60000}",
            initialDelayString = "${cashflow.tally.sync.initial-delay-ms:0}")
    @Transactional
    public void syncTallyInvoices() {
        syncTallyInvoices(false);
    }

    @Transactional
    public void syncTallyInvoices(boolean force) {
        if (!syncEnabled) {
            logger.info("Tally sync disabled; skipping schedule");
            return;
        }
        if (!syncInProgress.compareAndSet(false, true)) {
            logger.debug("Tally synchronization already in progress; skipping overlapping request");
            return;
        }

        if (!com.ncpl.sales.service.TallySyncCoordinator.tryAcquire()) {
            syncInProgress.set(false);
            logger.info("PO synchronization is using Tally; cashflow refresh will retry next cycle");
            return;
        }
        logger.info("Starting live TallyPrime synchronization... (force={})", force);
        LocalDateTime syncStartTime = LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata"));
        
        try {
            if (lastSyncMetadata == null) {
                lastSyncMetadata = initializeSyncMetadata();
            }

            List<Invoice> liveInvoices = tallyLiveService.fetchOutstandingBills();
            logger.info("Fetched {} outstanding bills from live TallyPrime", liveInvoices.size());

            protectLastGoodSnapshot(liveInvoices, force);

            List<InvoiceEntity> savedInvoices = replaceOutstandingSnapshot(liveInvoices);
            logger.info("Synchronized {} outstanding Tally bills to database", savedInvoices.size());

            TallySyncStatusEntity syncStatus = createSyncStatus(syncStartTime, savedInvoices.size(), "SUCCESS", null);
            syncStatusRepository.save(syncStatus);
            
            lastSyncMetadata.setLastSyncTime(syncStartTime);
            lastSyncMetadata.setLastSyncEndTime(LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")));
            lastSyncMetadata.setInvoicesSynced(savedInvoices.size());
            lastSyncMetadata.setLastSyncStatus("SUCCESS");
            lastSyncMetadata.setLastErrorMessage(null);
            
        } catch (IOException e) {
            logger.error("IO Error during Tally sync: {}", e.getMessage(), e);
            recordSyncFailure("IO Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Tally sync: {}", e.getMessage(), e);
            recordSyncFailure("Unexpected error: " + e.getMessage());
        } finally {
            syncInProgress.set(false);
            com.ncpl.sales.service.TallySyncCoordinator.release();
        }
    }

    /** Fetch the latest outstanding bills without mutating TallyPrime. */
    public List<Invoice> fetchLiveOutstandingInvoices() throws Exception {
        return tallyLiveService.fetchOutstandingBills();
    }

    /** Read-only, on-demand voucher lookup used by the invoice details drawer. */
    public TallyLiveService.VoucherDetails fetchLiveVoucherDetails(
            String billNumber, String partyName, LocalDate billDate) throws Exception {
        return tallyLiveService.fetchVoucherDetails(billNumber, partyName, billDate);
    }

    /** Read-only live classification of customer and supplier advance balances. */
    public TallyLiveService.AdvanceSnapshot fetchLiveAdvanceBalances() throws Exception {
        return tallyLiveService.fetchAdvanceBalances();
    }

    public TallyLiveService.AdvanceDetails fetchLiveAdvanceDetails(
            String partyName, TallyLiveService.AdvanceType type) throws Exception {
        return tallyLiveService.fetchAdvanceDetails(partyName, type);
    }

    public TallyLiveService.CostCentreSnapshot fetchLiveCostCentres() throws Exception {
        return tallyLiveService.fetchCostCentres();
    }

    public TallyLiveService.CostCentreTransactions fetchLiveCostCentreTransactions(
            String costCentreName, LocalDate fromDate, LocalDate toDate) throws Exception {
        return tallyLiveService.fetchCostCentreTransactions(costCentreName, fromDate, toDate);
    }

    public TallyLiveService.SiteProfitability fetchLiveSiteProfitability(
            String siteName, LocalDate fromDate, LocalDate toDate) throws Exception {
        return tallyLiveService.fetchSiteProfitability(siteName, fromDate, toDate);
    }

    private List<InvoiceEntity> replaceOutstandingSnapshot(List<Invoice> invoices) {
        List<InvoiceEntity> existing = invoiceRepository.findBySource("TALLY");
        Map<String, InvoiceEntity> existingByKey = new HashMap<>();
        for (InvoiceEntity entity : existing) {
            existingByKey.put(invoiceKey(entity.getCustomerName(), entity.getInvoiceNumber(), entity.getInvoiceDate()), entity);
        }

        List<InvoiceEntity> snapshot = new ArrayList<>();
        Set<Long> retainedIds = new HashSet<>();
        for (Invoice invoice : invoices) {
            if (!validateInvoice(invoice)) continue;
            String key = invoiceKey(invoice.getCustomerName(), invoice.getInvoiceNumber(), invoice.getInvoiceDate());
            InvoiceEntity entity = existingByKey.get(key);
            if (entity == null) {
                entity = convertToEntity(invoice);
            } else {
                entity.setDueDate(invoice.getDueDate());
                entity.setInvoiceValue(BigDecimal.valueOf(invoice.getInvoiceValue()));
                entity.setInvoiceType(InvoiceEntity.InvoiceType.valueOf(invoice.getType().name()));
            }
            InvoiceEntity saved = invoiceRepository.save(entity);
            snapshot.add(saved);
            retainedIds.add(saved.getId());
        }

        List<InvoiceEntity> settled = existing.stream()
                .filter(entity -> !retainedIds.contains(entity.getId()))
                .collect(java.util.stream.Collectors.toList());
        if (!settled.isEmpty()) invoiceRepository.deleteAll(settled);
        return snapshot;
    }

    private void protectLastGoodSnapshot(List<Invoice> incoming, boolean force) throws IOException {
        if (force) return;
        List<InvoiceEntity> existing = invoiceRepository.findBySource("TALLY");
        if (existing.size() < 20) return;
        long validIncoming = incoming.stream().filter(this::validateInvoice).count();
        if (validIncoming * 4L < existing.size() * 3L) {
            throw new IOException("Tally returned only " + validIncoming + " valid bills while the last good snapshot has "
                    + existing.size() + ". The previous values were preserved. Verify that the correct company is open in TallyPrime, then use Sync Now.");
        }
    }

    private String invoiceKey(String party, String number, LocalDate date) {
        return (party == null ? "" : party.trim().toUpperCase(Locale.ROOT)) + "|"
                + (number == null ? "" : number.trim().toUpperCase(Locale.ROOT)) + "|"
                + (date == null ? "" : date.toString());
    }

    private List<InvoiceEntity> processAndSaveInvoices(List<Invoice> invoices) {
        List<InvoiceEntity> saved = new ArrayList<>();

        for (Invoice invoice : invoices) {
            String invoiceNumber = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber().trim() : "";
            if (invoiceNumber.isEmpty()) {
                logger.warn("Skipping invoice with empty number");
                continue;
            }

            if (!validateInvoice(invoice)) {
                logger.warn("Skipping invalid invoice: {}", invoiceNumber);
                continue;
            }

            LocalDate invoiceDate = invoice.getInvoiceDate();
            if (invoiceRepository.existsBySourceAndInvoiceNumberAndInvoiceDate("TALLY", invoiceNumber, invoiceDate)) {
                logger.debug("Skipping existing invoice (by number+date): {} [{}]", invoiceNumber, invoiceDate);
                continue;
            }

            try {
                InvoiceEntity entity = convertToEntity(invoice);
                InvoiceEntity saved_entity = invoiceRepository.save(entity);
                saved.add(saved_entity);
            } catch (Exception e) {
                logger.error("Failed to save invoice {}: {}", invoiceNumber, e.getMessage());
            }
        }

        return saved;
    }

    private InvoiceEntity convertToEntity(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setCustomerName(invoice.getCustomerName());
        entity.setInvoiceNumber(invoice.getInvoiceNumber());
        entity.setInvoiceDate(invoice.getInvoiceDate());
        entity.setDueDate(invoice.getDueDate());
        entity.setInvoiceValue(BigDecimal.valueOf(invoice.getInvoiceValue()));
        
        String type = invoice.getType().toString();
        entity.setInvoiceType(InvoiceEntity.InvoiceType.valueOf(type));
        entity.setSource("TALLY");
        
        return entity;
    }

    private boolean validateInvoice(Invoice invoice) {
        return invoice.getInvoiceNumber() != null && !invoice.getInvoiceNumber().trim().isEmpty()
            && invoice.getCustomerName() != null && !invoice.getCustomerName().trim().isEmpty()
            && invoice.getInvoiceValue() > 0
            && invoice.getInvoiceDate() != null;
    }

    private TallySyncStatusEntity createSyncStatus(LocalDateTime startTime, int count, String status, String error) {
        TallySyncStatusEntity entity = new TallySyncStatusEntity();
        entity.setLastSyncTime(startTime);
        entity.setLastSyncEndTime(LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")));
        entity.setInvoicesSynced(count);
        entity.setLastSyncStatus(status);
        entity.setLastErrorMessage(error);
        return entity;
    }

    private TallySyncMetadata initializeSyncMetadata() {
        TallySyncMetadata metadata = new TallySyncMetadata();
        metadata.setLastSyncTime(LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).minusDays(1));
        metadata.setLastSyncStatus("INITIALIZED");
        return metadata;
    }

    private void recordSyncFailure(String errorMessage) {
        if (lastSyncMetadata == null) {
            lastSyncMetadata = new TallySyncMetadata();
        }
        lastSyncMetadata.setLastSyncStatus("FAILED");
        lastSyncMetadata.setLastErrorMessage(errorMessage);
        lastSyncMetadata.setLastSyncEndTime(LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")));
        
        try {
            TallySyncStatusEntity syncStatus = createSyncStatus(LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")), 0, "FAILED", errorMessage);
            syncStatusRepository.save(syncStatus);
        } catch (Exception e) {
            logger.error("Failed to record sync failure to database: {}", e.getMessage());
        }
    }

    public TallySyncMetadata getSyncStatus() {
        try {
            Optional<TallySyncStatusEntity> latestPersistedStatus = syncStatusRepository.findFirstByOrderByIdDesc();
            if (latestPersistedStatus.isPresent()) {
                TallySyncStatusEntity entity = latestPersistedStatus.get();
                TallySyncMetadata metadata = new TallySyncMetadata();
                metadata.setLastSyncTime(entity.getLastSyncTime());
                metadata.setLastSyncEndTime(entity.getLastSyncEndTime());
                metadata.setInvoicesSynced(entity.getInvoicesSynced() != null ? entity.getInvoicesSynced() : 0);
                metadata.setLastSyncStatus(entity.getLastSyncStatus());
                metadata.setLastErrorMessage(entity.getLastErrorMessage());
                return metadata;
            }
        } catch (Exception e) {
            logger.warn("Unable to load the latest Tally sync status from database: {}", e.getMessage());
        }

        return lastSyncMetadata;
    }

    public void triggerManualSync() {
        logger.info("Manual Tally sync triggered");
        syncTallyInvoices(true);
    }

    public Map<String, Object> getConfigStatus() {
        Map<String, Object> status = new HashMap<>();
        TallySyncMetadata currentStatus = getSyncStatus();
        status.put("enabled", syncEnabled);
        status.put("forceSync", forceSync);
        status.put("serverUrl", tallyLiveService.getServerUrl());
        status.put("serverAvailable", tallyLiveService.isAvailable());
        status.put("syncInProgress", syncInProgress.get());
        status.put("lastSync", currentStatus != null ? currentStatus.getLastSyncTime() : "Never");
        status.put("lastStatus", currentStatus != null ? currentStatus.getLastSyncStatus() : "Not yet run");

        try {
            status.put("minInvoiceDate", invoiceRepository.findMinInvoiceDateBySource("TALLY"));
            status.put("maxInvoiceDate", invoiceRepository.findMaxInvoiceDateBySource("TALLY"));
        } catch (Exception e) {
            logger.warn("Unable to compute invoice date range for TALLY: {}", e.getMessage());
        }

        return status;
    }
}

