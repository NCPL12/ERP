package com.ncpl.sales.service;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.repository.PurchaseRepo;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
class TallyPurchaseOrderAutoSyncTest {
    private void enable(TallyPurchaseOrderAutoSync sync) throws Exception {
        java.lang.reflect.Field f=TallyPurchaseOrderAutoSync.class.getDeclaredField("enabled"); f.setAccessible(true);f.set(sync,true);
        f=TallyPurchaseOrderAutoSync.class.getDeclaredField("fromDate");f.setAccessible(true);f.set(sync,"2026-04-01");
        f=TallyPurchaseOrderAutoSync.class.getDeclaredField("includePreviouslyExported");f.setAccessible(true);f.set(sync,true);
    }
    @Test void automaticallyExportsRealOrdersAndExcludesTestVouchers() throws Exception {
        PurchaseRepo repo=mock(PurchaseRepo.class);TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
        PurchaseOrder real=new PurchaseOrder();real.setPoNumber("BGLR-REAL-1"); real.setCreated(new java.util.Date(1780272000000L));
        PurchaseOrder test=new PurchaseOrder();test.setPoNumber("CODEX-FULL-TEST-1");
        when(repo.findActiveOrdersForTally(any(),any())).thenReturn(Arrays.asList(real,test));
        when(exporter.exportChangedOrders(anyList())).thenReturn(new TallyPurchaseOrderService.ExportResult(1,1,0,0,Collections.emptyList()));
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(repo,exporter); enable(sync);sync.synchronize();
        verify(exporter).exportChangedOrders(Collections.singletonList(real));assertThat(sync.getLastCompleted()).isNotNull();
    }
    @Test void retriesAfterOfflineTallyAndDoesNotOverlapManualExport() throws Exception {
        PurchaseRepo repo=mock(PurchaseRepo.class);TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
        when(repo.findActiveOrdersForTally(any(),any())).thenReturn(Collections.emptyList());
        when(exporter.exportChangedOrders(anyList())).thenThrow(new IllegalStateException("Tally offline"))
            .thenReturn(new TallyPurchaseOrderService.ExportResult(0,0,0,0,Collections.emptyList()));
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(repo,exporter);enable(sync);sync.synchronize();
        assertThat(sync.getLastError()).isEqualTo("Tally offline");sync.synchronize();assertThat(sync.getLastError()).isNull();
        when(exporter.isExportRunning()).thenReturn(true);sync.synchronize();verify(exporter,times(2)).exportChangedOrders(anyList());
    }
    @Test void rechecksTrackedOlderOrdersButExcludesArchivedFutureAndTestOrders() throws Exception {
        PurchaseRepo repo=mock(PurchaseRepo.class);TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
        PurchaseOrder old=new PurchaseOrder();old.setPoNumber("OLD-TRACKED");old.setCreated(new Date(1704067200000L));
        PurchaseOrder archived=new PurchaseOrder();archived.setPoNumber("ARCHIVED");archived.setCreated(new Date());archived.setArchive(true);
        PurchaseOrder future=new PurchaseOrder();future.setPoNumber("FUTURE");future.setCreated(new Date(System.currentTimeMillis()+86400000L*30));
        PurchaseOrder test=new PurchaseOrder();test.setPoNumber("erp-po-access-test");test.setCreated(new Date());
        when(repo.findActiveOrdersForTally(any(),any())).thenReturn(Arrays.asList(archived,future,test));
        when(exporter.previouslyExportedNumbers()).thenReturn(Collections.singleton("OLD-TRACKED"));
        when(repo.findAllById(any())).thenReturn(Collections.singletonList(old));
        when(exporter.exportChangedOrders(anyList())).thenReturn(new TallyPurchaseOrderService.ExportResult(1,0,1,0,Collections.emptyList()));
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(repo,exporter);enable(sync);
        java.lang.reflect.Field f=TallyPurchaseOrderAutoSync.class.getDeclaredField("includePreviouslyExported");f.setAccessible(true);f.set(sync,true);
        sync.synchronize();verify(exporter).exportChangedOrders(Collections.singletonList(old));
    }
    @Test void flagsArchivedAndMissingPreviouslyExportedOrdersWithoutDeletingFromTally() throws Exception {
        PurchaseRepo repo=mock(PurchaseRepo.class);TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
        PurchaseOrder archived=new PurchaseOrder();archived.setPoNumber("ARCHIVED-TRACKED");archived.setCreated(new Date());archived.setArchive(true);
        PurchaseOrder active=new PurchaseOrder();active.setPoNumber("ACTIVE-TRACKED");active.setCreated(new Date());
        when(repo.findActiveOrdersForTally(any(),any())).thenReturn(Collections.emptyList());
        when(exporter.previouslyExportedNumbers()).thenReturn(new HashSet<>(Arrays.asList("ARCHIVED-TRACKED","ACTIVE-TRACKED","MISSING-TRACKED")));
        when(repo.findAllById(any())).thenReturn(Arrays.asList(archived,active));
        when(exporter.exportChangedOrders(anyList())).thenReturn(new TallyPurchaseOrderService.ExportResult(1,0,1,0,Collections.emptyList()));
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(repo,exporter);enable(sync);sync.synchronize();
        verify(exporter).recordAutoSyncStatus("ARCHIVED-TRACKED","CANCELLATION_REVIEW", "PO is archived in ERP. Review whether its Tally voucher should be cancelled; no Tally deletion was made.");
        verify(exporter).recordAutoSyncStatus("MISSING-TRACKED","CANCELLATION_REVIEW", "Previously exported PO is no longer present in ERP. Confirm whether it was deleted before any action in Tally; no Tally deletion was made.");
        verify(exporter).exportChangedOrders(Collections.singletonList(active));
    }
    @Test void dailyIsTheDefaultAutoSyncInterval() {
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(mock(PurchaseRepo.class),mock(TallyPurchaseOrderService.class));
        assertThat(sync.getIntervalMs()).isEqualTo(86_400_000L);
    }
}
