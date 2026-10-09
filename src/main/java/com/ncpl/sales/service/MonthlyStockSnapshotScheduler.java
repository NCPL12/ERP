package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Captures a month-end stock snapshot while the live stock balance still
 * represents that calendar month's closing position. Historical reports then
 * use this immutable boundary instead of reconstructing it from legacy audit
 * data.
 */
@Component
public class MonthlyStockSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonthlyStockSnapshotScheduler.class);

    private final MonthlyStockMovementService monthlyStockMovementService;

    @Value("${monthly.stock.auto-freeze.enabled:false}")
    private boolean enabled;

    @Value("${monthly.stock.auto-freeze.zone:Asia/Kolkata}")
    private String zone;

    public MonthlyStockSnapshotScheduler(MonthlyStockMovementService monthlyStockMovementService) {
        this.monthlyStockMovementService = monthlyStockMovementService;
    }

    /**
     * Runs every night at 23:59. The date guard makes it act only on the last
     * calendar day of a month. It is intentionally disabled until the first
     * Finance-approved baseline has been imported.
     */
    @Scheduled(cron = "${monthly.stock.auto-freeze.cron:0 59 23 * * ?}", zone = "${monthly.stock.auto-freeze.zone:Asia/Kolkata}")
    public void freezeMonthEndStock() {
        if (!enabled) {
            return;
        }

        ZoneId zoneId = ZoneId.of(zone);
        LocalDate closingDate = LocalDate.now(zoneId);
        if (!closingDate.plusDays(1).getMonth().equals(closingDate.getMonth())) {
            return;
        }

        Timestamp from = Timestamp.valueOf(closingDate.withDayOfMonth(1).atStartOfDay());
        Timestamp to = Timestamp.valueOf(closingDate.atTime(LocalTime.MAX));
        try {
            int itemCount = monthlyStockMovementService.freeze(from, to);
            log.info("Automatically froze {} month-end stock records for {}", itemCount, closingDate);
        } catch (IllegalStateException alreadyFrozen) {
            log.info("Month-end stock snapshot already exists for {}", closingDate);
        } catch (Exception exception) {
            log.error("Automatic month-end stock snapshot failed for {}", closingDate, exception);
        }
    }
}
