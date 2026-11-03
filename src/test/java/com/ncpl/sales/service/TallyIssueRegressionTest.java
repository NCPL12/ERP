package com.ncpl.sales.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ncpl.sales.model.*;
import com.ncpl.sales.repository.PurchaseRepo;
import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TallyIssueRegressionTest {
    @Test void masterRejectionFlowsFromHttpResponseToPoError() throws Exception {
        com.sun.net.httpserver.HttpServer server=com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            byte[] response="<ENVELOPE><ERRORS>1</ERRORS><LINEERROR>Unknown stock group: Billable Materials</LINEERROR></ENVELOPE>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            try { while(exchange.getRequestBody().read()!=-1) {} exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response); }
            finally { exchange.close(); }
        });
        server.start();
        try {
            ItemMasterService items=mock(ItemMasterService.class);
            ItemMaster im=new ItemMaster();im.setModel("Screws");im.setItemName("Screws");Units unit=new Units();unit.setName("Nos");im.setItem_units(unit);
            when(items.getItemById("ITEM-1")).thenReturn(Optional.of(im));
            TallyPurchaseOrderService service=new TallyPurchaseOrderService(items);
            ReflectionTestUtils.setField(service,"serverUrl","http://127.0.0.1:"+server.getAddress().getPort());
            ReflectionTestUtils.setField(service,"companyName","Test Company");
            Object snapshot=snapshot(Collections.singletonMap("NOS","Nos"));
            ((Map)ReflectionTestUtils.getField(snapshot,"ledgerNames")).put("VENDOR","Vendor");
            Party vendor=new Party();vendor.setPartyName("Vendor");PurchaseOrder order=new PurchaseOrder();order.setParty(vendor);order.setPoNumber("PO-1");order.setCreated(new Date());
            PurchaseItem item=new PurchaseItem();item.setModelNo("ITEM-1");order.setItems(Collections.singletonList(item));
            Method ensure=TallyPurchaseOrderService.class.getDeclaredMethod("ensureRequiredMasters",List.class,snapshot.getClass());ensure.setAccessible(true);ensure.invoke(service,Collections.singletonList(order),snapshot);
            Method prepare=TallyPurchaseOrderService.class.getDeclaredMethod("prepare",PurchaseOrder.class,snapshot.getClass());prepare.setAccessible(true);
            assertThatThrownBy(()->prepare.invoke(service,order,snapshot)).hasStackTraceContaining("Unknown stock group: Billable Materials");
        } finally {server.stop(0);}
    }
    @Test void statusEndpointSerializesValuesRatherThanSpringBean() throws Exception {
        com.ncpl.sales.controller.SalesController controller=new com.ncpl.sales.controller.SalesController();
        TallyPurchaseOrderAutoSync sync=mock(TallyPurchaseOrderAutoSync.class);
        when(sync.isEnabled()).thenReturn(true);when(sync.isConfigured()).thenReturn(false);
        when(sync.getConfigurationError()).thenReturn("Starting date missing");
        ReflectionTestUtils.setField(controller,"tallyPoAutoSync",sync);
        Map<String,Object> status=controller.tallyPoSyncStatus();
        assertThat(status).containsEntry("enabled",true).containsEntry("configured",false);
        String json=new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(status);
        assertThat(json).contains("Starting date missing").doesNotContain("advisors");
    }
    @Test void checkpointPathRemainsStableAcrossWorkingDirectories() throws Exception {
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
        Method path=TallyPurchaseOrderService.class.getDeclaredMethod("syncStatePath");path.setAccessible(true);
        java.nio.file.Path expected=java.nio.file.Paths.get(com.ncpl.common.Constants.FILE_LOCATION,"data/tally-po-sync.properties");
        assertThat(path.invoke(service)).isEqualTo(expected);
        String absolute=java.nio.file.Paths.get(com.ncpl.common.Constants.FILE_LOCATION,"test-checkpoint.properties").toAbsolutePath().toString();
        ReflectionTestUtils.setField(service,"syncStateFile",absolute);
        assertThat(path.invoke(service)).isEqualTo(java.nio.file.Paths.get(absolute));
    }
    @Test void dateConversionDoesNotDependOnServerTimeZone() {
        TimeZone previous=TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
            assertThat(service.toLocalDate(Date.from(Instant.parse("2026-09-30T19:00:00Z"))))
                .isEqualTo(LocalDate.of(2026,10,1));
        } finally {TimeZone.setDefault(previous);}
    }
    @Test void deadlineBoundsBlockedRequestsAndSubsequentRequestsStillWork() throws Exception {
        CountDownLatch finished=new CountDownLatch(1);
        long start=System.nanoTime();
        assertThatThrownBy(()->TallyRequestDeadline.call(()->{
            try { Thread.sleep(5000); return "late"; } finally { finished.countDown(); }
        },100)).isInstanceOf(IllegalStateException.class).hasMessageContaining("timed out");
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start)).isLessThan(2000);
        assertThat(finished.await(2,TimeUnit.SECONDS)).isTrue();
        assertThat(TallyRequestDeadline.call(()->"ready",1000)).isEqualTo("ready");
    }
    @Test void missingTallyCompanyIsVisibleBeforeFirstScheduledRun() {
        TallyPurchaseOrderService exporter=new TallyPurchaseOrderService(null);
        ReflectionTestUtils.setField(exporter,"companyName"," ");
        TallyPurchaseOrderAutoSync sync=new TallyPurchaseOrderAutoSync(mock(PurchaseRepo.class),exporter);
        ReflectionTestUtils.setField(sync,"enabled",true);
        assertThat(sync.isConfigured()).isFalse();
        assertThat(sync.getConfigurationError()).contains("cashflow.tally.company-name");
    }
    @Test void unitsResolveAliasesAndExplicitMappingsWithoutPackSizeGuessing() throws Exception {
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
        Object snapshot=snapshot(Collections.singletonMap("PCS","Pcs"));
        assertThat(unit(service,snapshot,"Nos")).isEqualTo("Pcs");
        snapshot=snapshot(Collections.singletonMap("PKT","Pkt"));
        assertThat(unit(service,snapshot,"Pack of 10")).isNull();
        ReflectionTestUtils.setField(service,"unitMappings","Packet=Pkt");
        assertThat(unit(service,snapshot,"Packet")).isEqualTo("Pkt");
        ReflectionTestUtils.setField(service,"unitMappings","Packet=Missing");
        Object current=snapshot;
        assertThatThrownBy(()->unit(service,current,"Packet")).hasCauseInstanceOf(IllegalArgumentException.class).hasStackTraceContaining("Configured Tally unit 'Missing' does not exist for ERP unit 'Packet'.");
    }
    @Test void keepsAllTallyImportErrors() throws Exception {
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
        Method parse=TallyPurchaseOrderService.class.getDeclaredMethod("parseImportResponse",String.class);parse.setAccessible(true);
        Object response=parse.invoke(service,"<ENVELOPE><LINEERROR>Unknown stock group</LINEERROR><LINEERROR>Invalid unit</LINEERROR></ENVELOPE>");
        assertThat(ReflectionTestUtils.getField(response,"lineError")).isEqualTo("Unknown stock group; Invalid unit");
    }
    @Test void originalStockFailureIsReturnedInsteadOfNotFound() throws Exception {
        ItemMasterService items=mock(ItemMasterService.class);
        ItemMaster im=new ItemMaster();im.setModel("Screws");im.setItemName("Screws");
        when(items.getItemById("ITEM-1")).thenReturn(Optional.of(im));
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(items);
        Object snapshot=snapshot(Collections.emptyMap());
        ((Map)ReflectionTestUtils.getField(snapshot,"ledgerNames")).put("VENDOR","Vendor");
        ((Map)ReflectionTestUtils.getField(snapshot,"masterErrors")).put("SCREWS","Tally rejected stock item: Unknown stock group");
        Party vendor=new Party();vendor.setPartyName("Vendor");
        PurchaseOrder order=new PurchaseOrder();order.setParty(vendor);order.setPoNumber("PO-1");order.setCreated(new Date());
        PurchaseItem item=new PurchaseItem();item.setModelNo("ITEM-1");order.setItems(Collections.singletonList(item));
        Method prepare=TallyPurchaseOrderService.class.getDeclaredMethod("prepare",PurchaseOrder.class,snapshot.getClass());prepare.setAccessible(true);
        assertThatThrownBy(()->prepare.invoke(service,order,snapshot)).hasCauseInstanceOf(IllegalArgumentException.class).hasStackTraceContaining("Tally rejected stock item: Unknown stock group");
    }
    private Object snapshot(Map<String,String> units) throws Exception {
        Class<?> type=Class.forName(TallyPurchaseOrderService.class.getName()+"$MasterSnapshot");
        Constructor<?> c=type.getDeclaredConstructor(Map.class,Set.class,List.class,Map.class,Map.class,Map.class);c.setAccessible(true);
        return c.newInstance(new HashMap<>(),new HashSet<>(),new ArrayList<>(),new HashMap<>(),units,new HashMap<>());
    }
    private String unit(TallyPurchaseOrderService service,Object snapshot,String unit) throws Exception {
        Method m=TallyPurchaseOrderService.class.getDeclaredMethod("resolveTallyUnit",String.class,snapshot.getClass());m.setAccessible(true);
        return (String)m.invoke(service,unit,snapshot);
    }
}
