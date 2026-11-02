package com.ncpl.sales.cashflow.controller;

import com.ncpl.sales.cashflow.entity.*;
import com.ncpl.sales.cashflow.repository.InvoiceRepository;
import com.ncpl.sales.cashflow.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/cashflow-analyzer/api/cashflow/tally/projected-forecast")
public class ProjectedCashflowController {
    private final InvoiceRepository invoices; private final FinanceCashPlanService plan; private final ProjectedCashflowService projection;
    public ProjectedCashflowController(InvoiceRepository invoices,FinanceCashPlanService plan,ProjectedCashflowService projection){this.invoices=invoices;this.plan=plan;this.projection=projection;}
    @GetMapping
    public ResponseEntity<?> forecast(
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue="weekly") String granularity,
            @RequestParam(required=false) BigDecimal openingBalance,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate openingDate) {
        try {
            if(openingBalance==null){FinanceCashPlanSettingsEntity s=plan.getSettings();if(s.getBalanceAsOf()!=null){openingBalance=s.getOpeningBalance();openingDate=s.getBalanceAsOf();}}
            return ResponseEntity.ok(projection.project(invoices.findBySource("TALLY"),from,to,granularity,openingBalance,openingDate,LocalDate.now()));
        }catch(IllegalArgumentException e){return ResponseEntity.badRequest().body(Collections.singletonMap("error",e.getMessage()));}
    }
    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody List<Long> ids) throws Exception {
        Set<Long> selected=new HashSet<>(ids);
        List<InvoiceEntity> bills=new ArrayList<>();for(InvoiceEntity b:invoices.findBySource("TALLY"))if(selected.contains(b.getId()))bills.add(b);
        try(XSSFWorkbook book=new XSSFWorkbook();ByteArrayOutputStream output=new ByteArrayOutputStream()){
            Sheet sheet=book.createSheet("Forecast invoices");String[] headings={"Direction","Party","Invoice","Invoice date","Due date","Expected cash date","Outstanding"};Row head=sheet.createRow(0);for(int i=0;i<headings.length;i++)head.createCell(i).setCellValue(headings[i]);
            int n=1;for(Map<String,Object> b:projection.rows(bills,LocalDate.now())){Row row=sheet.createRow(n++);String[] keys={"direction","partyName","invoiceNumber","invoiceDate","dueDate","expectedCashDate","amount"};for(int i=0;i<keys.length;i++){Object v=b.get(keys[i]);if(v instanceof BigDecimal)row.createCell(i).setCellValue(((BigDecimal)v).doubleValue());else row.createCell(i).setCellValue(v==null?"":v.toString());}}
            for(int i=0;i<headings.length;i++)sheet.autoSizeColumn(i);book.write(output);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=Projected-Cashflow-Invoices.xlsx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(output.toByteArray());
        }
    }
}
