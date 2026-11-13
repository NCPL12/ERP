package com.ncpl.sales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.controller.PurchaseController;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserService;
import com.ncpl.sales.repository.*;
import com.ncpl.sales.util.NcplUtil;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfSystemProperty(named = "purchase.performance.live", matches = "true")
@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=none", "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF",
        "spring.datasource.hikari.read-only=true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@Transactional(readOnly = true)
class PurchaseDashboardLiveReadTest {
    @Autowired PurchaseRepo purchases;
    @Autowired PartyRepo parties;
    @Autowired ItemMasterRepo items;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory factory;

    @Test void preservesDashboardRowsAndMeasuresRemovedPageEntryWork() throws Exception {
        PurchaseOrderService purchaseService = new PurchaseOrderService(); purchaseService.purchaseRepo = purchases;
        PartyService partyService = new PartyService(); partyService.partyRepo = parties;
        ItemMasterService itemService = new ItemMasterService(); itemService.itemMasterRepo = items;
        ObjectMapper mapper = new NcplUtil().getObjectMapper();
        SessionFactory sessions = factory.unwrap(SessionFactory.class);
        sessions.getStatistics().clear();
        long start = System.nanoTime();
        String expected = mapper.writeValueAsString(purchaseService.getDashboardPoList());
        String supplierJson = mapper.writeValueAsString(partyService.getPartyListbyTypeSupplier());
        String itemJson = mapper.writeValueAsString(itemService.getModelAndUnitList());
        System.out.printf("PURCHASE_BEFORE queries=%d elapsedMs=%d unusedJsonChars=%d%n",
                sessions.getStatistics().getPrepareStatementCount(), (System.nanoTime()-start)/1000000,
                supplierJson.length()+itemJson.length());
        PurchaseController controller = new PurchaseController();
        ReflectionTestUtils.setField(controller, "purchaseService", purchaseService);
        ReflectionTestUtils.setField(controller, "partyService", partyService);
        ReflectionTestUtils.setField(controller, "itemMasterService", itemService);
        ReflectionTestUtils.setField(controller, "utilService", new NcplUtil());
        UserService users = mock(UserService.class); User user = new User(); user.setRole("SUPER ADMIN"); user.setUsername("tester");
        when(users.getCurrentUser()).thenReturn(user);
        ReflectionTestUtils.setField(controller, "userService", users);
        for (int attempt=0; attempt<2; attempt++) {
            entityManager.clear(); sessions.getStatistics().clear(); start=System.nanoTime();
            ExtendedModelMap model = new ExtendedModelMap();
            assertThat(controller.purchaseDashBoard(model)).isEqualTo("purchaseDashboard");
            assertThat(model.get("poList")).isEqualTo(expected);
            assertThat(model).doesNotContainKeys("partyList", "itemList");
            assertThat(sessions.getStatistics().getPrepareStatementCount()).isEqualTo(1);
            System.out.printf("PURCHASE_AFTER attempt=%d queries=%d elapsedMs=%d poJsonChars=%d%n", attempt,
                    sessions.getStatistics().getPrepareStatementCount(), (System.nanoTime()-start)/1000000, expected.length());
        }
    }
}
