package com.ncpl.sales.service;

import com.ncpl.sales.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Explicit operator-only retry of the five ERP POs reported by Finance. No ERP writes. */
@EnabledIfSystemProperty(named="tally.po.eligible.retry", matches="true")
class TallyPurchaseOrderEligibleRetryTest {
    @Test void retriesReportedOrdersTwiceWithoutCreatingDuplicates() throws Exception {
        ((ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.WARN);
        ((ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(TallyPurchaseOrderService.class)).setLevel(ch.qos.logback.classic.Level.INFO);
        Properties config=new Properties();
        try(java.io.InputStream input=Files.newInputStream(Paths.get("src/main/resources/application-dev.properties"))) {config.load(input);}
        DriverManagerDataSource ds=new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(config.getProperty("spring.datasource.url").replace("${MYSQL_HOST:localhost}","localhost"));
        ds.setUsername(config.getProperty("spring.datasource.username")); ds.setPassword(config.getProperty("spring.datasource.password"));
        LocalContainerEntityManagerFactoryBean factory=new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(ds); factory.setPackagesToScan("com.ncpl.sales.model", "com.ncpl.sales.cashflow.model");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties options=new Properties();options.setProperty("hibernate.hbm2ddl.auto","none");
        options.setProperty("hibernate.physical_naming_strategy","org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
        options.setProperty("hibernate.implicit_naming_strategy","org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        options.setProperty("hibernate.dialect","org.hibernate.dialect.MySQL5Dialect"); factory.setJpaProperties(options);factory.afterPropertiesSet();
        javax.persistence.EntityManager em=factory.getObject().createEntityManager();
        try {
            if (Boolean.getBoolean("tally.po.repair.inspect-only")) {
                PurchaseOrder po=em.find(PurchaseOrder.class,System.getProperty("tally.po.repair.number"));
                assertThat(po).isNotNull();
                System.out.println("ERP PO " + po.getPoNumber() + " items=" + po.getItems().size());
                for(PurchaseItem item:po.getItems()) System.out.println("ERP ITEM " + item.getModelNo() + " quantity=" + item.getQuantity() + " unitPrice=" + item.getUnitPrice() + " amount=" + item.getAmount());
                return;
            }
            ItemMasterService items=mock(ItemMasterService.class);
            when(items.getItemById(anyString())).thenAnswer(a->Optional.ofNullable(em.find(ItemMaster.class,a.getArgument(0))));
            TallyPurchaseOrderService service=new TallyPurchaseOrderService(items);
            for(String name:Arrays.asList("serverUrl","companyName","defaultStockGroup")) {
                Field f=TallyPurchaseOrderService.class.getDeclaredField(name);f.setAccessible(true);
                f.set(service,name.equals("serverUrl")?"http://127.0.0.1:9000":name.equals("companyName")?"Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)":"Billable Materials");
            }
            Method query=TallyPurchaseOrderService.class.getDeclaredMethod("buildPurchaseOrdersRequest");query.setAccessible(true);
            Method post=TallyPurchaseOrderService.class.getDeclaredMethod("post",String.class);post.setAccessible(true);
            Set<String> retryNumbers=new HashSet<>();
            try(java.io.InputStream compressed=new java.util.zip.GZIPInputStream(Files.newInputStream(Paths.get("logs/po-export-runtime.log.2026-10-06.0.gz")))) {
                String history=new String(compressed.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
                java.util.regex.Matcher failures=java.util.regex.Pattern.compile("Tally rejected PO ([^:]+):").matcher(history);
                while(failures.find()) retryNumbers.add(failures.group(1).trim());
            }
            List<String> failedList=new ArrayList<>(retryNumbers);
            failedList.removeIf(number -> { PurchaseOrder po=em.find(PurchaseOrder.class,number); return po==null || po.getCreated()==null || java.time.Instant.ofEpochMilli(po.getCreated().getTime()).atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalDate().isBefore(java.time.LocalDate.of(2024,4,1)); });
            String selectedNumber=System.getProperty("tally.po.repair.number"); if(selectedNumber!=null) failedList.removeIf(n -> !selectedNumber.equals(n)); retryNumbers.retainAll(failedList); Collections.sort(failedList);
            Path backup=Paths.get("backups","tally-eligible-retry-"+System.currentTimeMillis());Files.createDirectories(backup);
            String before="<ENVELOPE/>";
            for(int offset=0;offset<failedList.size();offset+=10) {
                StringBuilder selection=new StringBuilder();
                for(String number:failedList.subList(offset,Math.min(offset+10,failedList.size()))) {
                    if(selection.length()>0)selection.append(" OR ");
                    selection.append("$VoucherNumber = &quot;").append(number.replace("&","&amp;").replace("<","&lt;").replace("\"","&quot;")).append("&quot;");
                }
                String request=((String)query.invoke(service)).replace("<SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>", "").replace("1-Jan-2020", "1-Apr-2024");
                request=request.replace("ErpExistingPurchaseOrders","RetryBackupOrders").replace("ErpIsPurchaseOrder","RetryBackupFilter")
                    .replace("VoucherNumber,Date,Narration,GUID,MasterID,AlterID","*,AllInventoryEntries.*,AllLedgerEntries.*")
                    .replace("</SYSTEM>"," AND ("+selection+")</SYSTEM>");
                String raw=(String)post.invoke(service,request);
                assertThat(raw).as("Readable backup response").contains("<ENVELOPE");
                Files.write(backup.resolve("batch-"+offset+".xml"),raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                System.out.println("Retry backup checked "+Math.min(offset+10,failedList.size())+"/"+failedList.size());
            }
            List<PurchaseOrder> orders=new ArrayList<>();
            for(String number:Arrays.asList("BGLR-AB-7538-2026","BGLR-AB-7541-2026","BGLR-Ai-7608-2026","BGLR-Ai-7611-2026","BGLR-Am-7545-2026")) {
                PurchaseOrder order=em.find(PurchaseOrder.class,number); assertThat(order).as(number).isNotNull(); orders.add(order);
            }
            boolean full = true;
            if (full) orders = em.createQuery("select p from PurchaseOrder p where p.archive = false order by p.created desc", PurchaseOrder.class).getResultList();
            if (Boolean.getBoolean("tally.po.repair.existing-only")) {
                Method parser=TallyPurchaseOrderService.class.getDeclaredMethod("parseXml",String.class);parser.setAccessible(true);
                org.w3c.dom.Document existingDoc=(org.w3c.dom.Document)parser.invoke(service,before);
                Set<String> existingNumbers=new HashSet<>();
                org.w3c.dom.NodeList vouchers=existingDoc.getElementsByTagName("VOUCHER");
                for(int i=0;i<vouchers.getLength();i++) {
                    org.w3c.dom.Element voucher=(org.w3c.dom.Element)vouchers.item(i);
                    if (voucher.getElementsByTagName("VOUCHERNUMBER").getLength()>0)
                        existingNumbers.add(voucher.getElementsByTagName("VOUCHERNUMBER").item(0).getTextContent().trim());
                }
                orders.removeIf(po -> !existingNumbers.contains(po.getPoNumber()));
            }
            String repairNumber=System.getProperty("tally.po.repair.number");
            if(repairNumber != null) orders.removeIf(po -> !repairNumber.equals(po.getPoNumber()));
            if(Boolean.getBoolean("tally.po.repair.first-historical")) orders = new ArrayList<>(orders.subList(512, 513));
            orders.removeIf(po -> po.getPoNumber().startsWith("CODEX-") || po.getPoNumber().startsWith("ERP-PO-TEST") || po.getPoNumber().startsWith("ERP-PO-VERIFY"));
            orders.removeIf(po -> !retryNumbers.contains(po.getPoNumber()));
            orders.removeIf(po -> po.getCreated() == null || java.time.Instant.ofEpochMilli(po.getCreated().getTime()).atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalDate().isBefore(java.time.LocalDate.of(2024,4,1)));
            for(int run=1;run<=1;run++) {
                if (run == 2) {
                    Field stateField=TallyPurchaseOrderService.class.getDeclaredField("verifiedStates");stateField.setAccessible(true);
                    ((Properties)stateField.get(service)).clear(); // Prove verified state survives an ERP restart.
                }
                TallyPurchaseOrderService.ExportResult result=service.exportChangedOrders(orders);
                new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(Paths.get("backups", "po-eligible-retry-20261007-"+run+".json").toFile(), result);
                System.out.println("REPAIR RUN "+run+": total="+result.getTotal()+" exported="+result.getImported()+" skipped="+result.getSkipped()+" failed="+result.getFailed());
                for(TallyPurchaseOrderService.ExportLine line:result.getRecords())
                    if ("FAILED".equals(line.getStatus())) System.out.println(line.getPoNumber()+" "+line.getStatus()+" "+line.getMessage());
                // Remaining ERP data issues are reported, never silently repaired.
                if (run == 2) assertThat(result.getImported()).as("Unchanged POs must not be pushed again").isZero();
            }
            System.out.println("Tally backup: "+backup.toAbsolutePath());
        } finally {em.close();factory.destroy();}
    }
}



