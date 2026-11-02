package com.ncpl.sales.cashflow.service;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import java.sql.Date;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class InvoicePipelineServiceTest {
    final LocalDate day = LocalDate.of(2026,8,17);
    final String so = "SO-BLR-ELT-43637-2026";
    Map<String,Object> record(List<TallyLiveService.SalesVoucherLink> sales, String erpInvoices,
                              boolean salesFails, boolean possiblePurchase) throws Exception {
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        TallyLiveService tally=mock(TallyLiveService.class);
        Map<String,Object> row=new HashMap<>();
        row.put("vendor_invoice_no","V-100");row.put("vendor_name","Vendor");
        row.put("vendor_invoice_date",Date.valueOf(day));row.put("grn_date",Date.valueOf(day));
        row.put("so_number",so);row.put("client_name","Client");
        row.put("client_invoice_numbers",erpInvoices);
        when(jdbc.queryForList(anyString(),any(Date.class),any(Date.class)))
                .thenReturn(Collections.singletonList(row));
        when(tally.fetchPurchaseVouchers(any(),any())).thenReturn(Collections.singletonList(
                new TallyLiveService.PurchaseVoucherMatch("1",possiblePurchase?"V-101":"V-100","",day,
                        "Vendor","",100,Collections.emptyList())));
        if(salesFails)when(tally.fetchSalesVoucherLinks()).thenThrow(new Exception("unavailable"));
        else when(tally.fetchSalesVoucherLinks()).thenReturn(sales);
        Map<String,Object> data=new InvoicePipelineService(jdbc,tally).load(day,day);
        return ((List<Map<String,Object>>)data.get("records")).get(0);
    }
    TallyLiveService.SalesVoucherLink sales(String number, String reference) {
        return new TallyLiveService.SalesVoucherLink("2",number,day.plusMonths(2),"Client",Collections.singletonList(reference));
    }
    @Test void explicitSoLinksMultipleLaterSalesInvoices() throws Exception {
        Map<String,Object> r=record(Arrays.asList(sales("S1",so),sales("S2","ERP SO: "+so)),"",false,false);
        assertEquals("COMPLETE",r.get("status"));
        assertEquals(Arrays.asList("S1","S2"),r.get("clientInvoiceNumbers"));
        assertEquals("TALLY_SO_REFERENCE",r.get("clientLinkSource"));
    }
    @Test void clientNameOrWrongSoDoesNotLink() throws Exception {
        Map<String,Object> r=record(Arrays.asList(sales("S1","Client"),sales("S2",so+"0")),"",false,false);
        assertEquals("CLIENT_INVOICE_MISSING",r.get("status"));
    }
    @Test void salesFailureRequiresReviewAndPreservesErpLinks() throws Exception {
        assertEquals("REVIEW_REQUIRED",record(Collections.emptyList(),"",true,false).get("status"));
        assertEquals("COMPLETE",record(Collections.emptyList(),"ERP-INV1",true,false).get("status"));
    }
    @Test void possibleVendorMatchCannotBecomeLinked() throws Exception {
        assertEquals("REVIEW_REQUIRED",record(Collections.singletonList(sales("S1",so)),"",false,true).get("status"));
    }
    @Test void extractsBoundedReferencesWithMultipleSosAndNoDuplicates() {
        assertEquals(Arrays.asList("SOBLRELT436372026","SOBLRELT1232025"),
                InvoicePipelineService.salesOrderReferences(Arrays.asList(so,"SO/BLR/ELT/123/2025, "+so,so+"X","X"+so)));
    }
}
