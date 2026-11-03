package com.ncpl.sales.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.ncpl.sales.model.*;
import com.ncpl.sales.repository.GrnRepo;
import com.ncpl.sales.repository.PurchaseItemRepo;

public class GrnServiceTest {
    private GrnService service;
    private Grn grn;

    @Before
    public void setUp() {
        service = new GrnService();
        service.grnRepo = mock(GrnRepo.class);
        service.purchaseOrderService = mock(PurchaseOrderService.class);
        service.purchaseItemRepo = mock(PurchaseItemRepo.class);
        service.salesService = mock(SalesService.class);
        service.stockService = mock(StockService.class);
        service.itemMasterService = mock(ItemMasterService.class);
        grn = new Grn();
        grn.setPoNumber("BGLR-KA-7564-2026");
    }

    @Test
    public void partialReceiptSavesOnlyReceivedLinesAndUpdatesOnlyTheirStockAndSupplier() {
        GrnItems received = item("32796", 2.5f);
        received.setUnitPrice(100);
        grn.setItems(Arrays.asList(item("32797", 0), received, item("32801", 0)));
        PurchaseOrder po = new PurchaseOrder();
        Party vendor = new Party();
        vendor.setId("vendor");
        po.setParty(vendor);
        when(service.purchaseOrderService.findById(grn.getPoNumber())).thenReturn(Optional.of(po));
        PurchaseItem poItem = new PurchaseItem();
        poItem.setModelNo("received-item");
        poItem.setDescription("sales-line");
        when(service.purchaseItemRepo.findById(32796)).thenReturn(Optional.of(poItem));
        SalesOrder so = new SalesOrder();
        Party customer = new Party();
        customer.setId("customer");
        so.setParty(customer);
        SalesItem salesItem = new SalesItem();
        salesItem.setSalesOrder(so);
        when(service.salesService.getSalesItemObjById("sales-line")).thenReturn(Optional.of(salesItem));
        when(service.grnRepo.save(grn)).thenReturn(grn);

        assertSame(grn, service.saveGrn(grn));

        assertEquals(Collections.singletonList(received), grn.getItems());
        verify(service.grnRepo).save(grn);
        verify(service.purchaseItemRepo).findById(32796);
        verifyNoMoreInteractions(service.purchaseItemRepo);
        verify(service.stockService).updateStockQuantityFromGrn("received-item", "customer", 2.5f, "grn", so);
        verify(service.itemMasterService).updateSupplier("received-item", "vendor", 100f);
        verifyNoMoreInteractions(service.stockService, service.itemMasterService);
    }

    @Test
    public void allZeroReceiptIsRejected() {
        grn.setItems(Arrays.asList(item("32797", 0), item("32801", 0)));
        assertRejected("at least one item");
    }

    @Test
    public void negativeQuantityIsRejectedBeforeAnyStockIsUpdated() {
        grn.setItems(Arrays.asList(item("32796", 10), item("32797", -1)));
        assertRejected("32797");
        assertEquals(2, grn.getItems().size());
    }

    @Test
    public void nonFiniteQuantitiesAreRejected() {
        for (float qty : new float[] {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            grn.setItems(Collections.singletonList(item("32797", qty)));
            assertRejected("32797");
        }
    }

    @Test
    public void missingOrEmptyItemsAreRejected() {
        assertRejected("at least one item");
        grn.setItems(Collections.emptyList());
        assertRejected("at least one item");
    }

    private void assertRejected(String message) {
        try {
            service.saveGrn(grn);
            fail("Expected invalid receipt to be rejected");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains(message));
        }
        verifyZeroInteractions(service.grnRepo, service.purchaseOrderService, service.purchaseItemRepo,
                service.salesService, service.stockService, service.itemMasterService);
    }

    private GrnItems item(String poLine, float quantity) {
        GrnItems item = new GrnItems();
        item.setDescription(poLine);
        item.setReceivedQuantity(quantity);
        return item;
    }
}
