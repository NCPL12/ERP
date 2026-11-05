package com.ncpl.sales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.repository.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

// Explicitly opt in: uses the configured development database, with schema writes
// disabled and a read-only rollback transaction. Never submits or creates a DC.
@EnabledIfSystemProperty(named = "dc.performance.live", matches = "true")
@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=none", "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=OFF", "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF",
        "spring.datasource.hikari.read-only=true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@Transactional(readOnly = true)
class DcSalesOrderLiveReadTest {
    @Autowired SalesItemRepo items;
    @Autowired DeliveryChallanItemsRepo deliveries;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory factory;

    @Test void readsRequestedOrderWithTwoQueriesIncludingJsonSerialization() throws Exception {
        SalesService service = new SalesService();
        ReflectionTestUtils.setField(service, "salesItemrepo", items);
        service.dcItemRepo = deliveries;
        SessionFactory sessions = factory.unwrap(SessionFactory.class);
        sessions.getStatistics().clear();
        long start = System.nanoTime();
        List<SalesItem> result = service.getSalesListById(Collections.singletonList("SO-BLR-ELT-998-2020"), "dc");
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String json = mapper.writeValueAsString(result);
        long elapsed = (System.nanoTime() - start) / 1000000;
        long statements = sessions.getStatistics().getPrepareStatementCount();
        System.out.printf("DC_ORDER_READ items=%d statements=%d elapsedMs=%d jsonBytes=%d%n", result.size(), statements, elapsed, json.length());
        assertThat(result).isNotEmpty();
        assertThat(statements).isEqualTo(2);

        entityManager.clear();
        sessions.getStatistics().clear();
        start = System.nanoTime();
        List<SalesItem> repeated = service.getSalesListById(Collections.singletonList("SO-BLR-ELT-998-2020"), "dc");
        mapper.writeValueAsString(repeated);
        System.out.printf("DC_ORDER_READ_REPEAT items=%d statements=%d elapsedMs=%d%n", repeated.size(),
                sessions.getStatistics().getPrepareStatementCount(), (System.nanoTime() - start) / 1000000);
        assertThat(sessions.getStatistics().getPrepareStatementCount()).isEqualTo(2);

        // Independently verify eligibility and delivery totals directly against SQL.
        List<?> expected = entityManager.createNativeQuery("SELECT si.id FROM tbl_sales_item si " +
                "WHERE si.sales_order_id=:so AND si.unit_price>0 AND si.archive=0 " +
                "AND EXISTS (SELECT 1 FROM sales_order_design d JOIN sales_order_design_items di ON di.design_id=d.design_id WHERE d.sales_item_id=si.id) " +
                "AND (NOT EXISTS (SELECT 1 FROM tbl_dc_items i WHERE i.description=si.id) " +
                "OR si.quantity<>(SELECT SUM(i.todays_qty) FROM tbl_dc_items i WHERE i.description=si.id))")
                .setParameter("so", "SO-BLR-ELT-998-2020").getResultList();
        assertThat(result).extracting(SalesItem::getId).containsExactlyInAnyOrderElementsOf((List<String>) expected);
        for (SalesItem item : result) {
            Object total = entityManager.createNativeQuery("SELECT SUM(todays_qty) FROM tbl_dc_items WHERE description=:id")
                    .setParameter("id", item.getId()).getSingleResult();
            assertThat(item.get("deliveredQty")).isEqualTo(total == null ? 0f : ((Number)total).floatValue());
        }
    }
}
