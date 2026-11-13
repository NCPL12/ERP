package com.ncpl.sales.service;

import com.ncpl.sales.model.*;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseGstExportTest {
    @Test void tallyPayloadUsesSavedEighteenPercentAfterMasterChangesToTwelve() throws Exception {
        ItemMasterService masters = mock(ItemMasterService.class);
        ItemMaster master = master();
        when(masters.getItemById("ITEM")).thenReturn(Optional.of(master));
        TallyPurchaseOrderService tally = new TallyPurchaseOrderService(masters);
        ReflectionTestUtils.setField(tally, "companyName", "Test Company");
        String purchaseLedger = "Inter-State-Purchase @ 18%", taxLedger = "Input Tax IGST@18%";
        Map<String,String> ledgers = new HashMap<>();
        for (String name : Arrays.asList("Vendor", purchaseLedger, taxLedger)) ledgers.put(name.toUpperCase(Locale.ROOT), name);
        Class<?> stockClass = Class.forName(TallyPurchaseOrderService.class.getName()+"$TallyStockItem");
        Constructor<?> stockConstructor = stockClass.getDeclaredConstructor(String.class, String.class); stockConstructor.setAccessible(true);
        Map<String,Object> stock = Collections.singletonMap("MODEL", stockConstructor.newInstance("MODEL", "Nos"));
        Class<?> snapshotClass = Class.forName(TallyPurchaseOrderService.class.getName()+"$MasterSnapshot");
        Constructor<?> snapshotConstructor = snapshotClass.getDeclaredConstructor(Map.class, Set.class, List.class, Map.class, Map.class, Map.class);
        snapshotConstructor.setAccessible(true);
        Object snapshot = snapshotConstructor.newInstance(ledgers,
                Collections.singleton(purchaseLedger.toUpperCase(Locale.ROOT)), Collections.singletonList(purchaseLedger),
                stock, Collections.emptyMap(), Collections.emptyMap());
        PurchaseOrder order = order();
        TallyPurchaseOrderService.PreparedOrder prepared = ReflectionTestUtils.invokeMethod(tally, "prepare", order, snapshot);
        String xml = tally.buildImportRequest(prepared);
        assertThat(xml).contains("<LEDGERNAME>Inter-State-Purchase @ 18%</LEDGERNAME>")
                .contains("<LEDGERNAME>Input Tax IGST@18%</LEDGERNAME>")
                .contains("<AMOUNT>-18.00</AMOUNT>").contains("<AMOUNT>118.00</AMOUNT>");
    }

    @Test void excelReportUsesEachLinesSavedGstIncludingZeroAndDecimal() throws Exception {
        ItemMasterService masters = mock(ItemMasterService.class);
        when(masters.getItemById("ITEM")).thenReturn(Optional.of(master()));
        SalesService sales = mock(SalesService.class);
        SalesItem salesLine = mock(SalesItem.class, RETURNS_DEEP_STUBS);
        when(salesLine.getSalesOrder().getParty().getPartyName()).thenReturn("Client");
        when(sales.getSalesItemObjById("SALES")).thenReturn(Optional.of(salesLine));
        PurchaseOrder order = order();
        List<PurchaseItem> lines = new ArrayList<>();
        for (String rate : Arrays.asList("18", "0", "0.25")) {
            PurchaseItem line = line(); line.setGstRate(new BigDecimal(rate)); lines.add(line);
        }
        order.setItems(lines);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("poList", new ArrayList<>(Collections.singletonList(order)));
        request.setAttribute("itemMasterService", masters); request.setAttribute("salesService", sales);
        PurchaseExcel export = new PurchaseExcel();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            export.buildExcelDocument(Collections.emptyMap(), workbook, request, new MockHttpServletResponse());
            for (int i=0; i<lines.size(); i++) {
                assertThat(workbook.getSheetAt(0).getRow(i+1).getCell(33).getNumericCellValue()).isEqualTo(lines.get(i).getGstRate().doubleValue());
                assertThat(workbook.getSheetAt(0).getRow(i+1).getCell(34).getNumericCellValue()).isEqualTo(lines.get(i).getGstRate().doubleValue());
            }
        } finally { export.context.close(); }
    }

    private ItemMaster master() {
        ItemMaster master = new ItemMaster(); master.setGst(12); master.setModel("MODEL"); master.setItemName("Item");
        Units unit = new Units(); unit.setName("Nos"); master.setItem_units(unit); return master;
    }
    private PurchaseItem line() {
        PurchaseItem line = new PurchaseItem(); line.setModelNo("ITEM"); line.setDescription("SALES");
        line.setQuantity(1); line.setUnitPrice(100); line.setAmount(100); line.setGstRate(new BigDecimal("18")); return line;
    }
    private PurchaseOrder order() {
        PurchaseOrder order = new PurchaseOrder(); order.setPoNumber("PO"); order.setCreated(Timestamp.valueOf("2026-11-06 00:00:00"));
        Party vendor = mock(Party.class, RETURNS_DEEP_STUBS); when(vendor.getPartyName()).thenReturn("Vendor");
        when(vendor.getInterState()).thenReturn("true"); order.setParty(vendor);
        order.setItems(Collections.singletonList(line())); return order;
    }
}
