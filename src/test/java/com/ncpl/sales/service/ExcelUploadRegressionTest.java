package com.ncpl.sales.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesItemRepo;
import com.ncpl.sales.repository.SalesRepo;

public class ExcelUploadRegressionTest {
    private MockMultipartFile excel(XSSFWorkbook workbook, String name) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new MockMultipartFile("file", name, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
    }

    private void poRow(Sheet sheet, int number, String po, float quantity) {
        Row row = sheet.createRow(number);
        row.createCell(0).setCellValue(po);
        row.createCell(1).setCellValue("Widget");
        row.createCell(5).setCellValue("M1");
        row.createCell(6).setCellValue("HSN");
        row.createCell(7).setCellValue(quantity);
        row.createCell(8).setCellValue(10);
    }

    @Test
    public void poAcceptsB1VendorAndReportsQuantityAcrossRows() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(1).setCellValue("Vendor");
        poRow(sheet, 5, " PO / 1 ", 8);
        poRow(sheet, 6, "", 5);
        poRow(sheet, 7, "", 3);
        sheet.addMergedRegion(new CellRangeAddress(5, 7, 0, 0));
        sheet.createRow(9).createCell(1).setCellValue(" ");

        PurchaseUploadExcel upload = new PurchaseUploadExcel();
        PartyRepo parties = mock(PartyRepo.class);
        SalesRepo salesOrders = mock(SalesRepo.class);
        ItemMasterService items = mock(ItemMasterService.class);
        SalesService sales = mock(SalesService.class);
        PurchaseOrderService purchases = mock(PurchaseOrderService.class);
        PurchaseItemService purchaseItems = mock(PurchaseItemService.class);
        ReflectionTestUtils.setField(upload, "partyRepo", parties);
        ReflectionTestUtils.setField(upload, "salesRepo", salesOrders);
        ReflectionTestUtils.setField(upload, "itemService", items);
        ReflectionTestUtils.setField(upload, "salesService", sales);
        ReflectionTestUtils.setField(upload, "purchaseService", purchases);
        ReflectionTestUtils.setField(upload, "purchaseItemService", purchaseItems);

        Party party = new Party(); party.setId("V1");
        SalesOrder salesOrder = new SalesOrder();
        ItemMaster item = new ItemMaster(); item.setId("I1");
        SalesItem salesItem = new SalesItem(); salesItem.setId("S1");
        DesignItems design = new DesignItems(); design.setItemId("I1"); design.setQuantity(10);
        PurchaseOrder earlierPo = new PurchaseOrder(); earlierPo.setPoNumber("PO-OLD");
        PurchaseItem earlierItem = new PurchaseItem(); earlierItem.setQuantity(3); earlierItem.setPurchaseOrder(earlierPo);
        when(parties.findByName("Vendor")).thenReturn(party);
        when(salesOrders.getSalesOrderByClientPoNumber("PO/1")).thenReturn(salesOrder);
        when(items.getItemByModelNo("M1")).thenReturn(item);
        when(sales.getSalesItemByName("Widget", "PO/1", "I1")).thenReturn(salesItem);
        when(purchases.getDesignListOfItemBySalesItemId("S1")).thenReturn(Collections.singletonList(design));
        when(purchaseItems.getPurchaseItemListBySalesItemIdAndItemId("S1", "I1"))
                .thenReturn(Collections.singletonList(earlierItem));

        List<String> errors = upload.processExcelFile(excel(workbook, "po.xlsx"));
        assertEquals(3, errors.size());
        assertTrue(errors.get(0).contains("2 row(s) failed"));
        assertTrue(errors.get(1).contains("Remaining Quantity available: 7.0"));
        assertTrue(errors.get(1).contains("PO-OLD"));
        assertTrue(errors.get(2).contains("Remaining Quantity available: 2.0"));
        assertTrue(errors.get(2).contains("in this file: 5.0"));
        verify(purchases, never()).savePurchaseOrder(any(PurchaseOrder.class), anyString(), anyString());
    }

    @Test
    public void poAlsoAcceptsLegacyA2Vendor() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(1).createCell(0).setCellValue("Vendor");
        poRow(sheet, 5, "UNKNOWN", 1);
        poRow(sheet, 6, "", 1);
        PurchaseUploadExcel upload = new PurchaseUploadExcel();
        PartyRepo parties = mock(PartyRepo.class);
        Party party = new Party(); party.setId("V1");
        when(parties.findByName("Vendor")).thenReturn(party);
        ReflectionTestUtils.setField(upload, "partyRepo", parties);
        ReflectionTestUtils.setField(upload, "salesRepo", mock(SalesRepo.class));
        ReflectionTestUtils.setField(upload, "itemService", mock(ItemMasterService.class));
        List<String> errors = upload.processExcelFile(excel(workbook, "po.xlsx"));
        assertEquals(3, errors.size());
        assertTrue(errors.get(1).contains("Invalid Client PO Number: UNKNOWN"));
        assertTrue(errors.get(2).contains("Client PO Number is mandatory"));
        verify(parties).findByName("Vendor");
    }

    @Test
    public void poDoesNotSaveAnEmptyWorkbook() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(1).createCell(0).setCellValue("Vendor");
        sheet.createRow(5).createCell(0).setCellValue(" ");
        PurchaseUploadExcel upload = new PurchaseUploadExcel();
        PartyRepo parties = mock(PartyRepo.class);
        PurchaseOrderService purchases = mock(PurchaseOrderService.class);
        Party party = new Party(); party.setId("V1");
        when(parties.findByName("Vendor")).thenReturn(party);
        ReflectionTestUtils.setField(upload, "partyRepo", parties);
        ReflectionTestUtils.setField(upload, "purchaseService", purchases);
        assertEquals(Collections.singletonList("No purchase items found in the Excel file."),
                upload.processExcelFile(excel(workbook, "po.xlsx")));
        verify(purchases, never()).savePurchaseOrder(any(PurchaseOrder.class), anyString(), anyString());
    }

    @Test
    public void poReportsAnAlreadyUploadedFileWithoutSavingIt() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(1).createCell(0).setCellValue("Vendor");
        poRow(sheet, 5, "PO-1", 2);
        PurchaseUploadExcel upload = new PurchaseUploadExcel();
        PartyRepo parties = mock(PartyRepo.class);
        SalesRepo salesOrders = mock(SalesRepo.class);
        ItemMasterService items = mock(ItemMasterService.class);
        SalesService sales = mock(SalesService.class);
        PurchaseOrderService purchases = mock(PurchaseOrderService.class);
        PurchaseItemService purchaseItems = mock(PurchaseItemService.class);
        ReflectionTestUtils.setField(upload, "partyRepo", parties);
        ReflectionTestUtils.setField(upload, "salesRepo", salesOrders);
        ReflectionTestUtils.setField(upload, "itemService", items);
        ReflectionTestUtils.setField(upload, "salesService", sales);
        ReflectionTestUtils.setField(upload, "purchaseService", purchases);
        ReflectionTestUtils.setField(upload, "purchaseItemService", purchaseItems);
        Party party = new Party(); party.setId("V1");
        ItemMaster item = new ItemMaster(); item.setId("I1");
        SalesItem salesItem = new SalesItem(); salesItem.setId("S1");
        DesignItems design = new DesignItems(); design.setItemId("I1"); design.setQuantity(2);
        PurchaseOrder prior = new PurchaseOrder(); prior.setPoNumber("PO-OLD");
        PurchaseItem priorItem = new PurchaseItem(); priorItem.setQuantity(2); priorItem.setPurchaseOrder(prior);
        when(parties.findByName("Vendor")).thenReturn(party);
        when(salesOrders.getSalesOrderByClientPoNumber("PO-1")).thenReturn(new SalesOrder());
        when(items.getItemByModelNo("M1")).thenReturn(item);
        when(sales.getSalesItemByName("Widget", "PO-1", "I1")).thenReturn(salesItem);
        when(purchases.getDesignListOfItemBySalesItemId("S1")).thenReturn(Collections.singletonList(design));
        when(purchaseItems.getPurchaseItemListBySalesItemIdAndItemId("S1", "I1"))
                .thenReturn(Collections.singletonList(priorItem));

        List<String> errors = upload.processExcelFile(excel(workbook, "po.xlsx"));
        assertEquals(2, errors.size());
        assertTrue("errors=" + errors, errors.get(0).contains("1 row(s) failed"));
        assertTrue("errors=" + errors, errors.get(1).contains("already uploaded"));
        assertTrue(errors.get(1).contains("PO-OLD"));
        verify(purchases, never()).savePurchaseOrder(any(PurchaseOrder.class), anyString(), anyString());
    }

    @Test
    public void soSkipsBlankRowsAndReadsHeadingUnitsAndDecimals() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row header = sheet.createRow(2);
        header.createCell(0).setCellValue("CLIENT-1");
        header.createCell(1).setCellValue(new java.util.Date());
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("mm/dd/yyyy"));
        header.getCell(1).setCellStyle(dateStyle);
        header.createCell(2).setCellValue("Customer");
        header.createCell(4).setCellValue(0);
        Row item = sheet.createRow(6);
        item.createCell(1).setCellValue("Widget");
        item.createCell(5).setCellValue(6.1);
        item.createCell(8).setCellValue(1.5);
        item.createCell(9).setCellValue(" Nos.\u00a0 ");
        item.createCell(10).setCellValue("2.25");
        item.createCell(11).setCellValue("0.5");
        sheet.createRow(7).createCell(1).setCellValue(" ");
        Row heading = sheet.createRow(9);
        heading.createCell(1).setCellValue("Section");
        heading.createCell(9).setCellValue("Heading");

        SOUploadService upload = new SOUploadService();
        SalesRepo salesOrders = mock(SalesRepo.class);
        PartyRepo parties = mock(PartyRepo.class);
        ItemMasterService items = mock(ItemMasterService.class);
        SalesService sales = mock(SalesService.class);
        ReflectionTestUtils.setField(upload, "salesOrderRepository", salesOrders);
        ReflectionTestUtils.setField(upload, "partyrepo", parties);
        ReflectionTestUtils.setField(upload, "itemService", items);
        ReflectionTestUtils.setField(upload, "salesService", sales);
        ReflectionTestUtils.setField(upload, "salesItemRepository", mock(SalesItemRepo.class));
        Party party = new Party(); party.setId("C1");
        Units nos = new Units(); nos.setId(1L); nos.setName("Nos");
        Units headingUnit = new Units(); headingUnit.setId(2L); headingUnit.setName("Heading");
        when(salesOrders.getSalesOrderByClientPoNo("CLIENT-1")).thenReturn(Optional.empty());
        when(parties.findByName("Customer")).thenReturn(party);
        when(items.getUnitList()).thenReturn(Arrays.asList(nos, headingUnit));

        List<String> errors = upload.parseExcelFile(excel(workbook, "so.xlsx"));
        assertTrue(errors.toString(), errors.isEmpty());
        ArgumentCaptor<SalesOrder> captured = ArgumentCaptor.forClass(SalesOrder.class);
        verify(sales).savesales(captured.capture(), eq("C1"));
        List<SalesItem> saved = captured.getValue().getItems();
        assertEquals(2, saved.size());
        assertEquals("6.1", saved.get(0).getModelNo());
        assertEquals(1.5f, saved.get(0).getQuantity(), 0.001f);
        assertEquals(2.25f, saved.get(0).getUnitPrice(), 0.001f);
        assertEquals(0f, saved.get(1).getQuantity(), 0.001f);
        assertEquals(headingUnit, saved.get(1).getItem_units());
    }

    @Test
    public void soUploadKeepsNewLinesWhenAClientPoAlreadyExists() {
        SalesItem oldLine = new SalesItem();
        oldLine.setSlNo("1");
        oldLine.setDescription("Widget");
        oldLine.setModelNo("M1");
        oldLine.setUnit("1");
        oldLine.setQuantity(2);
        oldLine.setUnitPrice(10);

        SalesItem repeatedLine = new SalesItem();
        repeatedLine.setSlNo("1");
        repeatedLine.setDescription("Widget");
        repeatedLine.setModelNo("M1");
        repeatedLine.setUnit("1");
        repeatedLine.setQuantity(2);
        repeatedLine.setUnitPrice(10);

        SalesItem newLine = new SalesItem();
        newLine.setSlNo("2");
        newLine.setDescription("Gadget");
        newLine.setModelNo("M2");
        newLine.setUnit("1");
        newLine.setQuantity(1);
        newLine.setUnitPrice(20);

        assertTrue(SOUploadService.newSalesItems(
                Collections.singletonList(oldLine), Collections.singletonList(repeatedLine)).isEmpty());
        assertEquals(Collections.singletonList(newLine), SOUploadService.newSalesItems(
                Collections.singletonList(oldLine), Arrays.asList(repeatedLine, newLine)));
    }

    @Test
    public void soAcceptsTermsShiftedFromFToG() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row header = sheet.createRow(2);
        header.createCell(0).setCellValue("CLIENT-2");
        header.createCell(1).setCellValue(new java.util.Date());
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("mm/dd/yyyy"));
        header.getCell(1).setCellStyle(dateStyle);
        header.createCell(2).setCellValue("Customer");
        header.createCell(4).setCellValue(0);
        header.createCell(6).setCellValue("Net 30");
        header.createCell(11).setCellValue("Other terms");
        Row item = sheet.createRow(6);
        item.createCell(1).setCellValue("Widget");
        item.createCell(8).setCellValue(1);
        item.createCell(9).setCellValue("Nos");
        item.createCell(10).setCellValue(2);
        item.createCell(11).setCellValue(0);

        SOUploadService upload = new SOUploadService();
        SalesRepo salesOrders = mock(SalesRepo.class);
        PartyRepo parties = mock(PartyRepo.class);
        ItemMasterService items = mock(ItemMasterService.class);
        SalesService sales = mock(SalesService.class);
        ReflectionTestUtils.setField(upload, "salesOrderRepository", salesOrders);
        ReflectionTestUtils.setField(upload, "partyrepo", parties);
        ReflectionTestUtils.setField(upload, "itemService", items);
        ReflectionTestUtils.setField(upload, "salesService", sales);
        Party party = new Party(); party.setId("C1");
        Units nos = new Units(); nos.setId(1L); nos.setName("Nos");
        when(salesOrders.getSalesOrderByClientPoNo("CLIENT-2")).thenReturn(Optional.empty());
        when(parties.findByName("Customer")).thenReturn(party);
        when(items.getUnitList()).thenReturn(Collections.singletonList(nos));

        assertTrue(upload.parseExcelFile(excel(workbook, "so.xlsx")).isEmpty());
        ArgumentCaptor<SalesOrder> captured = ArgumentCaptor.forClass(SalesOrder.class);
        verify(sales).savesales(captured.capture(), eq("C1"));
        assertEquals("Net 30", captured.getValue().getModeOfPayment());
        assertEquals("Other terms", captured.getValue().getOtherTermsAndConditions());
    }

    @Test
    public void alternateSoUploadRejectsAnExistingClientPo() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(1).createCell(0).setCellValue("Client PO: PO-1");
        SalesOrderUploadService upload = new SalesOrderUploadService();
        SalesRepo salesOrders = mock(SalesRepo.class);
        ReflectionTestUtils.setField(upload, "salesOrderRepository", salesOrders);
        when(salesOrders.existsByClientPoNumberIgnoreCase("PO-1")).thenReturn(true);

        try {
            upload.uploadExcelFile(excel(workbook, "so.xlsx"));
            fail("Expected the duplicate upload to be rejected");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("already uploaded"));
        }
    }
}
