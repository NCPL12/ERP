package com.ncpl.sales.service;

import com.ncpl.sales.model.MonthlyStockMovement;
import com.ncpl.sales.repository.MonthlyStockMovementRepo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonthlyStockMovementRegressionTest {
    private final MonthlyStockMovementRepo repo = mock(MonthlyStockMovementRepo.class);
    private final EntityManager em = mock(EntityManager.class);
    private final Map<String, Query> queries = new LinkedHashMap<>();
    private final MonthlyStockMovementService service = new MonthlyStockMovementService(repo);

    MonthlyStockMovementRegressionTest() {
        ReflectionTestUtils.setField(service, "entityManager", em);
        when(repo.findByReportDate(any())).thenReturn(Collections.emptyList());
        when(em.createNativeQuery(anyString())).thenAnswer(call -> {
            String sql = call.getArgument(0);
            Query query = mock(Query.class, RETURNS_SELF);
            List<Object[]> rows = Collections.emptyList();
            if (sql.startsWith("SELECT model_no,unit_price")) rows = Arrays.asList(
                new Object[]{"item",100f},new Object[]{"item",null},new Object[]{"item",120f},
                new Object[]{"unpriced",null},new Object[]{"zero",0f});
            else if (sql.startsWith("SELECT id, model")) rows = Arrays.asList(
                new Object[]{"item","MODEL","Description"},new Object[]{"unpriced","NONE","No price"});
            else if (sql.startsWith("SELECT a.item_master_id")) rows = Collections.singletonList(new Object[]{"item",new BigDecimal("0.004321")});
            else if (sql.startsWith("SELECT pi.model_no, gi.received_quantity")) rows = Arrays.asList(
                new Object[]{"item",2f},new Object[]{"unpriced",1f});
            else if (sql.startsWith("SELECT di.item_id")) rows = Collections.singletonList(new Object[]{"item",1f});
            when(query.getResultList()).thenReturn(rows);
            queries.put(sql, query);
            return query;
        });
    }

    private Map<String, Map> generate() {
        return service.generate(Timestamp.valueOf("2026-09-01 00:00:00"),Timestamp.valueOf("2026-09-30 23:59:59"));
    }

    @Test void nullPricesAreIgnoredAndOneAverageIsSharedByAllValuations() {
        Map row = (Map)generate().get("grnlist").get("item");
        assertThat((BigDecimal)row.get("grnR1")).isEqualByComparingTo("110");
        assertThat((BigDecimal)row.get("grnV1")).isEqualByComparingTo("220");
        assertThat((BigDecimal)row.get("reportedDcValue")).isEqualByComparingTo("110");
        verify(em,times(1)).createNativeQuery("SELECT model_no,unit_price FROM tbl_purchase_items WHERE model_no IS NOT NULL");
        Map<String,BigDecimal> prices = ReflectionTestUtils.invokeMethod(service,"loadAveragePoPrices");
        assertThat(prices).doesNotContainKey("unpriced");
        assertThat(prices.get("zero")).isEqualByComparingTo("0");
    }

    @Test void freezePreservesSixDecimalQuantitiesAndFollowingMonthOpening() {
        assertThat(service.freeze(Timestamp.valueOf("2026-09-01 00:00:00"),Timestamp.valueOf("2026-09-30 23:59:59"))).isEqualTo(2);
        ArgumentCaptor<Iterable<MonthlyStockMovement>> saved = ArgumentCaptor.forClass(Iterable.class);
        verify(repo).saveAll(saved.capture());
        MonthlyStockMovement closing = saved.getValue().iterator().next();
        assertThat(closing.getClosingQty()).isEqualByComparingTo("0.004321");
        when(repo.findByReportDate(java.time.LocalDate.of(2026,9,30))).thenReturn(Collections.singletonList(closing));
        Map row=(Map)service.generate(Timestamp.valueOf("2026-10-01 00:00:00"),Timestamp.valueOf("2026-10-31 23:59:59")).get("grnlist").get("item");
        assertThat((BigDecimal)row.get("openQ1")).isEqualByComparingTo("0.004321");
    }

    @Test void auditAndJdbcBoundsUseSameBusinessIntervalInEveryServerZone() {
        TimeZone original=TimeZone.getDefault();
        try {
            for(String zone:Arrays.asList("UTC","America/Los_Angeles","Asia/Kolkata")) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                generate();
                long start=Instant.parse("2026-08-31T18:30:00Z").toEpochMilli();
                long end=Instant.parse("2026-09-30T18:30:00Z").toEpochMilli();
                for(Map.Entry<String,Query> entry:queries.entrySet()) {
                    String sql=entry.getKey();Query query=entry.getValue();
                    if(sql.contains(":fromDate")) {
                        ArgumentCaptor<Calendar> from=ArgumentCaptor.forClass(Calendar.class),to=ArgumentCaptor.forClass(Calendar.class);
                        verify(query).setParameter(eq("fromDate"),from.capture(),eq(TemporalType.TIMESTAMP));
                        verify(query).setParameter(eq("toDate"),to.capture(),eq(TemporalType.TIMESTAMP));
                        assertThat(from.getValue().getTimeInMillis()).isEqualTo(start);
                        assertThat(to.getValue().getTimeInMillis()).isEqualTo(end);
                        assertThat(from.getValue().getTimeZone().getID()).isEqualTo("Asia/Kolkata");
                        assertThat(sql).contains("< :toDate").doesNotContain("<= :toDate");
                    }
                }
                // Both audit queries use exactly the millisecond before the SQL boundaries.
                verify(em,times((Arrays.asList("UTC","America/Los_Angeles","Asia/Kolkata").indexOf(zone)+1)*2))
                    .createNativeQuery(contains("FROM tbl_stock_aud a"));
                Query audit=queries.entrySet().stream().filter(e->e.getKey().contains("FROM tbl_stock_aud a")).findFirst().get().getValue();
                verify(audit).setParameter("cutoff",end-1);
            }
        } finally {TimeZone.setDefault(original);}
    }

    @Test void hibernateBindsBusinessCalendarToJdbcWithoutUsingServerTimezone() throws Exception {
        TimeZone original=TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            Calendar boundary=ReflectionTestUtils.invokeMethod(service,"businessBoundary",Timestamp.valueOf("2026-10-01 00:00:00"));
            java.sql.PreparedStatement statement=mock(java.sql.PreparedStatement.class);
            org.hibernate.type.descriptor.WrapperOptions options=mock(org.hibernate.type.descriptor.WrapperOptions.class);
            org.hibernate.type.descriptor.sql.TimestampTypeDescriptor.INSTANCE
                .getBinder(org.hibernate.type.descriptor.java.CalendarTypeDescriptor.INSTANCE)
                .bind(statement,boundary,1,options);
            ArgumentCaptor<Timestamp> timestamp=ArgumentCaptor.forClass(Timestamp.class);
            verify(statement).setTimestamp(eq(1),timestamp.capture(),same(boundary));
            assertThat(timestamp.getValue().toInstant()).isEqualTo(Instant.parse("2026-09-30T18:30:00Z"));
            verify(statement,never()).setTimestamp(anyInt(),any(Timestamp.class));
        } finally {TimeZone.setDefault(original);}
    }
}
