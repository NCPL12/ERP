package com.ncpl.sales.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TallyPurchaseOrderServiceTest {
    @Test void rejectsZeroValueOrdersBeforeTallyImportButAllowsPricedOrders() {
        assertThatThrownBy(() -> TallyPurchaseOrderService.validateFinancialValue("ZERO-PO", BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("zero financial value").hasMessageContaining("nothing was sent to Tally");
        TallyPurchaseOrderService.validateFinancialValue("PRICED-PO", new BigDecimal("0.01"));
    }
    @Test void decodesUnicodeTallyResponsesAndUtf8Errors() {
        String xml = "<NAME>Camera 1/1.8\u2033 CMOS \u00d7</NAME>";
        assertThat(TallyPurchaseOrderService.decodeTallyResponse(xml.getBytes(java.nio.charset.StandardCharsets.UTF_16LE))).isEqualTo(xml);
        assertThat(TallyPurchaseOrderService.decodeTallyResponse(("\uFEFF" + xml).getBytes(java.nio.charset.StandardCharsets.UTF_16BE))).isEqualTo(xml);
        assertThat(TallyPurchaseOrderService.decodeTallyResponse("<ERROR>Unavailable</ERROR>".getBytes(java.nio.charset.StandardCharsets.UTF_8))).isEqualTo("<ERROR>Unavailable</ERROR>");
    }
    @Test void preservesRawControlWhitespaceFromTallyAttributes() throws Exception {
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
        java.lang.reflect.Method parse=TallyPurchaseOrderService.class.getDeclaredMethod("parseXml",String.class); parse.setAccessible(true);
        org.w3c.dom.Document doc=(org.w3c.dom.Document)parse.invoke(service,"<STOCKITEM NAME=\"Cable\r\n\"><NAME>Cable\r\n</NAME></STOCKITEM>");
        assertThat(TallyPurchaseOrderService.masterName(doc.getDocumentElement())).isEqualTo("Cable\r\n");
    }
    @Test void preservesHiddenMasterCharactersThroughXml() throws Exception {
        javax.xml.parsers.DocumentBuilder parser=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document master=parser.parse(new org.xml.sax.InputSource(new java.io.StringReader("<STOCKITEM NAME=\"DVR \"><NAME>DVR\t</NAME></STOCKITEM>")));
        assertThat(TallyPurchaseOrderService.masterName(master.getDocumentElement())).isEqualTo("DVR\t");
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
        java.lang.reflect.Method escape=TallyPurchaseOrderService.class.getDeclaredMethod("escapeXml",String.class);escape.setAccessible(true);
        String name="Cable\r\n";
        String xml="<NAME>"+escape.invoke(service,name)+"</NAME>";
        org.w3c.dom.Document decoded=parser.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        assertThat(decoded.getDocumentElement().getTextContent()).isEqualTo(name);
    }
    @Test void preservesExactWhitespaceInTallyMasterNames() throws Exception {
        javax.xml.parsers.DocumentBuilder parser=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc=parser.parse(new org.xml.sax.InputSource(new java.io.StringReader("<STOCKITEM NAME=\" 8 CHANNEL DVR (2MP) \"><NAME>Other alias</NAME></STOCKITEM>")));
        assertThat(TallyPurchaseOrderService.masterName(doc.getDocumentElement())).isEqualTo(" 8 CHANNEL DVR (2MP) ");
    }
    @Test
    void progressCountsAllBatchesAndKeepsSnapshotsStable() throws Exception {
        TallyPurchaseOrderService service = new TallyPurchaseOrderService(null);
        java.lang.reflect.Field previous = TallyPurchaseOrderService.class.getDeclaredField("completedProgress");
        previous.setAccessible(true);
        previous.set(service, Arrays.asList(new TallyPurchaseOrderService.ExportLine("A", "UPDATED", "")));
        java.lang.reflect.Field offset = TallyPurchaseOrderService.class.getDeclaredField("progressOffset");
        offset.setAccessible(true); offset.setInt(service, 1);
        java.lang.reflect.Method publish = TallyPurchaseOrderService.class.getDeclaredMethod("publishProgress", String.class, int.class, java.util.List.class);
        publish.setAccessible(true);
        publish.invoke(service, "Sending", 4, Arrays.asList(
            new TallyPurchaseOrderService.ExportLine("B", "IMPORTED", ""),
            new TallyPurchaseOrderService.ExportLine("C", "UNCHANGED", "")));
        Map<String,Object> snapshot = service.getProgress();
        assertThat(snapshot).containsEntry("processed",3).containsEntry("total",4)
            .containsEntry("updated",1).containsEntry("imported",1).containsEntry("skipped",1).containsEntry("failed",0);
        publish.invoke(service, "Verified", 4, Arrays.asList(
            new TallyPurchaseOrderService.ExportLine("B", "FAILED", "verification failed"),
            new TallyPurchaseOrderService.ExportLine("C", "UNCHANGED", ""),
            new TallyPurchaseOrderService.ExportLine("D", "SKIPPED", "protected")));
        assertThat(service.getProgress()).containsEntry("processed",4).containsEntry("failed",1).containsEntry("imported",0);
        assertThat(snapshot).containsEntry("processed",3).containsEntry("imported",1);
    }
    @Test
    void targetsStoredTallyDateWhenErpDateChanges() {
        TallyPurchaseOrderService service = new TallyPurchaseOrderService(null);
        TallyPurchaseOrderService.PreparedOrder order = new TallyPurchaseOrderService.PreparedOrder(
                "PO-CHANGED", LocalDate.of(2026, 10, 7), "VENDOR", BigDecimal.ZERO,
                java.util.Collections.emptyList(), java.util.Collections.emptyMap());
        TallyPurchaseOrderService.ExistingVoucher existing = new TallyPurchaseOrderService.ExistingVoucher(
                "", "", "guid", "123", "", "Imported from ERP", LocalDate.of(2026, 10, 6));
        assertThat(service.buildImportRequest(order, existing))
                .contains("DATE=\"06-Oct-2026\"")
                .contains("<DATE>20261007</DATE>")
                .contains("ACTION=\"Alter\"");
    }

    @Test
    void rejectsOverlappingExportWithoutContactingTally() throws Exception {
        TallyPurchaseOrderService service = new TallyPurchaseOrderService(null);
        java.lang.reflect.Field field = TallyPurchaseOrderService.class.getDeclaredField("exportRunning");
        field.setAccessible(true);
        java.util.concurrent.atomic.AtomicBoolean running = (java.util.concurrent.atomic.AtomicBoolean) field.get(service);
        running.set(true);
        assertThatThrownBy(() -> service.exportAllPending(java.util.Collections.emptyList()))
                .hasMessageContaining("already running");
        assertThat(running.get()).isTrue();
        running.set(false);
        service.exportAllPending(java.util.Collections.emptyList());
        assertThat(running.get()).isFalse();
    }

    @Test
    void batchVerificationSelectsExactVoucherAndRejectsMissingOrder() throws Exception {
        TallyPurchaseOrderService service = new TallyPurchaseOrderService(null);
        TallyPurchaseOrderService.PreparedOrder order = new TallyPurchaseOrderService.PreparedOrder(
                "PO-2", LocalDate.of(2026, 10, 1), "VENDOR", new BigDecimal("100"),
                java.util.Collections.emptyList(), java.util.Collections.emptyMap());
        String xml = "<ENVELOPE><VOUCHER><VOUCHERNUMBER>PO-1</VOUCHERNUMBER></VOUCHER>"
                + "<VOUCHER><VOUCHERNUMBER>PO-2</VOUCHERNUMBER><ALLLEDGERENTRIES.LIST>"
                + "<LEDGERNAME>VENDOR</LEDGERNAME><ISPARTYLEDGER>Yes</ISPARTYLEDGER>"
                + "<AMOUNT>100</AMOUNT></ALLLEDGERENTRIES.LIST></VOUCHER></ENVELOPE>";
        javax.xml.parsers.DocumentBuilder parser = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        service.verifySavedOrder(order, parser.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml))));
        org.w3c.dom.Document missing = parser.parse(new org.xml.sax.InputSource(new java.io.StringReader(
                xml.replace("<VOUCHERNUMBER>PO-2", "<VOUCHERNUMBER>PO-3"))));
        assertThatThrownBy(() -> service.verifySavedOrder(order, missing)).hasMessageContaining("could not be verified");
    }


    @Test
    void rejectsAcceptedVoucherWhenTaxIsFirstOrTotalIsWrong() throws Exception {
        TallyPurchaseOrderService service = new TallyPurchaseOrderService(null);
        Map<String, BigDecimal> taxes = new LinkedHashMap<>();
        taxes.put("CGST", new BigDecimal("9"));
        TallyPurchaseOrderService.PreparedOrder order = new TallyPurchaseOrderService.PreparedOrder(
                "PO-1", LocalDate.of(2026, 10, 1), "VENDOR", new BigDecimal("109"),
                java.util.Collections.emptyList(), taxes);
        String good = "<VOUCHER><ALLLEDGERENTRIES.LIST><LEDGERNAME>VENDOR</LEDGERNAME>"
                + "<ISPARTYLEDGER>Yes</ISPARTYLEDGER><AMOUNT>109</AMOUNT></ALLLEDGERENTRIES.LIST>"
                + "<ALLLEDGERENTRIES.LIST><LEDGERNAME>CGST</LEDGERNAME><ISPARTYLEDGER>No</ISPARTYLEDGER>"
                + "<AMOUNT>-9</AMOUNT></ALLLEDGERENTRIES.LIST></VOUCHER>";
        javax.xml.parsers.DocumentBuilder parser = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        service.verifySavedOrder(order, parser.parse(new org.xml.sax.InputSource(new java.io.StringReader(good))).getDocumentElement());
        org.w3c.dom.Element wrongParty = parser.parse(new org.xml.sax.InputSource(
                new java.io.StringReader(good.replace("<LEDGERNAME>VENDOR", "<LEDGERNAME>CGST")))).getDocumentElement();
        assertThatThrownBy(() -> service.verifySavedOrder(order, wrongParty)).hasMessageContaining("vendor");
        org.w3c.dom.Element wrongTotal = parser.parse(new org.xml.sax.InputSource(
                new java.io.StringReader(good.replace("<AMOUNT>109", "<AMOUNT>218")))).getDocumentElement();
        assertThatThrownBy(() -> service.verifySavedOrder(order, wrongTotal)).hasMessageContaining("total");
        org.w3c.dom.Element missingTax = parser.parse(new org.xml.sax.InputSource(
                new java.io.StringReader(good.replace("<AMOUNT>-9", "<AMOUNT>0")))).getDocumentElement();
        assertThatThrownBy(() -> service.verifySavedOrder(order, missingTax)).hasMessageContaining("GST");
    }
    @Test
    void buildsPurchaseOrderWithAutomaticPurchaseLedgerHsnAndGstBreakdown() {
        TallyPurchaseOrderService service = new TallyPurchaseOrderService(null);
        TallyPurchaseOrderService.PreparedItem first = new TallyPurchaseOrderService.PreparedItem(
                "ITEM-18", "Nos", new BigDecimal("2"), new BigDecimal("100.00"),
                new BigDecimal("200.00"), LocalDate.of(2026, 10, 15),
                "State Purchase @ 18%", "85371000", 18);
        TallyPurchaseOrderService.PreparedItem second = new TallyPurchaseOrderService.PreparedItem(
                "ITEM-5", "Nos", BigDecimal.ONE, new BigDecimal("50.00"),
                new BigDecimal("50.00"), LocalDate.of(2026, 10, 16),
                "State Purchase 5%", "85365090", 5);
        Map<String, BigDecimal> taxes = new LinkedHashMap<>();
        taxes.put("Input Tax- CGST @ 9%", new BigDecimal("18.00"));
        taxes.put("Input Tax- SGST @ 9%", new BigDecimal("18.00"));
        taxes.put("Input Tax-CGST @ 2.5%", new BigDecimal("1.25"));
        taxes.put("Input Tax-SGST @ 2.5%", new BigDecimal("1.25"));
        TallyPurchaseOrderService.PreparedOrder order = new TallyPurchaseOrderService.PreparedOrder(
                "ERP-PO-1", LocalDate.of(2026, 10, 1), "TEST VENDOR", new BigDecimal("288.50"),
                Arrays.asList(first, second), taxes);

        String xml = service.buildImportRequest(order);

        assertThat(xml).contains("<VOUCHERNUMBER>ERP-PO-1</VOUCHERNUMBER>")
                .contains("<GSTOVRDNHSNCODE>85371000</GSTOVRDNHSNCODE>")
                .contains("<LEDGERNAME>State Purchase @ 18%</LEDGERNAME>")
                .contains("<LEDGERNAME>State Purchase 5%</LEDGERNAME>")
                .contains("<LEDGERNAME>Input Tax- CGST @ 9%</LEDGERNAME>")
                .contains("<AMOUNT>-18.00</AMOUNT>")
                .contains("<AMOUNT>288.50</AMOUNT>");

        TallyPurchaseOrderService.ExistingVoucher existing = new TallyPurchaseOrderService.ExistingVoucher(
                "remote-1", "key-1", "guid-1", "100", "3", "Imported from ERP PO List by Date");
        assertThat(xml).contains("<ISINVOICE>No</ISINVOICE>")
                .contains("<PARTYNAME>TEST VENDOR</PARTYNAME>")
                .contains("<PARTYMAILINGNAME>TEST VENDOR</PARTYMAILINGNAME>");
        assertThat(xml.indexOf("<LEDGERNAME>TEST VENDOR</LEDGERNAME>"))
                .isLessThan(xml.indexOf("<ALLINVENTORYENTRIES.LIST>"));
        assertThat(xml).contains("<ISPARTYLEDGER>No</ISPARTYLEDGER>");
        String alterXml = service.buildImportRequest(order, existing);
        assertThat(alterXml).contains("ACTION=\"Alter\"")
                .contains("<TALLYREQUEST>Import</TALLYREQUEST>")
                .contains("DATE=\"01-Oct-2026\"")
                .contains("TAGNAME=\"Voucher Number\"")
                .contains("TAGVALUE=\"ERP-PO-1\"")
                .doesNotContain("REMOTEID=\"remote-1\"")
                .contains("<GUID>guid-1</GUID>")
                .doesNotContain("<MASTERID>")
                .doesNotContain("<ALTERID>");
    }
}
