package com.ncpl.sales.cashflow.service;
import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectedCashflowServiceTest {
    private final ProjectedCashflowService service=new ProjectedCashflowService();
    private final LocalDate today=LocalDate.of(2026,10,9);
    private InvoiceEntity bill(String number,String amount,int due,boolean receivable,Integer expected){
        InvoiceEntity b=new InvoiceEntity();b.setInvoiceNumber(number);b.setCustomerName("Party");b.setInvoiceValue(new BigDecimal(amount));b.setDueDate(today.plusDays(due));
        b.setInvoiceType(receivable?InvoiceEntity.InvoiceType.INFLOW:InvoiceEntity.InvoiceType.OUTFLOW);if(expected!=null)b.setExpectedCashDate(today.plusDays(expected));return b;
    }
    @SuppressWarnings("unchecked") private List<Map<String,Object>> periods(Map<String,Object> data){return (List<Map<String,Object>>)data.get("periods");}
    @Test void carriesOpeningAcrossEmptyDaysAndSchedulesBacklogOnce(){
        List<InvoiceEntity> bills=Arrays.asList(bill("receipt","500",0,true,null),bill("payment","200",2,false,null),bill("old-unscheduled","999",-5,true,null),bill("old-planned","50",-3,false,1));
        Map<String,Object> data=service.project(bills,today,today.plusDays(2),"daily",new BigDecimal("1000"),today,today);
        List<Map<String,Object>> p=periods(data);
        assertEquals(new BigDecimal("1500.00"),p.get(0).get("closingBalance"));
        assertEquals(p.get(0).get("closingBalance"),p.get(1).get("openingBalance"));
        assertEquals(new BigDecimal("1450.00"),p.get(1).get("closingBalance"));
        assertEquals(new BigDecimal("1250.00"),data.get("closingBalance"));assertEquals(1,data.get("unscheduledCount"));
    }
    @Test void dailyDrilldownCarriesEarlierSelectedFlowsAndMatchesWeeklyClose(){
        List<InvoiceEntity> bills=Arrays.asList(bill("receipt","0.10",0,true,null),bill("receipt2","0.20",1,true,null));
        Map<String,Object> weekly=service.project(bills,today,today.plusDays(6),"weekly",new BigDecimal("100"),today,today);
        Map<String,Object> daily=service.project(bills,today.plusDays(1),today.plusDays(6),"daily",new BigDecimal("100"),today,today);
        assertEquals(new BigDecimal("100.10"),daily.get("openingBalance"));assertEquals(weekly.get("closingBalance"),daily.get("closingBalance"));
    }
    @Test void unknownOpeningDoesNotPretendCashStartsAtZero(){
        Map<String,Object> data=service.project(Collections.singletonList(bill("receipt","20",0,true,null)),today,today,"daily",null,null,today);
        assertNull(data.get("openingBalance"));assertNull(data.get("closingBalance"));assertEquals(new BigDecimal("20.00"),data.get("finalCumulative"));
    }
    @Test void expiredExpectedDatesRequireReschedulingAndDuplicateBillsAreNotCountedTwice(){
        InvoiceEntity expired=bill("expired","100",3,true,-1), receipt=bill("receipt","20",0,true,null);
        Map<String,Object> data=service.project(Arrays.asList(expired,receipt,receipt),today,today.plusDays(4),"daily",BigDecimal.ZERO,today,today);
        assertEquals(1,data.get("unscheduledCount"));assertEquals(new BigDecimal("20.00"),data.get("closingBalance"));assertEquals(5,periods(data).size());
    }
    @Test void rejectsMissingOrLaterOpeningDate(){
        assertThrows(IllegalArgumentException.class,()->service.project(Collections.emptyList(),today,today,"daily",BigDecimal.TEN,null,today));
        assertThrows(IllegalArgumentException.class,()->service.project(Collections.emptyList(),today,today,"daily",BigDecimal.TEN,today.plusDays(1),today));
    }
    @Test void retentionRequiresAnExplicitReleaseDate(){
        InvoiceEntity retention=bill("R1","100",1,true,null);retention.setCustomerName("Retention-JSS");
        assertNull(service.effectiveDate(retention,today));retention.setExpectedCashDate(today.plusDays(3));
        assertEquals(today.plusDays(3),service.effectiveDate(retention,today));
    }
}
