package com.ncpl.sales.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.PurchaseItem;
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
import javax.persistence.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

// Opt-in comparison against the configured development database. No schema
// updates, writes, GRN submission, or application background jobs.
@EnabledIfSystemProperty(named = "grn.performance.live", matches = "true")
@DataJpaTest(showSql = false, properties = {
        "spring.jpa.hibernate.ddl-auto=none", "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF",
        "spring.datasource.hikari.read-only=true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@Transactional(readOnly = true)
class GrnPurchaseItemsLiveReadTest {
    @Autowired PurchaseItemRepo items;
    @Autowired GrnItemRepo receipts;
    @Autowired SalesItemRepo salesItems;
    @Autowired ItemMasterRepo masters;
    @Autowired SupplierRepo suppliers;
    @Autowired DeliveryChallanItemsRepo deliveries;
    @Autowired EntityManager entityManager;
    @Autowired EntityManagerFactory factory;

    @Test void comparesTheRequestedPoWithTheOriginalServiceAndMeasuresTwoQueryResponse() throws Exception {
        PurchaseItemService service = new PurchaseItemService();
        service.purchaseItemRepo = items; service.grnItemRepo = receipts;
        ItemMasterService itemService = new ItemMasterService();
        itemService.itemMasterRepo = masters; itemService.supplierRepo = suppliers;
        service.itemMasterService = itemService;
        SalesService sales = new SalesService();
        ReflectionTestUtils.setField(sales, "salesItemrepo", salesItems);
        DeliveryChallanService dc = new DeliveryChallanService(); dc.dcItemRepo = deliveries;
        sales.dcService = dc; service.salesService = sales;
        GrnService grn = new GrnService(); grn.grnItemRepo = receipts; service.grnService = grn;
        SessionFactory sessions = factory.unwrap(SessionFactory.class);
        sessions.getStatistics().clear();
        long start = System.nanoTime();
        List<PurchaseItem> legacy = service.getPurchaseItem("BGLR-AC-7787-2026", "grn");
        System.out.printf("GRN_LEGACY items=%d statements=%d elapsedMs=%d%n", legacy.size(),
                sessions.getStatistics().getPrepareStatementCount(), (System.nanoTime()-start)/1000000);
        List<Map<String, Object>> expected = new ArrayList<>();
        for (PurchaseItem item : legacy) {
            Map<String, Object> row = new HashMap<>();
            row.put("purchase_item_id", item.getPurchase_item_id()); row.put("quantity", item.getQuantity());
            row.put("amount", item.getAmount()); row.put("unitPrice", item.getUnitPrice());
            row.put("poDescription", item.getPoDescription()); row.put("delivaryDate", item.getDelivaryDate());
            row.put("lrNum", item.getLrNum()); row.put("description", item.getDescription());
            row.put("hsnCode", item.getHsnCode()); row.put("modelNo", item.getModelNo());
            row.put("unitName", item.get("unitName")); row.put("receivedQty", item.get("receivedQty"));
            expected.add(row);
        }
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        for (int attempt = 0; attempt < 2; attempt++) {
            entityManager.clear(); sessions.getStatistics().clear(); start = System.nanoTime();
            List<Map<String, Object>> result = service.getGrnPurchaseItems("BGLR-AC-7787-2026");
            String json = mapper.writeValueAsString(result);
            System.out.printf("GRN_OPTIMIZED attempt=%d items=%d statements=%d elapsedMs=%d jsonBytes=%d%n", attempt,
                    result.size(), sessions.getStatistics().getPrepareStatementCount(), (System.nanoTime()-start)/1000000, json.length());
            assertThat(result).isNotEmpty().containsExactlyInAnyOrderElementsOf(expected);
            assertThat(sessions.getStatistics().getPrepareStatementCount()).isEqualTo(2);
        }
    }
}
