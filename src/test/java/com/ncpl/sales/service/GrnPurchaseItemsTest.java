package com.ncpl.sales.service;

import com.ncpl.sales.repository.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrnPurchaseItemsTest {
    private final PurchaseItemService service = new PurchaseItemService();
    private final PurchaseItemRepo items = mock(PurchaseItemRepo.class);
    private final GrnItemRepo receipts = mock(GrnItemRepo.class);

    GrnPurchaseItemsTest() {
        service.purchaseItemRepo = items; service.grnItemRepo = receipts;
        service.salesService = mock(SalesService.class);
        service.itemMasterService = mock(ItemMasterService.class);
        service.grnService = mock(GrnService.class);
    }

    @Test void batchesReceiptsAndPreservesPendingQuantitiesAndLineFields() {
        Map<String, Object> fresh = row(1, 10), partial = row(2, 10), complete = row(3, 10), zeroReceipt = row(4, 10);
        when(items.findGrnPurchaseItemRows("PO")).thenReturn(Arrays.asList(fresh, partial, complete, zeroReceipt));
        when(receipts.findReceivedQuantities(Arrays.asList("1", "2", "3", "4")))
                .thenReturn(Arrays.asList(new Object[]{"2", 3d}, new Object[]{"3", 10d}, new Object[]{"4", 0d}));
        List<Map<String, Object>> result = service.getGrnPurchaseItems("PO");
        assertThat(result).containsExactly(fresh, partial, zeroReceipt);
        assertThat(fresh.get("receivedQty")).isEqualTo(0f);
        assertThat(partial.get("receivedQty")).isEqualTo(7f);
        assertThat(zeroReceipt.get("receivedQty")).isEqualTo(10f);
        assertThat(partial.get("modelNo")).isEqualTo("MODEL");
        assertThat(partial.get("unitName")).isEqualTo("Nos");
        assertThat(partial.get("unitPrice")).isEqualTo(12.5f);
        verify(items).findGrnPurchaseItemRows("PO");
        verify(receipts).findReceivedQuantities(Arrays.asList("1", "2", "3", "4"));
        verifyNoMoreInteractions(items, receipts);
        verifyNoInteractions(service.salesService, service.itemMasterService, service.grnService);
    }

    @Test void emptyPoAvoidsTheReceiptQuery() {
        when(items.findGrnPurchaseItemRows("EMPTY")).thenReturn(Collections.emptyList());
        assertThat(service.getGrnPurchaseItems("EMPTY")).isEmpty();
        verifyNoInteractions(receipts);
    }

    @Test void zeroQuantityLinesRemainExcludedEvenWithoutReceipts() {
        when(items.findGrnPurchaseItemRows("ZERO")).thenReturn(Collections.singletonList(row(1, 0)));
        when(receipts.findReceivedQuantities(Collections.singletonList("1"))).thenReturn(Collections.emptyList());
        assertThat(service.getGrnPurchaseItems("ZERO")).isEmpty();
    }

    @Test void queryCountDoesNotIncreaseWithLineCount() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 1; i <= 200; i++) rows.add(row(i, 5));
        when(items.findGrnPurchaseItemRows("PO")).thenReturn(rows);
        when(receipts.findReceivedQuantities(anyList())).thenReturn(Collections.emptyList());
        assertThat(service.getGrnPurchaseItems("PO")).hasSize(200);
        verify(items).findGrnPurchaseItemRows("PO");
        verify(receipts).findReceivedQuantities(anyList());
        verifyNoMoreInteractions(items, receipts);
    }

    private Map<String, Object> row(int id, float qty) {
        Map<String, Object> row = new HashMap<>();
        row.put("purchase_item_id", id); row.put("quantity", qty);
        row.put("modelNo", "MODEL"); row.put("unitName", "Nos"); row.put("unitPrice", 12.5f);
        return row;
    }
}
