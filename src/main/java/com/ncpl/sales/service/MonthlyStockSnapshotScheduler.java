package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Clock;
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
    private final Clock clock;

    @Value("${monthly.stock.auto-freeze.enabled:false}")
    private boolean enabled;

    @Value("${monthly.stock.auto-freeze.zone:Asia/Kolkata}")
    private String zone = "Asia/Kolkata";

    @org.springframework.beans.factory.annotation.Autowired
    public MonthlyStockSnapshotScheduler(MonthlyStockMovementService monthlyStockMovementService) {
        this(monthlyStockMovementService, Clock.systemUTC());
    }

    MonthlyStockSnapshotScheduler(MonthlyStockMovementService service, Clock clock) {
        this.monthlyStockMovementService = service;
        this.clock = clock;
    }

    /** Runs after the final minute has completed, targeting only the previous month-end. */
    @Scheduled(cron = "${monthly.stock.auto-freeze.cron:0 5 0 1 * ?}", zone = "${monthly.stock.auto-freeze.zone:Asia/Kolkata}")
    public void freezeMonthEndStock() {
        if (!enabled) {
            return;
        }

        ZoneId zoneId = ZoneId.of(zone);
        LocalDate closingDate = LocalDate.now(clock.withZone(zoneId)).minusDays(1);
        if (closingDate.plusDays(1).getMonth().equals(closingDate.getMonth())) {
            return;
        }

        Timestamp from = Timestamp.valueOf(closingDate.withDayOfMonth(1).atStartOfDay());
        Timestamp to = Timestamp.valueOf(closingDate.atTime(LocalTime.MAX));
        try {
            int itemCount = monthlyStockMovementService.freeze(from, to);
            log.info("Automatically froze {} month-end stock records for {}", itemCount, closingDate);
        } catch (IllegalStateException alreadyFrozen) {
            if (alreadyFrozen.getMessage() != null && alreadyFrozen.getMessage().startsWith("A frozen monthly stock snapshot already exists for ")) {
                log.info("Month-end stock snapshot already exists for {}", closingDate);
            } else {
                log.error("Month-end stock validation failed for {}", closingDate, alreadyFrozen);
            }
        } catch (Exception exception) {
            log.error("Automatic month-end stock snapshot failed for {}", closingDate, exception);
        }
    }
}
