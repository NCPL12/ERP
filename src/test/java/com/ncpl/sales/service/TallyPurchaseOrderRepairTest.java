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
@EnabledIfSystemProperty(named="tally.po.repair", matches="true")
class TallyPurchaseOrderRepairTest {
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
            ItemMasterService items=mock(ItemMasterService.class);
            when(items.getItemById(anyString())).thenAnswer(a->Optional.ofNullable(em.find(ItemMaster.class,a.getArgument(0))));
            TallyPurchaseOrderService service=new TallyPurchaseOrderService(items);
            for(String name:Arrays.asList("serverUrl","companyName","defaultStockGroup")) {
                Field f=TallyPurchaseOrderService.class.getDeclaredField(name);f.setAccessible(true);
                f.set(service,name.equals("serverUrl")?"http://127.0.0.1:9000":name.equals("companyName")?"Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)":"Billable Materials");
            }
            Method query=TallyPurchaseOrderService.class.getDeclaredMethod("buildPurchaseOrdersRequest");query.setAccessible(true);
            Method post=TallyPurchaseOrderService.class.getDeclaredMethod("post",String.class);post.setAccessible(true);
            Path backup=Paths.get("backups","tally-before-duplicate-repair-"+System.currentTimeMillis()+".xml");
            String request=(String)query.invoke(service);
            String existingBackup=System.getProperty("tally.po.repair.backup");
            String before=existingBackup == null ? (String)post.invoke(service,request.replace("VoucherNumber,Date,Narration,GUID,MasterID,AlterID","*")) : Files.readString(Paths.get(existingBackup));
            Files.write(backup,before.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            List<PurchaseOrder> orders=new ArrayList<>();
            for(String number:Arrays.asList("BGLR-AB-7538-2026","BGLR-AB-7541-2026","BGLR-Ai-7608-2026","BGLR-Ai-7611-2026","BGLR-Am-7545-2026")) {
                PurchaseOrder order=em.find(PurchaseOrder.class,number); assertThat(order).as(number).isNotNull(); orders.add(order);
            }
            boolean full = Boolean.getBoolean("tally.po.repair.all");
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
            for(int run=1;run<=2;run++) {
                if (run == 2) {
                    Field stateField=TallyPurchaseOrderService.class.getDeclaredField("verifiedStates");stateField.setAccessible(true);
                    ((Properties)stateField.get(service)).clear(); // Prove verified state survives an ERP restart.
                }
                TallyPurchaseOrderService.ExportResult result=service.exportChangedOrders(orders);
                new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValue(Paths.get("backups", "po-full-repair-run-"+run+".json").toFile(), result);
                System.out.println("REPAIR RUN "+run+": total="+result.getTotal()+" exported="+result.getImported()+" skipped="+result.getSkipped()+" failed="+result.getFailed());
                for(TallyPurchaseOrderService.ExportLine line:result.getRecords())
                    if ("FAILED".equals(line.getStatus())) System.out.println(line.getPoNumber()+" "+line.getStatus()+" "+line.getMessage());
                assertThat(result.getFailed()).isZero();
                if (run == 2) assertThat(result.getImported()).as("Unchanged POs must not be pushed again").isZero();
            }
            System.out.println("Tally backup: "+backup.toAbsolutePath());
        } finally {em.close();factory.destroy();}
    }
}



