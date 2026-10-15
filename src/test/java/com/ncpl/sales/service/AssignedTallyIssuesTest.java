package com.ncpl.sales.service;

import com.ncpl.sales.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.aopalliance.intercept.MethodInvocation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AssignedTallyIssuesTest {
    @Test void createAndAlterAlwaysSelectConfiguredCompanyAndStableId() {
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
        ReflectionTestUtils.setField(service,"companyName","Neptune & Company A");
        TallyPurchaseOrderService.PreparedOrder order=order();
        String create=service.buildImportRequest(order);
        TallyPurchaseOrderService.ExistingVoucher existing=new TallyPurchaseOrderService.ExistingVoucher("","","guid-a","123","1","Imported from ERP",LocalDate.of(2026,9,30));
        String alter=service.buildImportRequest(order,existing);
        for(String xml:Arrays.asList(create,alter))assertThat(xml).contains("<SVCURRENTCOMPANY>Neptune &amp; Company A</SVCURRENTCOMPANY>");
        assertThat(alter).contains("TAGNAME=\"MASTER ID\" TAGVALUE=\"123\"").contains("DATE=\"30-Sep-2026\"");
    }
    @Test void fractionalQuantitiesAreNotRoundedLikeMoney() {
        assertThat(TallyPurchaseOrderService.quantityDecimal(1.234f)).isEqualByComparingTo("1.234");
        assertThat(TallyPurchaseOrderService.quantityDecimal(0.004f)).isEqualByComparingTo("0.004");
        assertThat(new TallyPurchaseOrderService(null).buildImportRequest(order())).contains("<BILLEDQTY>1.234 Mtr</BILLEDQTY>");
    }
    @Test void mismatchedUnitsRequireAnApprovedFactorAndPreserveLineValue() throws Exception {
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
        Object snapshot=snapshot();
        assertThatThrownBy(()->convert(service,snapshot,"Box","Nos")).hasStackTraceContaining("no implicit conversion");
        ReflectionTestUtils.setField(service,"unitConversions","Box=Nos*10");
        BigDecimal[] converted=convert(service,snapshot,"Box","Nos");
        assertThat(converted[0]).isEqualByComparingTo("20");assertThat(converted[1]).isEqualByComparingTo("10");
        assertThat(converted[0].multiply(converted[1])).isEqualByComparingTo("200");
        ReflectionTestUtils.setField(service,"unitConversions","Box=Nos*0");
        assertThatThrownBy(()->convert(service,snapshot,"Box","Nos")).hasStackTraceContaining("positive");
        assertThat(convert(service,snapshot,"Meters","Mtr")[0]).isEqualByComparingTo("2");
        assertThatThrownBy(()->convert(service,snapshot,"m²","m")).hasStackTraceContaining("no implicit conversion");
    }
    @Test void duplicatesAreFlaggedWithoutOverwritingVoucherIdentity() {
        Map<String,TallyPurchaseOrderService.ExistingVoucher> known=new HashMap<>();Set<String> ambiguous=new HashSet<>();
        TallyPurchaseOrderService.ExistingVoucher first=new TallyPurchaseOrderService.ExistingVoucher("","","guid-a","123","1","Imported from ERP");
        TallyPurchaseOrderService.recordExistingVoucher(known,ambiguous,"PO-1",first);
        TallyPurchaseOrderService.recordExistingVoucher(known,ambiguous," po-1 ",new TallyPurchaseOrderService.ExistingVoucher("","","guid-b","124","1","Imported from ERP"));
        assertThat(ambiguous).containsExactly("PO-1");assertThat(known.get("PO-1")).isSameAs(first);
    }
    @Test void archivedOrdersAreSkippedWithoutAnyTallyConnection() throws Exception {
        PurchaseOrder archived=new PurchaseOrder();archived.setPoNumber("ARCHIVED");archived.setArchive(true);
        TallyPurchaseOrderService.ExportResult result=new TallyPurchaseOrderService(null).exportAllPending(Collections.singletonList(archived));
        assertThat(result.getSkipped()).isEqualTo(1);assertThat(result.getImported()).isZero();
    }
    @Test void inventoryMustMatchEvenWhenFinancialTotalsMatch() throws Exception {
        TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);String saved=savedVoucher();
        service.verifySavedOrder(order(),xml(saved));
        for(String wrong:Arrays.asList(saved.replace("Cable","Different item"),saved.replace("1.234 Mtr","1.230 Mtr"),saved.replace("10/Mtr","20/Mtr"),saved.replace("01-Oct-26","02-Oct-26"),saved.replace("-12.34","-12.30")))
            assertThatThrownBy(()->service.verifySavedOrder(order(),xml(wrong))).isInstanceOf(IllegalStateException.class).hasMessageContaining("inventory");
    }
    @Test void exportPermissionsFollowConfiguredEditAuthorityRatherThanRoleNames() throws Exception {
        Class<?> type=com.ncpl.sales.controller.SalesController.class;
        for(Method method:type.getDeclaredMethods()) {
            if(!Arrays.asList("sendPurchaseOrdersToTally","importPurchaseOrdersToTally").contains(method.getName()))continue;
            PreAuthorize rule=method.getAnnotation(PreAuthorize.class);assertThat(rule).isNotNull();
            DefaultMethodSecurityExpressionHandler handler=new DefaultMethodSecurityExpressionHandler();
            MethodInvocation invocation=mock(MethodInvocation.class);when(invocation.getMethod()).thenReturn(method);when(invocation.getThis()).thenReturn(new com.ncpl.sales.controller.SalesController());when(invocation.getArguments()).thenReturn(new Object[method.getParameterCount()]);
            for(String authority:Arrays.asList("SALES","PURCHASE","ADMIN","PURCHASE_VIEW","PURCHASE_EDIT")) {
                UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken("user","",Collections.singletonList(new SimpleGrantedAuthority(authority)));
                Boolean allowed=handler.getExpressionParser().parseExpression(rule.value()).getValue(handler.createEvaluationContext(auth,invocation),Boolean.class);
                assertThat(allowed).as(method.getName()+" authority="+authority).isEqualTo("PURCHASE_EDIT".equals(authority));
            }
        }
    }
    @Test void reportPageDoesNotContactTally() throws Exception {
        com.ncpl.sales.controller.itemMasterController controller=new com.ncpl.sales.controller.itemMasterController();
        PartyService parties=mock(PartyService.class);TallyPurchaseOrderService tally=mock(TallyPurchaseOrderService.class);
        when(parties.getPartyListByTypeCustomer()).thenReturn(Collections.emptyList());when(parties.getPartyListbyTypeSupplier()).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(controller,"partyService",parties);ReflectionTestUtils.setField(controller,"tallyPurchaseOrderService",tally);
        assertThat(controller.deliveryChallanPage(new org.springframework.ui.ExtendedModelMap(),new org.springframework.mock.web.MockHttpServletRequest())).isEqualTo("salesReport");
        verifyZeroInteractions(tally);
    }
    static TallyPurchaseOrderService.PreparedOrder order() {
        TallyPurchaseOrderService.PreparedItem item=new TallyPurchaseOrderService.PreparedItem("Cable","Mtr",new BigDecimal("1.234"),BigDecimal.TEN,new BigDecimal("12.34"),LocalDate.of(2026,10,1),"State Purchase@0%","",0);
        return new TallyPurchaseOrderService.PreparedOrder("PO-1",LocalDate.of(2026,10,1),"Vendor",new BigDecimal("12.34"),Collections.singletonList(item),Collections.emptyMap());
    }
    static String savedVoucher() {
        return "<VOUCHER><VOUCHERNUMBER>PO-1</VOUCHERNUMBER><DATE>20261001</DATE><ALTERID>1</ALTERID><ALLLEDGERENTRIES.LIST><LEDGERNAME>Vendor</LEDGERNAME><ISPARTYLEDGER>Yes</ISPARTYLEDGER><AMOUNT>12.34</AMOUNT></ALLLEDGERENTRIES.LIST><ALLINVENTORYENTRIES.LIST><STOCKITEMNAME>Cable</STOCKITEMNAME><BILLEDQTY>1.234 Mtr</BILLEDQTY><ACTUALQTY>1.234 Mtr</ACTUALQTY><RATE>10/Mtr</RATE><AMOUNT>-12.34</AMOUNT><BATCHALLOCATIONS.LIST><BILLEDQTY>1.234 Mtr</BILLEDQTY><ORDERDUEDATE>01-Oct-26</ORDERDUEDATE></BATCHALLOCATIONS.LIST></ALLINVENTORYENTRIES.LIST></VOUCHER>";
    }
    static org.w3c.dom.Element xml(String s) throws Exception {return javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(s))).getDocumentElement();}
    private Object snapshot() throws Exception {
        Class<?> type=Class.forName(TallyPurchaseOrderService.class.getName()+"$MasterSnapshot");Constructor<?> c=type.getDeclaredConstructor(Map.class,Set.class,List.class,Map.class,Map.class,Map.class);c.setAccessible(true);
        return c.newInstance(new HashMap<>(),new HashSet<>(),new ArrayList<>(),new HashMap<>(),new HashMap<>(),new HashMap<>());
    }
    private BigDecimal[] convert(TallyPurchaseOrderService service,Object snapshot,String from,String to) throws Exception {
        Method m=TallyPurchaseOrderService.class.getDeclaredMethod("convertUnits",String.class,String.class,BigDecimal.class,BigDecimal.class,snapshot.getClass());m.setAccessible(true);
        return (BigDecimal[])m.invoke(service,from,to,new BigDecimal("2"),new BigDecimal("100"),snapshot);
    }
}
