package com.ncpl.sales.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DashboardCountDto;
import com.ncpl.sales.model.DashboardListsDto;
import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.TdsItems;
import com.ncpl.sales.repository.DashboardAggregateJdbcRepository;

import javax.annotation.PostConstruct;

/**
 * Dashboard counts + the 7 dashboard list cards: in-memory snapshots for fast HTTP.
 * Recomputation (including the two N+1-shaped list queries — invoices, TDS approved)
 * always runs on a background thread (async warmup + periodic refresh, see
 * {@link DashboardCountsRefreshScheduler}) so a page load is always a cache read.
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private DashboardAggregateJdbcRepository dashboardAggRepo;
    @Autowired
    private SalesService salesService;
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private TdsService tdsService;

    private volatile DashboardCountDto snapshot;
    private volatile DashboardListsDto listsSnapshot;
    private final Object snapshotLock = new Object();
    private final Object listsSnapshotLock = new Object();

    @PostConstruct
    public void warmupSnapshotAsync() {
        CompletableFuture.runAsync(this::recomputeSnapshotQuietly);
        CompletableFuture.runAsync(this::recomputeListsSnapshotQuietly);
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
                snapshot = loadCounts();
            }
            return DashboardCountDto.copyOf(snapshot);
        }
    }

    /**
     * HTTP entry for the 7 dashboard list cards: cache read, same blocking-on-first-call
     * fallback as {@link #getDashboardCounts()}.
     */
    public DashboardListsDto getDashboardLists() {
        DashboardListsDto s = listsSnapshot;
        if (s != null) {
            return s;
        }
        synchronized (listsSnapshotLock) {
            if (listsSnapshot == null) {
                listsSnapshot = loadLists();
            }
            return listsSnapshot;
        }
    }

    /** Invoked by {@link DashboardCountsRefreshScheduler}. */
    public void scheduledRefreshSnapshot() {
        recomputeSnapshotQuietly();
        recomputeListsSnapshotQuietly();
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
            snapshot = loadCounts();
        }
    }

    private void recomputeListsSnapshotQuietly() {
        try {
            synchronized (listsSnapshotLock) {
                listsSnapshot = loadLists();
            }
        } catch (Exception e) {
            log.error("Dashboard lists refresh failed", e);
        }
    }

    private DashboardCountDto loadCounts() {
        return dashboardAggRepo.fetchAllCounts();
    }

    private DashboardListsDto loadLists() {
        DashboardListsDto d = new DashboardListsDto();
        d.setPendingSalesList(salesService.getPendingSalesList());
        d.setPendingPurchaseList(purchaseOrderService.getPenidngPoList());
        d.setInvoiceList(invoiceService.getInvoiceList());
        d.setAllSalesList(salesService.getAllSalesOrderList());
        d.setTdsApprovedList(tdsService.getTdsItemsListWhereTdsApprovedAndPoNotDone());
        List<Map<String, Object>> withoutDesign = salesService.getSoWithoutDesignSummaryForDashboard();
        d.setSalesItemsWithoutDesignList(withoutDesign);
        d.setSalesOrderWithDesignList(salesService.getAllSalesOrderQithDesignAndPoNotDoneForDashboard());
        return d;
    }
}
