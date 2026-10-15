package com.ncpl.sales.service;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.ncpl.sales.model.*;
import java.util.*;
import java.time.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class TallyVerificationFlowTest {
    @Test void duplicateTallyNumbersBlockBeforeAnyMasterOrVoucherWrite() throws Exception {run(true,false);}
    @Test void matchingTotalWithWrongInventoryNeverCreatesVerifiedCheckpoint() throws Exception {run(false,false);}
    @Test void wrongPurchaseLedgerNeverCreatesVerifiedCheckpoint() throws Exception {run(false,true);}
    private void run(boolean duplicate, boolean wrongLedger) throws Exception {
        AtomicInteger writes=new AtomicInteger();
        com.sun.net.httpserver.HttpServer server=com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[2048];int n;
            while((n=exchange.getRequestBody().read(buffer))!=-1)bytes.write(buffer,0,n);
            byte[] body=bytes.toByteArray();String request=new String(body,body.length>1&&body[1]==0?StandardCharsets.UTF_16LE:StandardCharsets.UTF_8);
            String response="<ENVELOPE/>";
            if(request.contains("<TALLYREQUEST>Import")){writes.incrementAndGet();response="<RESPONSE><CREATED>1</CREATED></RESPONSE>";}
            else if(request.contains("ErpVerifiedPurchaseOrders"))response="<ENVELOPE>"+(wrongLedger ? AssignedTallyIssuesTest.savedVoucher().replace("State Purchase@0%","Wrong Purchase") : AssignedTallyIssuesTest.savedVoucher().replace("1.234 Mtr","1.230 Mtr"))+"</ENVELOPE>";
            else if(request.contains("ErpExistingPurchaseOrders")&&duplicate)response="<ENVELOPE>"+voucher("123")+voucher("124")+"</ENVELOPE>";
            else if(request.contains("ErpTallyGroups"))response="<ENVELOPE><GROUP NAME=\"Purchase Accounts\"><PARENT>Primary</PARENT></GROUP></ENVELOPE>";
            else if(request.contains("ErpTallyLedgers"))response="<ENVELOPE><LEDGER NAME=\"Vendor\"><PARENT>Sundry Creditors</PARENT></LEDGER><LEDGER NAME=\"State Purchase@0%\"><PARENT>Purchase Accounts</PARENT></LEDGER></ENVELOPE>";
            else if(request.contains("ErpTallyStockItems"))response="<ENVELOPE><STOCKITEM NAME=\"Cable\"><BASEUNITS>Mtr</BASEUNITS></STOCKITEM></ENVELOPE>";
            else if(request.contains("ErpTallyUnits"))response="<ENVELOPE><UNIT NAME=\"Mtr\"><DECIMALPLACES>3</DECIMALPLACES></UNIT></ENVELOPE>";
            byte[] out=response.getBytes(StandardCharsets.UTF_8);
            try{exchange.sendResponseHeaders(200,out.length);exchange.getResponseBody().write(out);}finally{exchange.close();}
        });server.start();
        Path state=Paths.get("target","verification-flow-"+UUID.randomUUID()+".properties").toAbsolutePath();
        try {
            ItemMasterService items=mock(ItemMasterService.class);ItemMaster im=new ItemMaster();im.setModel("Cable");im.setItemName("Cable");Units units=new Units();units.setName("Mtr");im.setItem_units(units);
            when(items.getItemById("ITEM-1")).thenReturn(Optional.of(im));
            TallyPurchaseOrderService service=new TallyPurchaseOrderService(items);
            ReflectionTestUtils.setField(service,"serverUrl","http://127.0.0.1:"+server.getAddress().getPort());
            ReflectionTestUtils.setField(service,"companyName","Company A");ReflectionTestUtils.setField(service,"syncStateFile",state.toString());
            PurchaseOrder po=new PurchaseOrder();po.setPoNumber("PO-1");po.setCreated(Date.from(Instant.parse("2026-10-01T06:00:00Z")));Party party=new Party();party.setPartyName("Vendor");po.setParty(party);
            PurchaseItem item=new PurchaseItem();item.setModelNo("ITEM-1");item.setQuantity(1.234f);item.setUnitPrice(10);item.setAmount(12.34f);po.setItems(Collections.singletonList(item));
            TallyPurchaseOrderService.ExportResult result=service.exportAllPending(Collections.singletonList(po));
            assertThat(result.getFailed()).isEqualTo(1);assertThat(result.getImported()).isZero();
            if(duplicate){assertThat(writes.get()).isZero();assertThat(result.getRecords().get(0).getMessage()).contains("Multiple Tally POs");}
            else {assertThat(writes.get()).isEqualTo(1);assertThat(result.getRecords().get(0).getMessage()).contains("verification failed").contains("inventory");}
            assertThat((Properties)ReflectionTestUtils.getField(service,"verifiedStates")).isEmpty();
        }finally{server.stop(0);Files.deleteIfExists(state);}
    }
    private String voucher(String id){return "<VOUCHER><VOUCHERNUMBER>PO-1</VOUCHERNUMBER><DATE>20261001</DATE><MASTERID>"+id+"</MASTERID><GUID>guid-"+id+"</GUID><NARRATION>Imported from ERP</NARRATION></VOUCHER>";}
}
