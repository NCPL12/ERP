package com.ncpl.sales.service;

import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.repository.ItemMasterRepo;
import com.ncpl.sales.repository.SupplierRepo;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemMasterGstLockTest {
    private final ItemMasterService service = new ItemMasterService();
    private final ItemMasterRepo items = mock(ItemMasterRepo.class);
    ItemMasterGstLockTest() {
        service.itemMasterRepo = items; service.supplierRepo = mock(SupplierRepo.class);
        when(items.save(any(ItemMaster.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
    @AfterEach void clearUser() { SecurityContextHolder.clearContext(); }

    @Test void noRoleCanOverrideGstThroughAnUpdateRequest() {
        ItemMaster original = item("ITEM", 18); original.setCreated(new Date(1000)); original.setCreatedBy("creator");
        when(items.findById("ITEM")).thenReturn(Optional.of(original));
        for (String role : new String[]{"SUPER ADMIN", "ADMIN", "PURCHASE USER", "STORE USER"}) {
            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("tester", "", role));
            ItemMaster request = item("ITEM", 12); request.setItemName("Updated description"); request.setLocation("Shelf B");
            ItemMaster saved = service.saveItemMaster(request);
            assertThat(saved.getGst()).as(role).isEqualTo(18);
            assertThat(saved.getItemName()).isEqualTo("Updated description");
            assertThat(saved.getLocation()).isEqualTo("Shelf B");
            assertThat(saved.getCreated()).isEqualTo(original.getCreated());
            assertThat(saved.getCreatedBy()).isEqualTo("creator");
        }
        verify(items, times(4)).save(argThat(item -> item.getGst() == 18));
    }

    @Test void existingZeroRateIsAlsoLocked() {
        when(items.findById("ITEM")).thenReturn(Optional.of(item("ITEM", 0)));
        assertThat(service.saveItemMaster(item("ITEM", 18)).getGst()).isZero();
        verify(items).save(argThat(item -> item.getGst() == 0));
    }

    @Test void creatingItemsStillAcceptsTheirInitialGst() {
        assertThat(service.saveItemMaster(item("", 18)).getGst()).isEqualTo(18);
        assertThat(service.saveItemMaster(item("", 0)).getGst()).isZero();
        verify(items, never()).findById(anyString());
    }
    private ItemMaster item(String id, int gst) {
        ItemMaster item = new ItemMaster(); item.setId(id); item.setModel("MODEL"); item.setGst(gst); return item;
    }
}
