package com.ncpl.sales.cashflow.service;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TallyJava8HttpTest {
    @Test void exportsCompanyScopedBillsOverHttpAndKeepsClassification() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        List<String> requests=new ArrayList<>();
        server.createContext("/",exchange -> {
            ByteArrayOutputStream input=new ByteArrayOutputStream();
            byte[] buffer=new byte[1024]; int n;
            while((n=exchange.getRequestBody().read(buffer))!=-1) input.write(buffer,0,n);
            String request=new String(input.toByteArray(),StandardCharsets.UTF_8); requests.add(request);
            String xml="<ENVELOPE><HEADER><STATUS>1</STATUS></HEADER><BODY><DATA><COLLECTION>";
            if(request.contains("<TYPE>Bill</TYPE>")) {
                xml+="<BILL NAME=\"INV-1\"><PARENT>Customer</PARENT><BILLDATE>20260101</BILLDATE><BILLCREDITPERIOD>30 Days</BILLCREDITPERIOD><CLOSINGBALANCE>-1200.00</CLOSINGBALANCE></BILL>";
                xml+="<BILL NAME=\"ADV-1\"><ISADVANCE>Yes</ISADVANCE><PARENT>Customer</PARENT><BILLDATE>20260101</BILLDATE><CLOSINGBALANCE>100</CLOSINGBALANCE></BILL>";
            }
            xml+="</COLLECTION></DATA></BODY></ENVELOPE>";
            byte[] response=xml.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,response.length); exchange.getResponseBody().write(response);exchange.close();
        });
        server.start();
        try {
            TallyLiveService service=new TallyLiveService();
            ReflectionTestUtils.setField(service,"serverUrl","http://127.0.0.1:"+server.getAddress().getPort());
            ReflectionTestUtils.setField(service,"companyName","Test & Company");
            List<com.ncpl.sales.cashflow.model.Invoice> bills=service.fetchOutstandingBills();
            assertEquals(1,bills.size()); assertEquals(1200,bills.get(0).getInvoiceValue());
            assertEquals(java.time.LocalDate.of(2026,1,31),bills.get(0).getDueDate());
            assertEquals(com.ncpl.sales.cashflow.model.Invoice.InvoiceType.INFLOW,bills.get(0).getType());
            assertEquals(3,requests.size());
            for(String request:requests) {
                assertTrue(request.contains("<TALLYREQUEST>Export</TALLYREQUEST>"));
                assertTrue(request.contains("<SVCURRENTCOMPANY>Test &amp; Company</SVCURRENTCOMPANY>"));
            }
        } finally { server.stop(0); }
    }
}
