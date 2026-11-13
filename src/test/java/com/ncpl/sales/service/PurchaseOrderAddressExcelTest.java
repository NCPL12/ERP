package com.ncpl.sales.service;

import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.security.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PurchaseOrderAddressExcelTest {
    @Test void companyAddressesHaveNoPaddingInDownloadedWorkbook() throws Exception {
        checkAddresses(false);
    }

    @Test void alternateDeliveryAddressHasNoPaddingInDownloadedWorkbook() throws Exception {
        checkAddresses(true);
    }

    private void checkAddresses(boolean alternate) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, String> terms = new HashMap<>();
        terms.put("shippingAddressId", alternate ? "party1" : "key1");
        terms.put("billingAddressId", "key1");
        terms.put("gstRegion", "interState");
        terms.put("quoteDate", "2026-11-11");
        terms.put("modeOfPayment", "30 days");
        request.setAttribute("termsAndConditions", terms);
        request.setAttribute("modelMaps", Collections.singletonMap("MODEL", "Nos&MODEL$18"));
        User user = new User();
        user.setName("Aniksha");
        user.setEmailId("aniksha@ncpl.co");
        user.setNumber("8197563637");
        request.setAttribute("user", user);
        Party party = mock(Party.class, RETURNS_DEEP_STUBS);
        when(party.getPartyName()).thenReturn("  Supplier Pvt Ltd  ");
        when(party.getAddr1()).thenReturn(" \tNo. 56/4, Sharada Towers Unit-II  \n  Nandi Durga Road\t ");
        when(party.getAddr2()).thenReturn("  Jayamahal Extension  ");
        when(party.getParty_city().getName()).thenReturn("Bengaluru");
        when(party.getPin()).thenReturn("560046");
        request.setAttribute("party", party);
        if (alternate) request.setAttribute("altshippingParty", party);
        PurchaseOrderCustomProperty properties = new PurchaseOrderCustomProperty();
        String address = "key1$$ Neptune Controls Pvt Ltd $$ 2nd Floor, No. 56/4, Sharada Towers Unit-II, Nandi Durga Road $$ Near Bharathi Airtel Office, Jayamahal Extension, Bengaluru Urban $$Bengaluru$$Karnataka$$India$$560046";
        properties.setBillingAddress(address);
        properties.setShippingAddress(address);
        request.setAttribute("customProperty", properties);
        PurchaseItem item = new PurchaseItem();
        item.setModelNo("MODEL");
        item.setPoDescription("Test item");
        item.setQuantity(1);
        item.setUnitPrice(100);
        item.setAmount(100);
        item.setGstRate(new BigDecimal("18"));
        PurchaseOrder order = new PurchaseOrder();
        order.setPoNumber("PO-TEST");
        order.setCreated(Timestamp.valueOf("2026-11-11 00:00:00"));
        order.setItems(Collections.singletonList(item));
        purchaseOrderExcel export = new purchaseOrderExcel();
        export.logoService = mock(InvoiceExcelLogoService.class);
        export.signService = mock(PurchaseSignatureLogo.class);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            export.buildExcelDocument(Collections.singletonMap("poData",
                    Collections.singletonMap("purchaseData", Optional.of(order))),
                    workbook, request, new MockHttpServletResponse());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            workbook.write(bytes);
            try (XSSFWorkbook downloaded = new XSSFWorkbook(new ByteArrayInputStream(bytes.toByteArray()))) {
                for (int column : new int[] {0, 4, 8}) {
                    Cell cell = downloaded.getSheetAt(0).getRow(5).getCell(column);
                    assertThat(cell.getCellStyle().getAlignment()).isEqualTo(CellStyle.ALIGN_LEFT);
                    assertThat(cell.getCellStyle().getVerticalAlignment()).isEqualTo(CellStyle.VERTICAL_TOP);
                    assertThat(cell.getCellStyle().getWrapText()).isTrue();
                    assertThat(cell.getCellStyle().getIndention()).isZero();
                    for (String line : cell.getStringCellValue().split("\n")) {
                        assertThat(line).isEqualTo(line.trim());
                    }
                    assertThat(cell.getStringCellValue()).contains("No. 56/4, Sharada Towers Unit-II");
                }
                if (!alternate) {
                    assertThat(downloaded.getSheetAt(0).getRow(5).getCell(4).getStringCellValue())
                            .isEqualTo("Deliver To:\nNeptune Controls Pvt Ltd\n"
                                    + "2nd Floor, No. 56/4, Sharada Towers Unit-II, Nandi Durga Road\n"
                                    + "Near Bharathi Airtel Office, Jayamahal Extension, Bengaluru Urban\n"
                                    + "Bengaluru,Karnataka 560046\nContact: Aniksha@:8197563637\nEmail:aniksha@ncpl.co");
                }
                assertThat(downloaded.getSheetAt(0).getRow(5).getCell(8).getStringCellValue())
                        .isEqualTo("Billing & Invoice:\nNeptune Controls Pvt Ltd\n"
                                + "2nd Floor, No. 56/4, Sharada Towers Unit-II, Nandi Durga Road\n"
                                + "Near Bharathi Airtel Office, Jayamahal Extension, Bengaluru Urban\n"
                                + "Bengaluru,Karnataka 560046\nPAN NO: " + export.billingPan + "\nGSTIN : " + export.billingGst);
            }
        } finally {
            export.context.close();
        }
    }
}
