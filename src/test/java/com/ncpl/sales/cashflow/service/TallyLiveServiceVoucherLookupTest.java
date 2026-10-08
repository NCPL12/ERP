package com.ncpl.sales.cashflow.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TallyLiveServiceVoucherLookupTest {
    @Test
    void findsExactVoucherWhenPersistedBillDateIsOneDayEarlier() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> requestBody = new AtomicReference<>();
        server.createContext("/", exchange -> {
            ByteArrayOutputStream input = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = exchange.getRequestBody().read(buffer)) != -1) input.write(buffer, 0, count);
            requestBody.set(new String(input.toByteArray(), StandardCharsets.UTF_8));
            String xml = "<ENVELOPE><HEADER><STATUS>1</STATUS></HEADER><BODY><DATA><COLLECTION>"
                    + "<VOUCHER><MASTERID>67726</MASTERID><DATE>20260817</DATE>"
                    + "<VOUCHERTYPENAME>Sales</VOUCHERTYPENAME><VOUCHERNUMBER>BLR2026260</VOUCHERNUMBER>"
                    + "<PARTYLEDGERNAME>L&amp;T Technology Services Limited</PARTYLEDGERNAME>"
                    + "<ALLLEDGERENTRIES.LIST><LEDGERNAME>L&amp;T Technology Services Limited</LEDGERNAME>"
                    + "<AMOUNT>-23802370.00</AMOUNT><BILLALLOCATIONS.LIST><NAME>BLR2026260</NAME>"
                    + "<BILLTYPE>New Ref</BILLTYPE><AMOUNT>-23802370.00</AMOUNT></BILLALLOCATIONS.LIST>"
                    + "</ALLLEDGERENTRIES.LIST></VOUCHER></COLLECTION></DATA></BODY></ENVELOPE>";
            byte[] response = xml.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            TallyLiveService service = new TallyLiveService();
            ReflectionTestUtils.setField(service, "serverUrl", "http://127.0.0.1:" + server.getAddress().getPort());
            ReflectionTestUtils.setField(service, "companyName", "Neptune Controls Pvt Ltd.");

            TallyLiveService.VoucherDetails details = service.fetchVoucherDetails(
                    "BLR2026260", "L&T Technology Services Limited", LocalDate.of(2026, 8, 16));

            assertTrue(details.found());
            assertEquals("BLR2026260", details.voucherNumber());
            assertEquals(LocalDate.of(2026, 8, 17), details.voucherDate());
            assertEquals(23802370d, details.originalInvoiceValue(), 0.01d);
            assertEquals("Exact bill and party match", details.matchedBy());
            assertTrue(requestBody.get().contains("$Date &gt;= $$Date:\"14-Aug-2026\""));
            assertTrue(requestBody.get().contains("$Date &lt;= $$Date:\"18-Aug-2026\""));
        } finally {
            server.stop(0);
        }
    }
}
