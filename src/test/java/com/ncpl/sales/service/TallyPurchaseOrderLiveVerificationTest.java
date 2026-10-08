package com.ncpl.sales.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

/** Explicit opt-in only: uses an isolated temporary PO, then removes only that PO. */
@EnabledIfSystemProperty(named = "tally.po.live.verify", matches = "true")
class TallyPurchaseOrderLiveVerificationTest {
    @Test
    void createsUpdatesReadsBackAndRemovesIsolatedPurchaseOrder() throws Exception {
        TallyPurchaseOrderService service = new TallyPurchaseOrderService(null);
        Field server = TallyPurchaseOrderService.class.getDeclaredField("serverUrl");
        server.setAccessible(true); server.set(service, "http://127.0.0.1:9000");
        Field company = TallyPurchaseOrderService.class.getDeclaredField("companyName");
        company.setAccessible(true); company.set(service, "Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)");
        Method post = TallyPurchaseOrderService.class.getDeclaredMethod("post", String.class);
        post.setAccessible(true);
        Method parse = TallyPurchaseOrderService.class.getDeclaredMethod("parseXml", String.class);
        parse.setAccessible(true);
        String number = "ERP-PO-VERIFY-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDate date = LocalDate.of(2026, 10, 6);
        Map<String, BigDecimal> taxes = new LinkedHashMap<>();
        taxes.put("Input Tax- CGST @ 9%", new BigDecimal("3.96"));
        taxes.put("Input Tax- SGST @ 9%", new BigDecimal("3.96"));
        TallyPurchaseOrderService.PreparedItem item = new TallyPurchaseOrderService.PreparedItem(
                "Category 6 U/UTP Cable, low smoke zero halogen, gray jacket, 4 pair count, 1000 ft (305 m) length Co",
                "Box", BigDecimal.ONE, new BigDecimal("44.00"), new BigDecimal("44.00"), date,
                "State Purchase @ 18%", "", 18);
        TallyPurchaseOrderService.PreparedOrder order = new TallyPurchaseOrderService.PreparedOrder(
                number, date, "Kothari Electronic House", new BigDecimal("51.92"),
                Collections.singletonList(item), taxes);
        String query = "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>VerifyPO</ID></HEADER><BODY><DESC><STATICVARIABLES><SVCURRENTCOMPANY>Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)</SVCURRENTCOMPANY></STATICVARIABLES><TDL><TDLMESSAGE><COLLECTION NAME=\"VerifyPO\"><TYPE>Voucher</TYPE><FILTER>VerifyPOFilter</FILTER><FETCH>VoucherNumber,Date,GUID,MasterID,PartyName,AllLedgerEntries.*,AllInventoryEntries.*</FETCH></COLLECTION><SYSTEM TYPE=\"Formulae\" NAME=\"VerifyPOFilter\">$VoucherTypeName = &quot;Purchase Order&quot; AND $VoucherNumber = &quot;" + number + "&quot;</SYSTEM></TDLMESSAGE></TDL></DESC></BODY></ENVELOPE>";
        Element saved = null;
        try {
            Document created = (Document) parse.invoke(service, post.invoke(service, service.buildImportRequest(order)));
            assertThat(created.getElementsByTagName("CREATED").item(0).getTextContent()).isEqualTo("1");
            Document snapshot = (Document) parse.invoke(service, post.invoke(service, query));
            assertThat(snapshot.getElementsByTagName("ALLLEDGERENTRIES.LIST").getLength()).isEqualTo(4);

            // XML whitespace is not an object: locate the exact voucher by its number.
            for (int i = 0; i < snapshot.getElementsByTagName("VOUCHER").getLength(); i++) {
                Element candidate = (Element) snapshot.getElementsByTagName("VOUCHER").item(i);
                if (candidate.getElementsByTagName("VOUCHERNUMBER").getLength() > 0) saved = candidate;
            }
            service.verifySavedOrder(order, saved);
            assertThat(saved.getElementsByTagName("PARTYNAME").item(0).getTextContent()).isEqualTo("Kothari Electronic House");
            String guid = saved.getElementsByTagName("GUID").item(0).getTextContent();
            String id = saved.getElementsByTagName("MASTERID").item(0).getTextContent();
            TallyPurchaseOrderService.ExistingVoucher existing = new TallyPurchaseOrderService.ExistingVoucher(
                    "", "", guid, id, "", "Imported from ERP verification", date);
            order = new TallyPurchaseOrderService.PreparedOrder(number, date.plusDays(1), "Kothari Electronic House",
                    new BigDecimal("51.92"), Collections.singletonList(item), taxes);
            Document changed = (Document) parse.invoke(service, post.invoke(service, service.buildImportRequest(order, existing)));
            assertThat(changed.getElementsByTagName("ALTERED").item(0).getTextContent()).isEqualTo("1");
            Document repeated = (Document) parse.invoke(service, post.invoke(service, query));
            int count = 0;
            for (int i = 0; i < repeated.getElementsByTagName("VOUCHER").getLength(); i++) {
                Element candidate = (Element) repeated.getElementsByTagName("VOUCHER").item(i);
                if (candidate.getElementsByTagName("VOUCHERNUMBER").getLength() > 0) {
                    count++; service.verifySavedOrder(order, candidate);
                    assertThat(candidate.getElementsByTagName("MASTERID").item(0).getTextContent()).isEqualTo(id);
                }
            }
            assertThat(count).isEqualTo(1);
            assertThat(repeated.getElementsByTagName("DATE").item(0).getTextContent()).isEqualTo("20261007");
            System.out.println("LIVE PO VERIFIED: Create + Alter; vendor, both GST entries, total; no duplicate. " + number);
        } finally {
            // Remove only the temporary voucher created by this test, identified by its unique number and date.
            if (saved != null) {
                Document cleanupSnapshot = (Document) parse.invoke(service, post.invoke(service, query));
                String cleanupDate = cleanupSnapshot.getElementsByTagName("DATE").item(0).getTextContent();
                String cleanupIdentifierDate = LocalDate.parse(cleanupDate, java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy", java.util.Locale.ENGLISH));
                String delete = "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Import</TALLYREQUEST><TYPE>Data</TYPE><ID>Vouchers</ID></HEADER><BODY><DESC/><DATA><TALLYMESSAGE><VOUCHER DATE=\"" + cleanupIdentifierDate + "\" TAGNAME=\"Voucher Number\" TAGVALUE=\"" + number + "\" VCHTYPE=\"Purchase Order\" ACTION=\"Delete\"><DATE>" + cleanupDate + "</DATE><VOUCHERTYPENAME>Purchase Order</VOUCHERTYPENAME><VOUCHERNUMBER>" + number + "</VOUCHERNUMBER></VOUCHER></TALLYMESSAGE></DATA></BODY></ENVELOPE>";
                Document removed = (Document) parse.invoke(service, post.invoke(service, delete));
                assertThat(removed.getElementsByTagName("DELETED").item(0).getTextContent()).isEqualTo("1");
                System.out.println("Temporary verification PO removed.");
            }
        }
    }
}
