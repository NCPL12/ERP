package com.ncpl.sales.controller;

import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserService;
import com.ncpl.sales.service.*;
import com.ncpl.sales.util.NcplUtil;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseDashboardTest {
    @Test void dashboardLoadsOnlyItsRowsAndCurrentUser() throws Exception {
        PurchaseController controller = new PurchaseController();
        PurchaseOrderService purchases = mock(PurchaseOrderService.class);
        UserService users = mock(UserService.class);
        PartyService parties = mock(PartyService.class);
        ItemMasterService items = mock(ItemMasterService.class);
        ReflectionTestUtils.setField(controller, "purchaseService", purchases);
        ReflectionTestUtils.setField(controller, "userService", users);
        ReflectionTestUtils.setField(controller, "partyService", parties);
        ReflectionTestUtils.setField(controller, "itemMasterService", items);
        ReflectionTestUtils.setField(controller, "utilService", new NcplUtil());
        User user = new User(); user.setRole("SUPER ADMIN"); user.setUsername("tester");
        when(users.getCurrentUser()).thenReturn(user);
        when(purchases.getDashboardPoList()).thenReturn(Collections.singletonList(Collections.singletonMap("poNumber", "PO-1")));
        ExtendedModelMap model = new ExtendedModelMap();
        assertThat(controller.purchaseDashBoard(model)).isEqualTo("purchaseDashboard");
        assertThat(model.get("poList")).isEqualTo("[{\"poNumber\":\"PO-1\"}]");
        assertThat(model).doesNotContainKeys("partyList", "itemList");
        verifyNoInteractions(parties, items);
    }
}
