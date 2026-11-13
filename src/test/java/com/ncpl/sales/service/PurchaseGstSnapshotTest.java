package com.ncpl.sales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.*;
import com.ncpl.sales.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseGstSnapshotTest {
    private final PurchaseOrderService service = new PurchaseOrderService();
    private final ItemMasterService masters = mock(ItemMasterService.class);
    PurchaseGstSnapshotTest() { service.itemMasterService = masters; }

    @Test void savesSnapshotBeforePersistenceAndNewPoUsesChangedMasterRate() {
        ItemMaster master = master(18);
        when(masters.getItemById("ITEM")).thenReturn(Optional.of(master));
        service.designService = mock(SalesOrderDesignService.class);
        service.purchaseItemRepo = mock(PurchaseItemRepo.class);
        service.partyService = mock(PartyService.class);
        service.purchaseRepo = mock(PurchaseRepo.class);
        service.purchaseOrderAuditService = mock(PurchaseOrderAuditService.class);
        when(service.purchaseRepo.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            assertThat(saved.getItems().get(0).getGstRate()).isEqualByComparingTo(master.getGst() == 18 ? "18" : "12");
            return saved;
        });
        PurchaseItem old = line(0, null);
        PurchaseOrder first = new PurchaseOrder(); first.setItems(Collections.singletonList(old));
        service.savePurchaseOrder(first, "", "vendor");
        master.setGst(12);
        PurchaseItem fresh = line(0, null);
        PurchaseOrder second = new PurchaseOrder(); second.setItems(Collections.singletonList(fresh));
        service.savePurchaseOrder(second, "", "vendor");
        assertThat(old.resolveGstRate(master)).isEqualByComparingTo("18");
        assertThat(fresh.resolveGstRate(master)).isEqualByComparingTo("12");
        verify(service.purchaseRepo, times(2)).save(any(PurchaseOrder.class));
    }

    @Test void formRatesIncludingZeroAndDecimalsArePreserved() {
        PurchaseItem zero = line(0, "0"), decimal = line(0, "0.25");
        service.prepareGstRates(Arrays.asList(zero, decimal), Collections.emptyList());
        assertThat(zero.resolveGstRate(master(18))).isEqualByComparingTo("0");
        assertThat(decimal.resolveGstRate(master(18))).isEqualByComparingTo("0.25");
        verifyNoInteractions(masters);
    }

    @Test void ordinaryEditsCannotReplaceSavedRateWithCurrentMasterOrClientRate() {
        PurchaseItem old = line(7, "18"), edited = line(7, "12");
        edited.setQuantity(20); edited.setUnitPrice(120);
        service.prepareGstRates(Collections.singletonList(edited), Collections.singletonList(old));
        assertThat(edited.getGstRate()).isEqualByComparingTo("18");
        assertThat(edited.getQuantity()).isEqualTo(20);
        assertThat(edited.getUnitPrice()).isEqualTo(120);
        verifyNoInteractions(masters);
    }

    @Test void newlyAddedLineAndReplacementCaptureTheirOwnRate() {
        PurchaseItem replaced = line(7, null); replaced.setModelNo("NEW");
        when(masters.getItemById("NEW")).thenReturn(Optional.of(master(5)));
        PurchaseItem added = line(0, "12");
        service.prepareGstRates(Arrays.asList(replaced, added), Collections.singletonList(line(7, "18")));
        assertThat(replaced.getGstRate()).isEqualByComparingTo("5");
        assertThat(added.getGstRate()).isEqualByComparingTo("12");
    }

    @Test void legacyReadsAndEditsDoNotInventAHistoricalRate() {
        PurchaseItem edited = line(7, "18");
        service.prepareGstRates(Collections.singletonList(edited), Collections.singletonList(line(7, null)));
        assertThat(edited.getGstRate()).isNull();
        assertThat(edited.resolveGstRate(master(12))).isEqualByComparingTo("12");
        assertThat(edited.getGstRate()).isNull();
        assertThatThrownBy(() -> edited.resolveGstRate(null)).hasMessageContaining("unknown");
    }

    @Test void missingMasterCannotSilentlyCreateZeroGst() {
        when(masters.getItemById("ITEM")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.prepareGstRates(Collections.singletonList(line(0, null)), Collections.emptyList()))
                .hasMessageContaining("Cannot determine GST");
    }

    @Test void rejectsInvalidRates() {
        for (String value : Arrays.asList("-1", "100.01", "18.001"))
            assertThatThrownBy(() -> line(0, value)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void formBindingJsonAndAuditExposeSavedRate() throws Exception {
        PurchaseItem item = line(7, null);
        new org.springframework.beans.BeanWrapperImpl(item).setPropertyValue("gstRate", "18.00");
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.readTree(mapper.writeValueAsString(item)).get("gstRate").decimalValue()).isEqualByComparingTo("18");
        assertThat(new PurchaseOrderAuditService().toAuditMap(item)).containsEntry("gstRate", item.getGstRate());
    }

    private PurchaseItem line(int id, String gst) {
        PurchaseItem item = new PurchaseItem(); item.setPurchase_item_id(id); item.setModelNo("ITEM");
        if (gst != null) item.setGstRate(new BigDecimal(gst));
        return item;
    }
    private ItemMaster master(int gst) { ItemMaster item = new ItemMaster(); item.setGst(gst); return item; }
}
