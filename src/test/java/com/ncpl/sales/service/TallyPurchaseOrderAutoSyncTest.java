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
    }
    @Test void automaticallyExportsRealOrdersAndExcludesTestVouchers() throws Exception {
        PurchaseRepo repo=mock(PurchaseRepo.class);TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
        PurchaseOrder real=new PurchaseOrder();real.setPoNumber("BGLR-REAL-1"); real.setCreated(new java.util.Date(1780272000000L));
        PurchaseOrder test=new PurchaseOrder();test.setPoNumber("CODEX-FULL-TEST-1");
        when(repo.findAllPO()).thenReturn(Arrays.asList(real,test));
        when(exporter.exportChangedOrders(anyList())).thenReturn(new TallyPurchaseOrderService.ExportResult(1,1,0,0,Collections.emptyList()));
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(repo,exporter); enable(sync);sync.synchronize();
        verify(exporter).exportChangedOrders(Collections.singletonList(real));assertThat(sync.getLastCompleted()).isNotNull();
    }
    @Test void retriesAfterOfflineTallyAndDoesNotOverlapManualExport() throws Exception {
        PurchaseRepo repo=mock(PurchaseRepo.class);TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
        when(repo.findAllPO()).thenReturn(Collections.emptyList());
        when(exporter.exportChangedOrders(anyList())).thenThrow(new IllegalStateException("Tally offline"))
            .thenReturn(new TallyPurchaseOrderService.ExportResult(0,0,0,0,Collections.emptyList()));
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(repo,exporter);enable(sync);sync.synchronize();
        assertThat(sync.getLastError()).isEqualTo("Tally offline");sync.synchronize();assertThat(sync.getLastError()).isNull();
        when(exporter.isExportRunning()).thenReturn(true);sync.synchronize();verify(exporter,times(2)).exportChangedOrders(anyList());
    }
}
