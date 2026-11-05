package com.ncpl.sales.service;

import com.ncpl.sales.model.*;
import com.ncpl.sales.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DcSalesOrderItemsTest {
    private final SalesService service = new SalesService();
    private final SalesItemRepo items = mock(SalesItemRepo.class);
    private final DeliveryChallanItemsRepo deliveries = mock(DeliveryChallanItemsRepo.class);
    private final SalesRepo orders = mock(SalesRepo.class);
    private final SalesOrderDesignService designs = mock(SalesOrderDesignService.class);
    private final DeliveryChallanService dcService = mock(DeliveryChallanService.class);

    DcSalesOrderItemsTest() {
        ReflectionTestUtils.setField(service, "salesItemrepo", items);
        service.dcItemRepo = deliveries;
        ReflectionTestUtils.setField(service, "salesrepo", orders);
        service.soDesignService = designs;
        service.dcService = dcService;
    }

    @Test void batchesDeliveryTotalsAndPreservesFilteringQuantitiesAndSorting() {
        List<String> ids = Collections.singletonList("SO-BLR-ELT-998-2020");
        SalesItem fresh = item(3, 10), partial = item(1, 10), complete = item(2, 10), zeroHistory = item(4, 5);
        when(items.findDcSalesItems(ids)).thenReturn(Arrays.asList(fresh, partial, complete, zeroHistory));
        List<String> itemIds = Arrays.asList(fresh.getId(), partial.getId(), complete.getId(), zeroHistory.getId());
        when(deliveries.findDeliveredQuantities(itemIds)).thenReturn(Arrays.asList(
                new Object[]{partial.getId(), 4d}, new Object[]{complete.getId(), 10d}, new Object[]{zeroHistory.getId(), 0d}));

        List<SalesItem> result = service.getSalesListById(ids, "DC");

        assertThat(result).containsExactly(partial, fresh, zeroHistory);
        assertThat(partial.get("unitName")).isEqualTo("Nos");
        assertThat(partial.get("deliveredQty")).isEqualTo(4f);
        assertThat(partial.get("todaysQty")).isEqualTo(6f);
        assertThat(fresh.get("deliveredQty")).isEqualTo(0f);
        assertThat(fresh.get("todaysQty")).isEqualTo(0f);
        assertThat(zeroHistory.get("todaysQty")).isEqualTo(5f);
        verify(items).findDcSalesItems(ids);
        verify(deliveries).findDeliveredQuantities(itemIds);
        verifyNoMoreInteractions(items, deliveries);
        verifyNoInteractions(orders, designs, dcService);
    }

    @Test void queryCountDoesNotGrowWithTheNumberOfSalesItems() {
        List<String> ids = Arrays.asList("SO1", "SO2");
        List<SalesItem> candidates = new ArrayList<>();
        for (int i = 1; i <= 200; i++) candidates.add(item(i, 1));
        when(items.findDcSalesItems(ids)).thenReturn(candidates);
        when(deliveries.findDeliveredQuantities(anyList())).thenReturn(Collections.emptyList());
        assertThat(service.getSalesListById(ids, "dc")).hasSize(200);
        verify(items).findDcSalesItems(ids);
        verify(deliveries).findDeliveredQuantities(anyList());
        verifyNoMoreInteractions(items, deliveries);
        verifyNoInteractions(orders, designs, dcService);
    }

    @Test void emptyRequestsAndOrdersAvoidUnnecessaryQueries() {
        assertThat(service.getSalesListById(Collections.emptyList(), "dc")).isEmpty();
        assertThat(service.getSalesListById(null, "dc")).isEmpty();
        verifyNoInteractions(items, deliveries);
        when(items.findDcSalesItems(Collections.singletonList("EMPTY"))).thenReturn(Collections.emptyList());
        assertThat(service.getSalesListById(Collections.singletonList("EMPTY"), "dc")).isEmpty();
        verifyNoInteractions(deliveries, orders, designs, dcService);
    }

    private SalesItem item(int suffix, float qty) {
        SalesItem item = new SalesItem(); item.setId("SO-Item-" + suffix); item.setQuantity(qty);
        Units unit = new Units(); unit.setName("Nos"); item.setItem_units(unit);
        return item;
    }
}
