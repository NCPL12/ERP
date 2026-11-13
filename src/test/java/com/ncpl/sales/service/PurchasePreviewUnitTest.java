package com.ncpl.sales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.*;
import com.ncpl.sales.repository.PurchaseItemRepo;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchasePreviewUnitTest {
    @Test void previewReturnsMasterUnitWithoutNeedingTheFullItemCatalogue() throws Exception {
        PurchaseItemService service = new PurchaseItemService();
        service.purchaseItemRepo = mock(PurchaseItemRepo.class);
        service.itemMasterService = mock(ItemMasterService.class);
        service.salesService = mock(SalesService.class);
        PurchaseItem line = new PurchaseItem(); line.setModelNo("ITEM"); line.setDescription("SALES");
        ItemMaster item = new ItemMaster(); item.setModel("MODEL");
        Units masterUnit = new Units(); masterUnit.setName("Nos"); item.setItem_units(masterUnit);
        SalesItem sales = new SalesItem(); Units salesUnit = new Units(); salesUnit.setName("Set"); sales.setItem_units(salesUnit);
        when(service.purchaseItemRepo.findByPurchaseOrder_PoNumber("PO")).thenReturn(Collections.singletonList(line));
        when(service.itemMasterService.getItemById("ITEM")).thenReturn(Optional.of(item));
        when(service.salesService.getSalesItemById("SALES", false)).thenReturn(Optional.of(sales));
        PurchaseItem result = service.getPurchaseItem("PO", "po").get(0);
        assertThat(result.get("poUnitName")).isEqualTo("Nos");
        assertThat(result.get("unitName")).isEqualTo("Set");
        assertThat(new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(result)).get("poUnitName").asText()).isEqualTo("Nos");
        verify(service.itemMasterService).getItemById("ITEM");
        verifyNoMoreInteractions(service.itemMasterService);
    }
}
