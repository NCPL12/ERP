package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import com.ncpl.sales.cashflow.repository.InvoiceRepository;
import com.ncpl.sales.cashflow.repository.TallySyncStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TallySyncServiceSnapshotSafetyTest {
    @Test
    void scheduledSyncRunsAsOneTransaction() throws Exception {
        Method scheduledSync = TallySyncService.class.getMethod("syncTallyInvoices");
        assertNotNull(scheduledSync.getAnnotation(Transactional.class));
    }

    @Test
    void preservesTheLastGoodSnapshotWhenTallyReturnsAnImplausiblySmallResult() throws Exception {
        TallyLiveService tally = mock(TallyLiveService.class);
        InvoiceRepository invoices = mock(InvoiceRepository.class);
        TallySyncStatusRepository statuses = mock(TallySyncStatusRepository.class);
        TallySyncService service = new TallySyncService(tally, invoices, statuses);
        setField(service, "syncEnabled", true);

        List<InvoiceEntity> existing = new ArrayList<>();
        for (int index = 0; index < 100; index++) existing.add(new InvoiceEntity());
        when(invoices.findBySource("TALLY")).thenReturn(existing);
        when(tally.fetchOutstandingBills()).thenReturn(Collections.emptyList());

        service.syncTallyInvoices(false);

        verify(invoices, never()).save(any(InvoiceEntity.class));
        verify(invoices, never()).deleteAll(any());
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
