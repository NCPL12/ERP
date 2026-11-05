package com.ncpl.sales.service;

import com.ncpl.sales.model.*;
import com.ncpl.sales.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryChallanDesignAllocationTest {
    private final DeliveryChallanService service = new DeliveryChallanService();
    private final DeliveryChallan dc = new DeliveryChallan();
    private final SalesOrder so = new SalesOrder();
    private final SalesOrderDesign design = new SalesOrderDesign();
    private final DesignItems first = new DesignItems(), second = new DesignItems();
    // Stop at persistence so this service test never creates files or sends email.
    private final RuntimeException persistenceReached = new RuntimeException("persistence reached");

    @BeforeEach void setUp() {
        service.salesRepo = mock(SalesRepo.class);
        service.dcRepo = mock(DeliveryChallanRepo.class);
        service.designService = mock(SalesOrderDesignService.class);
        service.designItemRepo = mock(SalesOrderDesignItemsRepo.class);
        service.stockRepo = mock(StockRepo.class);
        service.stockService = mock(StockService.class);
        Party party = new Party(); party.setId("CLIENT"); so.setParty(party);
        when(service.salesRepo.findById("SO")).thenReturn(Optional.of(so));
        DeliveryChallanItems row = new DeliveryChallanItems();
        row.setDescription("SO-Item-55433"); row.setTodaysQty(1);
        dc.setItems(Collections.singletonList(row));
        design.setId(103764); design.setSalesItemId(row.getDescription());
        configure(first, "IM-BLR-ELT-1121-2020");
        configure(second, "IM-BLR-ELT-6890-2020");
        design.setItems(Arrays.asList(first, second));
        when(service.designService.findSalesOrderDesignObjBysalesItemId(row.getDescription())).thenReturn(design);
        when(service.dcRepo.save(dc)).thenThrow(persistenceReached);
    }

    private void configure(DesignItems item, String id) {
        item.setItemId(id); item.setQuantity(1); item.setSalesOrderDesign(design);
        when(service.designItemRepo.findDesignItemListByItemIdAndDesignId(id, design.getId()))
                .thenReturn(Collections.singletonList(item));
        Stock stock = new Stock(); stock.setQuantity(10);
        when(service.stockRepo.findStockByClientAndItemId(id, "CLIENT")).thenReturn(stock);
    }

    private JSONObject allocation(DesignItems item, float qty) {
        return new JSONObject().put("model", item.getItemId()).put("itemId", item.getItemId())
                .put("designId", "103764").put("todayQty", Float.toString(qty));
    }

    private void save(float firstQty, float secondQty) throws Exception {
        service.saveDc(dc, new String[]{new JSONArray().put(allocation(first, firstQty))
                .put(allocation(second, secondQty)).toString()}, "SO");
    }

    @Test void oneSalesUnitCanDispatchBothRequiredDesignComponents() {
        assertThatThrownBy(() -> save(1, 1)).isSameAs(persistenceReached);
        assertThat(first.getDeliveredQty()).isEqualTo(1);
        assertThat(second.getDeliveredQty()).isEqualTo(1);
        verify(service.stockService).updateStockQuantityFromGrn(first.getItemId(), "CLIENT", 1, "dc", so);
        verify(service.stockService).updateStockQuantityFromGrn(second.getItemId(), "CLIENT", 1, "dc", so);
    }

    @Test void componentQuantitiesCanDifferFromEachOtherAndTheSalesUnit() {
        first.setQuantity(2); second.setQuantity(3);
        assertThatThrownBy(() -> save(2, 3)).isSameAs(persistenceReached);
        assertThat(first.getDeliveredQty()).isEqualTo(2);
        assertThat(second.getDeliveredQty()).isEqualTo(3);
    }

    @Test void cannotDeliverMoreThanTheRemainingDesignQuantity() {
        first.setDeliveredQty(1);
        assertThatThrownBy(() -> save(1, 1)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds remaining quantity");
        verifyNoInteractions(service.dcRepo, service.stockService);
        assertThat(first.getDeliveredQty()).isEqualTo(1);
    }

    @Test void insufficientComponentStockStillBlocksTheDc() {
        when(service.stockRepo.findStockByClientAndItemId(first.getItemId(), "CLIENT")).thenReturn(null);
        assertThatThrownBy(() -> save(1, 1)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
        verifyNoInteractions(service.dcRepo, service.stockService);
    }

    @Test void negativeAndNonFiniteComponentQuantitiesAreRejected() {
        for (float qty : new float[]{-1, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThatThrownBy(() -> save(qty, 1)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("finite, non-negative");
        }
        verifyNoInteractions(service.dcRepo, service.stockService);
    }

    @Test void designsFromAnotherSalesItemAreRejected() {
        SalesOrderDesign other = new SalesOrderDesign(); other.setSalesItemId("OTHER");
        first.setSalesOrderDesign(other);
        assertThatThrownBy(() -> save(1, 1)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
        verifyNoInteractions(service.dcRepo, service.stockService);
    }

    @Test void positiveSalesDeliveryRequiresPositiveComponentAllocation() {
        assertThatThrownBy(() -> save(0, 0)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Allocate design quantities");
        verifyNoInteractions(service.dcRepo, service.stockService);
    }

    @Test void multipleDesignsStillRequireExplicitAllocation() {
        assertThatThrownBy(() -> service.saveDc(dc, new String[]{"[]"}, "SO"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Allocate today's quantity");
        verifyNoInteractions(service.dcRepo, service.stockService);
    }

    @Test void singleDesignAllocationStillMustMatchSalesDelivery() {
        design.setItems(Collections.singletonList(first)); first.setQuantity(2);
        assertThatThrownBy(() -> service.saveDc(dc,
                new String[]{new JSONArray().put(allocation(first, 2)).toString()}, "SO"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("do not match");
        verifyNoInteractions(service.dcRepo);
    }

    @Test void singleDesignWithoutAllocationDeductsStockOnce() {
        design.setItems(Collections.singletonList(first));
        assertThatThrownBy(() -> service.saveDc(dc, new String[]{"[]"}, "SO"))
                .isSameAs(persistenceReached);
        assertThat(first.getDeliveredQty()).isEqualTo(1);
        verify(service.stockService).updateStockQuantityFromGrn(first.getItemId(), "CLIENT", 1, "dc", so);
        verify(service.designItemRepo).save(first);
        verifyNoMoreInteractions(service.stockService);
    }
}
