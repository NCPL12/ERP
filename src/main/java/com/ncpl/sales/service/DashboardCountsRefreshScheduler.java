package com.ncpl.sales.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps {@link DashboardService} snapshot fresh without blocking HTTP threads.
 */
@Component
public class DashboardCountsRefreshScheduler {

    @Autowired
    private DashboardService dashboardService;

    @Scheduled(fixedDelayString = "${dashboard.counts.refresh-ms:60000}")
    public void refreshDashboardCounts() {
        dashboardService.scheduledRefreshSnapshot();
    }
}
