package com.ncpl.sales.service;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import com.ncpl.sales.repository.MonthlyStockMovementRepo;
import java.time.*;
import java.sql.Timestamp;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class MonthlyStockSnapshotSchedulerTest {
    private MonthlyStockSnapshotScheduler scheduler(MonthlyStockMovementService service,Instant now) {
        MonthlyStockSnapshotScheduler s=new MonthlyStockSnapshotScheduler(service,Clock.fixed(now,ZoneOffset.UTC));
        ReflectionTestUtils.setField(s,"enabled",true);return s;
    }
    @Test void ordinaryDaysDoNotFreezeAnySnapshot() {
        MonthlyStockMovementService service=mock(MonthlyStockMovementService.class);
        scheduler(service,Instant.parse("2026-10-15T00:00:00Z")).freezeMonthEndStock();
        verifyZeroInteractions(service);
    }
    @Test void firstDayFreezesPreviousMonthIncludingFullLastMinute() {
        MonthlyStockMovementService service=mock(MonthlyStockMovementService.class);
        scheduler(service,Instant.parse("2026-09-30T18:35:00Z")).freezeMonthEndStock();
        verify(service).freeze(Timestamp.valueOf("2026-09-01 00:00:00"),Timestamp.valueOf("2026-09-30 23:59:59.999999999"));
    }
    @Test void monthLengthsLeapYearsAndYearBoundariesSelectCorrectClose() {
        for(String closing:new String[]{"2026-01-31","2026-02-28","2028-02-29","2026-04-30","2026-12-31"}) {
            LocalDate date=LocalDate.parse(closing);MonthlyStockMovementService service=mock(MonthlyStockMovementService.class);
            Instant run=date.plusDays(1).atTime(0,5).atZone(ZoneId.of("Asia/Kolkata")).toInstant();
            scheduler(service,run).freezeMonthEndStock();
            verify(service).freeze(Timestamp.valueOf(date.withDayOfMonth(1).atStartOfDay()),Timestamp.valueOf(date.atTime(LocalTime.MAX)));
        }
    }
    @Test void disabledSchedulerNeverWrites() {
        MonthlyStockMovementService service=mock(MonthlyStockMovementService.class);
        MonthlyStockSnapshotScheduler scheduler=scheduler(service,Instant.parse("2026-09-30T18:35:00Z"));ReflectionTestUtils.setField(scheduler,"enabled",false);
        scheduler.freezeMonthEndStock();verifyZeroInteractions(service);
    }
    @Test void duplicateFreezeIsLeftIntactAndOtherValidationFailuresAreNotWritten() {
        MonthlyStockMovementService service=mock(MonthlyStockMovementService.class);
        when(service.freeze(any(),any())).thenThrow(new IllegalStateException("A frozen monthly stock snapshot already exists for 2026-09-30"));
        scheduler(service,Instant.parse("2026-09-30T18:35:00Z")).freezeMonthEndStock();
        verify(service,times(1)).freeze(any(),any());
    }
    @Test void configuredCronRunsAfterMonthEndInBusinessTimezone() throws Exception {
        Scheduled annotation=MonthlyStockSnapshotScheduler.class.getMethod("freezeMonthEndStock").getAnnotation(Scheduled.class);
        String cron=annotation.cron().substring(annotation.cron().indexOf(':')+1,annotation.cron().length()-1);
        CronSequenceGenerator generator=new CronSequenceGenerator(cron,TimeZone.getTimeZone("Asia/Kolkata"));
        Date next=generator.next(Date.from(Instant.parse("2026-09-30T17:00:00Z")));
        assertThat(next.toInstant()).isEqualTo(Instant.parse("2026-09-30T18:35:00Z"));
    }
    @Test void historicalAuditCutoffDoesNotDependOnServerTimezone() {
        TimeZone previous=TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            MonthlyStockMovementService service=new MonthlyStockMovementService(mock(MonthlyStockMovementRepo.class));
            Long cutoff=ReflectionTestUtils.invokeMethod(service,"snapshotCutoff",Timestamp.valueOf("2026-09-30 23:59:59.999"));
            assertThat(cutoff).isEqualTo(Instant.parse("2026-09-30T18:29:59.999Z").toEpochMilli());
        }finally{TimeZone.setDefault(previous);}
    }
}
