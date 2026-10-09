package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectedCashflowService {
    public Map<String,Object> project(List<InvoiceEntity> source, LocalDate from, LocalDate to,
            String granularity, BigDecimal opening, LocalDate openingDate, LocalDate today) {
        if (from == null) from = today;
        if (to == null) to = from.plusDays(89);
        if (from.isAfter(to) || ChronoUnit.DAYS.between(from, to) > 370)
            throw new IllegalArgumentException("Choose a valid forecast range of one year or less.");
        if (!"daily".equals(granularity) && !"weekly".equals(granularity))
            throw new IllegalArgumentException("Granularity must be daily or weekly.");
        if (opening != null && openingDate == null)
            throw new IllegalArgumentException("Enter the date of the opening cash/bank balance.");
        if (opening != null && openingDate.isAfter(from))
            throw new IllegalArgumentException("Opening balance date must be on or before the forecast From date.");
        Map<String,InvoiceEntity> unique = new LinkedHashMap<>();
        for (InvoiceEntity b : source) {
            String key = b.getInvoiceType()+"|"+b.getCustomerName()+"|"+b.getInvoiceNumber()+"|"+b.getInvoiceDate()+"|"+b.getDueDate()+"|"+money(b.getInvoiceValue());
            unique.putIfAbsent(key,b);
        }
        List<InvoiceEntity> bills = new ArrayList<>(unique.values());
        List<InvoiceEntity> backlog = bills.stream().filter(b -> b.getDueDate()!=null && b.getDueDate().isBefore(today)).collect(Collectors.toList());
        List<InvoiceEntity> unscheduled = bills.stream().filter(b -> effectiveDate(b,today)==null).collect(Collectors.toList());
        BigDecimal carried = BigDecimal.ZERO;
        if (opening != null) for (InvoiceEntity b : bills) {
            LocalDate date = effectiveDate(b,today);
            if (date!=null && !date.isBefore(openingDate) && date.isBefore(from)) carried=carried.add(signed(b));
        }
        BigDecimal rangeOpening=opening==null?null:money(opening).add(carried), closing=rangeOpening;
        BigDecimal receipts=BigDecimal.ZERO, payments=BigDecimal.ZERO, net=BigDecimal.ZERO;
        List<Map<String,Object>> periods=new ArrayList<>();
        for (LocalDate start=from; !start.isAfter(to);) {
            LocalDate end="daily".equals(granularity)?start:start.plusDays(7-start.getDayOfWeek().getValue());
            if(end.isAfter(to))end=to;
            final LocalDate a=start,z=end;
            List<InvoiceEntity> items=bills.stream().filter(b->{LocalDate d=effectiveDate(b,today);return d!=null&&!d.isBefore(a)&&!d.isAfter(z);}).collect(Collectors.toList());
            BigDecimal in=total(items,InvoiceEntity.InvoiceType.INFLOW), out=total(items,InvoiceEntity.InvoiceType.OUTFLOW);
            BigDecimal change=in.subtract(out), periodOpening=closing;
            net=net.add(change);receipts=receipts.add(in);payments=payments.add(out);
            if(closing!=null)closing=closing.add(change);
            Map<String,Object> p=new LinkedHashMap<>();
            p.put("weekStart",start);p.put("weekEnd",end);p.put("receivables",in);p.put("payables",out);
            p.put("net",change);p.put("cumulative",net);p.put("openingBalance",periodOpening);p.put("closingBalance",closing);
            p.put("billCount",items.size());p.put("bills",rows(items,today));
            p.put("receivableParties",parties(items,InvoiceEntity.InvoiceType.INFLOW));p.put("payableParties",parties(items,InvoiceEntity.InvoiceType.OUTFLOW));
            periods.add(p);start=end.plusDays(1);
        }
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("from",from);result.put("to",to);result.put("granularity",granularity);result.put("periods",periods);
        result.put("openingBalance",rangeOpening);result.put("balanceAsOf",openingDate);result.put("enteredOpeningBalance",opening);
        result.put("carriedNet",carried);result.put("closingBalance",closing);result.put("finalCumulative",net);
        result.put("totalReceivables",receipts);result.put("totalPayables",payments);result.put("backlogBills",rows(backlog,today));
        result.put("unscheduledCount",unscheduled.size());result.put("unscheduledBills",rows(unscheduled,today));
        result.put("backlogReceivables",total(backlog,InvoiceEntity.InvoiceType.INFLOW));result.put("backlogPayables",total(backlog,InvoiceEntity.InvoiceType.OUTFLOW));
        return result;
    }
    public LocalDate effectiveDate(InvoiceEntity b,LocalDate today) {
        if (RetentionLedgerClassifier.isRetention(b.getCustomerName()) && b.getExpectedCashDate()==null) return null;
        // An explicit but expired promise must be rescheduled; do not fall back to due date.
        LocalDate date=b.getExpectedCashDate()!=null?b.getExpectedCashDate():b.getDueDate();
        return date==null||date.isBefore(today)?null:date;
    }
    private BigDecimal money(BigDecimal value){return (value==null?BigDecimal.ZERO:value).setScale(2,RoundingMode.HALF_UP);}
    private BigDecimal signed(InvoiceEntity b){return b.getInvoiceType()==InvoiceEntity.InvoiceType.INFLOW?money(b.getInvoiceValue()):money(b.getInvoiceValue()).negate();}
    private BigDecimal total(List<InvoiceEntity> bills,InvoiceEntity.InvoiceType type){return bills.stream().filter(b->b.getInvoiceType()==type).map(b->money(b.getInvoiceValue())).reduce(BigDecimal.ZERO,BigDecimal::add);}
    private List<Map<String,Object>> parties(List<InvoiceEntity> bills,InvoiceEntity.InvoiceType type){
        Map<String,List<InvoiceEntity>> grouped=bills.stream().filter(b->b.getInvoiceType()==type).collect(Collectors.groupingBy(b->b.getCustomerName()==null?"Unknown":b.getCustomerName()));
        return grouped.entrySet().stream().sorted((a,b)->total(b.getValue(),type).compareTo(total(a.getValue(),type))).map(e->{Map<String,Object> p=new LinkedHashMap<>();p.put("name",e.getKey());p.put("amount",total(e.getValue(),type));p.put("billCount",e.getValue().size());return p;}).collect(Collectors.toList());
    }
    public List<Map<String,Object>> rows(List<InvoiceEntity> bills,LocalDate today){
        return bills.stream().map(b->{Map<String,Object> r=new LinkedHashMap<>();boolean overdue=b.getDueDate()!=null&&b.getDueDate().isBefore(today);
            r.put("id",b.getId());r.put("direction",b.getInvoiceType()==InvoiceEntity.InvoiceType.INFLOW?"RECEIVABLE":"PAYABLE");r.put("partyName",b.getCustomerName());r.put("invoiceNumber",b.getInvoiceNumber());
            r.put("invoiceDate",b.getInvoiceDate());r.put("dueDate",b.getDueDate());r.put("expectedCashDate",b.getExpectedCashDate());r.put("cashDate",effectiveDate(b,today));r.put("cashflowRemarks",b.getCashflowRemarks());r.put("amount",money(b.getInvoiceValue()));
            r.put("dueAmount",overdue?BigDecimal.ZERO:money(b.getInvoiceValue()));r.put("overdueAmount",overdue?money(b.getInvoiceValue()):BigDecimal.ZERO);r.put("ageingDays",b.getInvoiceDate()==null?0:Math.max(0,ChronoUnit.DAYS.between(b.getInvoiceDate(),today)));return r;
        }).collect(Collectors.toList());
    }
}
