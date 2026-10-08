package com.ncpl.sales.cashflow.service;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class TallySalesVoucherLinkTest {
    @Test void readsReferencesAndExcludesCancelledAndOptionalInvoices() throws Exception {
        AtomicReference<String> request=new AtomicReference<>();
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{
            request.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            String xml="<ENVELOPE><HEADER><STATUS>1</STATUS></HEADER><BODY><DATA><COLLECTION>"
                +"<VOUCHER><MASTERID>42</MASTERID><VOUCHERNUMBER>S1</VOUCHERNUMBER><DATE>20261001</DATE>"
                +"<REFERENCE>SO-BLR-ELT-123-2026</REFERENCE><PARTYLEDGERNAME>Client</PARTYLEDGERNAME>"
                +"<ALLINVENTORYENTRIES.LIST><ORDERNO>SO-BLR-ELT-124-2026</ORDERNO></ALLINVENTORYENTRIES.LIST></VOUCHER>"
                +"<VOUCHER><VOUCHERNUMBER>S2</VOUCHERNUMBER><ISCANCELLED>Yes</ISCANCELLED></VOUCHER>"
                +"<VOUCHER><VOUCHERNUMBER>S3</VOUCHERNUMBER><ISOPTIONAL>Yes</ISOPTIONAL></VOUCHER>"
                +"</COLLECTION></DATA></BODY></ENVELOPE>";
            byte[] bytes=xml.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });server.start();
        try {
            TallyLiveService service=new TallyLiveService();
            ReflectionTestUtils.setField(service,"serverUrl","http://127.0.0.1:"+server.getAddress().getPort());
            ReflectionTestUtils.setField(service,"companyName","");
            List<TallyLiveService.SalesVoucherLink> result=service.fetchSalesVoucherLinks();
            assertEquals(1,result.size());assertEquals("S1",result.get(0).voucherNumber);
            assertEquals(2,result.get(0).references.size());
            assertTrue(request.get().contains("$$IsSales:$VoucherTypeName"));
            assertFalse(request.get().contains("$Date"));
        } finally {server.stop(0);}
    }
}
