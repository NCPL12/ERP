package com.ncpl.sales.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DashboardCountDto;
import com.ncpl.sales.repository.DashboardAggregateJdbcRepository;

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
    private DashboardAggregateJdbcRepository dashboardAggRepo;

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
                snapshot = loadCounts();
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
            snapshot = loadCounts();
        }
    }

    private DashboardCountDto loadCounts() {
        return dashboardAggRepo.fetchAllCounts();
    }
}
