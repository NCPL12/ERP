package com.ncpl.sales.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DashboardCountDto;
import com.ncpl.sales.repository.InvoiceRepo;
import com.ncpl.sales.repository.SalesRepo;
import com.ncpl.sales.repository.WorkOrderRepo;

import javax.annotation.PostConstruct;

/**
 * Dashboard counts: in-memory snapshot for fast HTTP; recomputation uses one JDBC
 * round-trip when possible, plus async warmup and periodic refresh (see
 * {@link DashboardCountsRefreshScheduler}).
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private SalesRepo salesRepo;

    @Autowired
    private InvoiceRepo invoiceRepo;

    @Autowired
    private WorkOrderRepo workOrderRepo;

    private volatile DashboardCountDto snapshot;
    private final Object snapshotLock = new Object();

    @PostConstruct
    public void warmupSnapshotAsync() {
        CompletableFuture.runAsync(this::recomputeSnapshotQuietly);
    }   

    /**
     * HTTP entry: returns cached snapshot when available (typically under 1 ms). If the
     * cache is still empty (very first request before warmup finishes), blocks until
     * the first load completes.
     */
    public DashboardCountDto getDashboardCounts() {
        DashboardCountDto s = snapshot;
        if (s != null) {
            return DashboardCountDto.copyOf(s);
        }
        synchronized (snapshotLock) {
            if (snapshot == null) {
                snapshot = loadCountsViaJpaParallel();
            }
            return DashboardCountDto.copyOf(snapshot);
        }
    }

    /** Invoked by {@link DashboardCountsRefreshScheduler}. */
    public void scheduledRefreshSnapshot() {
        recomputeSnapshotQuietly();
    }

    private void recomputeSnapshotQuietly() {
        try {
            recomputeSnapshot();
        } catch (Exception e) {
            log.error("Dashboard counts refresh failed", e);
        }
    }

    private void recomputeSnapshot() {
        synchronized (snapshotLock) {
            snapshot = loadCountsViaJpaParallel();
        }
    }

    private DashboardCountDto loadCountsViaJpaParallel() {
        CompletableFuture<Long> salesOrderCount = CompletableFuture.supplyAsync(
                () -> salesRepo.countSalesOrdersWithoutDC() + salesRepo.countPendingSalesOrdersPartialDC());
        CompletableFuture<Long> invoiceCount = CompletableFuture.supplyAsync(() -> invoiceRepo.count());
        CompletableFuture<Long> tdsItemsCount = CompletableFuture
                .supplyAsync(() -> salesRepo.countTdsApprovedAndPoNotDoneListDashboard());
        CompletableFuture<Long> activeSalesOrders = CompletableFuture
                .supplyAsync(() -> salesRepo.countActiveSalesOrders());
        CompletableFuture<Long> workOrders = CompletableFuture.supplyAsync(() -> workOrderRepo.count());
        CompletableFuture<Long> sowithoutDesign = CompletableFuture
                .supplyAsync(() -> salesRepo.countSalesOrderWithoutDesign());
        CompletableFuture<Long> sowithDesign = CompletableFuture
                .supplyAsync(() -> salesRepo.countSalesOrderWithDesign());

        CompletableFuture.allOf(salesOrderCount, invoiceCount, tdsItemsCount,
                activeSalesOrders, workOrders, sowithoutDesign, sowithDesign).join();

        DashboardCountDto dto = new DashboardCountDto();
        dto.setSalesOrderCount(salesOrderCount.join());
        dto.setInvoiceCount(invoiceCount.join());
        dto.setTdsItemsCount(tdsItemsCount.join());
        dto.setProjectPreviewCount(workOrders.join() + activeSalesOrders.join());
        dto.setSowithoutDesignCount(sowithoutDesign.join());
        dto.setSowithDesignCount(sowithDesign.join());
        return dto;
    }
}
