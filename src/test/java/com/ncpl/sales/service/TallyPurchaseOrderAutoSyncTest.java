package com.ncpl.sales.service;

import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.repository.PurchaseRepo;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TallyPurchaseOrderAutoSyncTest {

    private void enable(TallyPurchaseOrderAutoSync sync) throws Exception {
        java.lang.reflect.Field field = TallyPurchaseOrderAutoSync.class.getDeclaredField("enabled");
        field.setAccessible(true);
        field.set(sync, true);
    }

    @Test
    void automaticSyncIsDisabledByDefault() throws Exception {
        PurchaseRepo repo = mock(PurchaseRepo.class);
        TallyPurchaseOrderService exporter = mock(TallyPurchaseOrderService.class);
        TallyPurchaseOrderAutoSync sync = new TallyPurchaseOrderAutoSync(repo, exporter);

        sync.synchronize();

        assertThat(sync.isEnabled()).isFalse();
        verify(repo, never()).findActiveOrdersForTally(any(), any());
        verify(exporter, never()).exportChangedOrders(any());
    }

    @Test
    void exportsOnlyPreviousCalendarDayOrdersAndExcludesTestVouchers() throws Exception {
        PurchaseRepo repo = mock(PurchaseRepo.class);
        TallyPurchaseOrderService exporter = mock(TallyPurchaseOrderService.class);
        PurchaseOrder real = new PurchaseOrder();
        real.setPoNumber("BGLR-REAL-1");
        PurchaseOrder test = new PurchaseOrder();
        test.setPoNumber("CODEX-FULL-TEST-1");

        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate previousDay = LocalDate.now(zone).minusDays(1);
        Timestamp from = Timestamp.from(previousDay.atStartOfDay(zone).toInstant());
        Timestamp toExclusive = Timestamp.from(previousDay.plusDays(1).atStartOfDay(zone).toInstant());
        when(repo.findActiveOrdersForTally(any(), any())).thenReturn(Arrays.asList(real, test));
        when(exporter.exportChangedOrders(any())).thenReturn(
                new TallyPurchaseOrderService.ExportResult(1, 1, 0, 0, Collections.emptyList()));

        TallyPurchaseOrderAutoSync sync = new TallyPurchaseOrderAutoSync(repo, exporter);
        enable(sync);
        sync.synchronize();

        verify(repo).findActiveOrdersForTally(eq(from), eq(toExclusive));
        verify(exporter).exportChangedOrders(Collections.singletonList(real));
        assertThat(sync.getLastCompleted()).isNotNull();
    }

    @Test
    void recordsFailureAndClearsItAfterSuccessfulLaterAttempt() throws Exception {
        PurchaseRepo repo = mock(PurchaseRepo.class);
        TallyPurchaseOrderService exporter = mock(TallyPurchaseOrderService.class);
        when(repo.findActiveOrdersForTally(any(), any())).thenReturn(Collections.emptyList());
        when(exporter.exportChangedOrders(any()))
                .thenThrow(new IllegalStateException("Tally offline"))
                .thenReturn(new TallyPurchaseOrderService.ExportResult(0, 0, 0, 0, Collections.emptyList()));

        TallyPurchaseOrderAutoSync sync = new TallyPurchaseOrderAutoSync(repo, exporter);
        enable(sync);

        sync.synchronize();
        assertThat(sync.getLastError()).isEqualTo("Tally offline");
        sync.synchronize();

        assertThat(sync.getLastError()).isNull();
        verify(exporter, times(2)).exportChangedOrders(any());
    }
}
