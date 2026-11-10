package com.ncpl.sales.controller;

import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.service.PurchaseItemService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrnPurchaseItemsRouteTest {
    private final PurchaseController controller = new PurchaseController();
    private final PurchaseItemService service = mock(PurchaseItemService.class);

    GrnPurchaseItemsRouteTest() {
        ReflectionTestUtils.setField(controller, "purchaseItemService", service);
    }

    @Test void grnRequestsUseTheScalarResponse() {
        List<Map<String, Object>> rows = Collections.singletonList(Collections.singletonMap("purchase_item_id", 1));
        when(service.getGrnPurchaseItems("PO")).thenReturn(rows);
        assertThat(controller.purchaseItemList("PO", "GRN", null).getBody()).isSameAs(rows);
        verify(service).getGrnPurchaseItems("PO");
        verifyNoMoreInteractions(service);
    }

    @Test void purchasePreviewKeepsItsExistingResponse() {
        List<PurchaseItem> rows = Collections.singletonList(new PurchaseItem());
        when(service.getPurchaseItem("PO", "po")).thenReturn(rows);
        assertThat(controller.purchaseItemList("PO", "po", null).getBody()).isSameAs(rows);
        verify(service).getPurchaseItem("PO", "po");
        verifyNoMoreInteractions(service);
    }
}
