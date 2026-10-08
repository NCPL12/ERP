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
@EnabledIfSystemProperty(named="tally.po.audit", matches="true")
class TallyPurchaseOrderAuditTest {
    @Test void comparesProtectedPurchaseOrdersWithoutWrites() throws Exception {
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
            com.fasterxml.jackson.databind.ObjectMapper json=new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode previous=json.readTree(Paths.get("backups/po-full-repair-run-1.json").toFile());
            List<String> numbers=new ArrayList<>();
            for(com.fasterxml.jackson.databind.JsonNode row:previous.path("records")) if("SKIPPED".equals(row.path("status").asText())) numbers.add(row.path("poNumber").asText());
            assertThat(numbers.size()).isEqualTo(937);
            Method post=method("post",String.class),parse=method("parseXml",String.class),load=method("loadMasterSnapshot",boolean.class);
            Object snapshot=load.invoke(service,true);
            Method prepare=method("prepare",PurchaseOrder.class,snapshot.getClass());
            List<Map<String,Object>> report=new ArrayList<>();
            if(Boolean.getBoolean("tally.po.audit.resume") && Files.exists(Paths.get("backups","po-tally-comparison-20261006.json"))) report=json.readValue(Paths.get("backups","po-tally-comparison-20261006.json").toFile(),new com.fasterxml.jackson.core.type.TypeReference<List<Map<String,Object>>>(){});
            Map<String,String> identities=new HashMap<>();
            for(int start=report.size();start<numbers.size();start+=10){
                List<String> batch=numbers.subList(start,Math.min(start+10,numbers.size()));
                StringBuilder filter=new StringBuilder();
                for(String number:batch){if(filter.length()>0)filter.append(" OR ");filter.append("$VoucherNumber = &quot;").append(xml(number)).append("&quot;");}
                String request="<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>ErpProtectedPoAudit</ID></HEADER><BODY><DESC><STATICVARIABLES>"
                    +"<SVCURRENTCOMPANY>Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)</SVCURRENTCOMPANY><SVFROMDATE TYPE=\"Date\">1-Jan-2020</SVFROMDATE><SVTODATE TYPE=\"Date\">"+java.time.LocalDate.now().plusYears(1).format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy",Locale.ENGLISH))+"</SVTODATE>"
                    +"</STATICVARIABLES><TDL><TDLMESSAGE><COLLECTION NAME=\"ErpProtectedPoAudit\"><TYPE>Voucher</TYPE><FILTER>ErpProtectedPoAuditFilter</FILTER><FETCH>*,AllInventoryEntries.*,AllLedgerEntries.*</FETCH></COLLECTION><SYSTEM TYPE=\"Formulae\" NAME=\"ErpProtectedPoAuditFilter\">$VoucherTypeName = &quot;Purchase Order&quot; AND ("+filter+")</SYSTEM></TDLMESSAGE></TDL></DESC></BODY></ENVELOPE>";
                String raw=(String)post.invoke(service,request);
                Files.write(Paths.get("backups","po-readonly-audit-batch-"+start+".xml"),raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                org.w3c.dom.Document doc=(org.w3c.dom.Document)parse.invoke(service,raw);
                for(String number:batch){
                    Map<String,Object> row=new LinkedHashMap<>();row.put("poNumber",number);
                    List<String> differences=new ArrayList<>(),review=new ArrayList<>();
                    PurchaseOrder source=em.find(PurchaseOrder.class,number);
                    org.w3c.dom.Element voucher=null;int found=0;
                    org.w3c.dom.NodeList all=doc.getElementsByTagName("VOUCHER");
                    for(int i=0;i<all.getLength();i++){org.w3c.dom.Element v=(org.w3c.dom.Element)all.item(i);if(number.equals(text(v,"VOUCHERNUMBER"))){voucher=v;found++;}}
                    if(source==null)review.add("ERP PO no longer exists.");
                    if(found!=1)review.add(found==0?"PO not returned by live Tally query.":"Multiple Tally vouchers use this PO number.");
                    if(source!=null && found==1){
                        String expectedDate=source.getCreated()==null?"":java.time.Instant.ofEpochMilli(source.getCreated().getTime()).atZone(java.time.ZoneId.of("Asia/Kolkata")).toLocalDate().toString();
                        String date=text(voucher,"DATE");String actualDate=date.matches("\\d{8}")?java.time.LocalDate.parse(date,java.time.format.DateTimeFormatter.BASIC_ISO_DATE).toString():date;
                        row.put("erpDate",expectedDate);row.put("tallyDate",actualDate);
                        if(!expectedDate.equals(actualDate))differences.add("Date: ERP "+expectedDate+"; Tally "+actualDate);
                        String expectedVendor=source.getParty()==null?"":source.getParty().getPartyName();String actualVendor=text(voucher,"PARTYLEDGERNAME");
                        row.put("erpVendor",expectedVendor);row.put("tallyVendor",actualVendor);
                        if(!norm(expectedVendor).equals(norm(actualVendor))){
                            String left="",right=""; try { left=ledgerGuid(service,post,parse,identities,expectedVendor); right=ledgerGuid(service,post,parse,identities,actualVendor); } catch(Exception identityError) { review.add("Vendor ledger identity lookup timed out or failed; identity remains unverified."); }
                            if(left.isEmpty()||right.isEmpty())review.add("Vendor names differ; ledger identity could not be confirmed.");
                            else if(!left.equals(right))differences.add("Vendor: ERP "+expectedVendor+"; Tally "+actualVendor);
                        }
                        try{
                            Object expected=prepare.invoke(service,source,snapshot);
                            List<?> expectedItems=(List<?>)field(expected,"items");
                            List<String> expectedSignatures=new ArrayList<>(),actualSignatures=new ArrayList<>();
                            java.math.BigDecimal expectedBase=java.math.BigDecimal.ZERO,actualBase=java.math.BigDecimal.ZERO;
                            for(Object item:expectedItems){expectedSignatures.add(signature((String)field(item,"name"),(java.math.BigDecimal)field(item,"quantity"),(java.math.BigDecimal)field(item,"rate"),(java.math.BigDecimal)field(item,"amount"),(String)field(item,"unit")));expectedBase=expectedBase.add((java.math.BigDecimal)field(item,"amount"));}
                            List<org.w3c.dom.Element> inventories=children(voucher,"ALLINVENTORYENTRIES.LIST");if(inventories.isEmpty())inventories=children(voucher,"INVENTORYENTRIES.LIST");
                            for(org.w3c.dom.Element item:inventories){String qty=text(item,"BILLEDQTY");if(qty.isEmpty())qty=text(item,"ACTUALQTY");java.math.BigDecimal amount=number(text(item,"AMOUNT")).abs();actualBase=actualBase.add(amount);String unit=qty.replaceFirst("^[\\s+\\-0-9.,]+","").trim();actualSignatures.add(signature(text(item,"STOCKITEMNAME"),number(qty).abs(),number(text(item,"RATE")).abs(),amount,unit));}
                            Collections.sort(expectedSignatures);Collections.sort(actualSignatures);
                            row.put("erpItems",expectedSignatures);row.put("tallyItems",actualSignatures);
                            if(!expectedSignatures.equals(actualSignatures))differences.add("Item names, quantities, units, rates or amounts differ; see both item lists.");
                            if(expectedBase.compareTo(actualBase)!=0)differences.add("Taxable amount: ERP "+expectedBase+"; Tally "+actualBase);
                            row.put("erpTaxable",expectedBase);row.put("tallyTaxable",actualBase);
                            Map<String,java.math.BigDecimal> expectedTaxes=new TreeMap<>(),actualTaxes=new TreeMap<>();
                            Map<String,java.math.BigDecimal> taxLedgers=(Map<String,java.math.BigDecimal>)field(expected,"taxLedgers");
                            for(Map.Entry<String,java.math.BigDecimal> tax:taxLedgers.entrySet())addTax(expectedTaxes,tax.getKey(),tax.getValue());
                            java.math.BigDecimal actualTotal=java.math.BigDecimal.ZERO;int parties=0;
                            List<org.w3c.dom.Element> ledgers=children(voucher,"ALLLEDGERENTRIES.LIST");if(ledgers.isEmpty())ledgers=children(voucher,"LEDGERENTRIES.LIST");
                            for(org.w3c.dom.Element ledger:ledgers){java.math.BigDecimal amount=number(text(ledger,"AMOUNT")).abs();String name=text(ledger,"LEDGERNAME");addTax(actualTaxes,name,amount);if("Yes".equalsIgnoreCase(text(ledger,"ISPARTYLEDGER"))||norm(name).equals(norm(actualVendor))){actualTotal=actualTotal.add(amount);parties++;}}
                            if(!expectedTaxes.equals(actualTaxes)){Set<String> kinds=new TreeSet<>(expectedTaxes.keySet());kinds.addAll(actualTaxes.keySet());for(String kind:kinds){java.math.BigDecimal a=expectedTaxes.getOrDefault(kind,java.math.BigDecimal.ZERO),b=actualTaxes.getOrDefault(kind,java.math.BigDecimal.ZERO);if(a.compareTo(b)!=0)differences.add(kind+": ERP "+a+"; Tally "+b);}}
                            row.put("erpGST",expectedTaxes);row.put("tallyGST",actualTaxes);
                            java.math.BigDecimal total=(java.math.BigDecimal)field(expected,"total");row.put("erpTotal",total);row.put("tallyTotal",actualTotal);
                            if(parties==0)review.add("Tally party amount is unavailable.");else if(total.compareTo(actualTotal)!=0)differences.add("Total: ERP "+total+"; Tally "+actualTotal);
                        }catch(java.lang.reflect.InvocationTargetException problem){review.add("ERP calculation needs review: "+problem.getCause().getMessage());}
                    }
                    row.put("differences",differences);row.put("reviewNotes",review);row.put("status",!review.isEmpty()?"NEEDS_REVIEW":differences.isEmpty()?"MATCHING":"DIFFERENCES");report.add(row);
                }
                json.writerWithDefaultPrettyPrinter().writeValue(Paths.get("backups","po-tally-comparison-20261006.json").toFile(),report);
                writeHtml(report);
                System.out.println("Read-only comparison checked "+report.size()+"/"+numbers.size());
            }
            Map<String,Integer> counts=new TreeMap<>();for(Map<String,Object> row:report)counts.merge(row.get("status").toString(),1,Integer::sum);
            System.out.println("READ-ONLY AUDIT: "+counts);assertThat(report.size()).isEqualTo(937);
        }finally{em.close();factory.destroy();}
    }
    static Method method(String name,Class<?>...types)throws Exception{Method m=TallyPurchaseOrderService.class.getDeclaredMethod(name,types);m.setAccessible(true);return m;}
    static Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    static String text(org.w3c.dom.Element element,String tag){org.w3c.dom.NodeList n=element.getElementsByTagName(tag);return n.getLength()==0?"":n.item(0).getTextContent().trim();}
    static List<org.w3c.dom.Element> children(org.w3c.dom.Element element,String tag){List<org.w3c.dom.Element> list=new ArrayList<>();for(org.w3c.dom.Node node=element.getFirstChild();node!=null;node=node.getNextSibling())if(node instanceof org.w3c.dom.Element && tag.equals(node.getNodeName()))list.add((org.w3c.dom.Element)node);return list;}
    static String norm(String value){return value==null?"":value.trim().replaceAll("\\s+"," ").toUpperCase(Locale.ENGLISH);}
    static String xml(String value){return value==null?"":value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");}
    static java.math.BigDecimal number(String value){java.util.regex.Matcher m=java.util.regex.Pattern.compile("[-+]?\\d+(?:\\.\\d+)?").matcher(value.replace(",",""));return m.find()?new java.math.BigDecimal(m.group()):java.math.BigDecimal.ZERO;}
    static String signature(String name,java.math.BigDecimal qty,java.math.BigDecimal rate,java.math.BigDecimal amount,String unit){return norm(name)+" | qty "+qty.stripTrailingZeros().toPlainString()+" "+norm(unit)+" | rate "+rate.stripTrailingZeros().toPlainString()+" | amount "+amount.stripTrailingZeros().toPlainString();}
    static void addTax(Map<String,java.math.BigDecimal> target,String name,java.math.BigDecimal amount){String upper=norm(name);for(String kind:Arrays.asList("CGST","SGST","IGST","UTGST","CESS"))if(upper.contains(kind)){target.merge(kind,amount,(a,b)->a.add(b));break;}}
    static String ledgerGuid(TallyPurchaseOrderService service,Method post,Method parse,Map<String,String> cache,String name)throws Exception{
        if(cache.containsKey(name))return cache.get(name);String request="<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Object</TYPE><SUBTYPE>Ledger</SUBTYPE><ID TYPE=\"Name\">"+xml(name)+"</ID></HEADER><BODY><DESC><STATICVARIABLES><SVCURRENTCOMPANY>Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)</SVCURRENTCOMPANY></STATICVARIABLES><FETCHLIST><FETCH>GUID</FETCH></FETCHLIST></DESC></BODY></ENVELOPE>";
        org.w3c.dom.Document doc=(org.w3c.dom.Document)parse.invoke(service,post.invoke(service,request));String guid="";org.w3c.dom.NodeList ledgers=doc.getElementsByTagName("LEDGER");for(int i=0;i<ledgers.getLength();i++){org.w3c.dom.Element ledger=(org.w3c.dom.Element)ledgers.item(i);if(ledger.hasAttribute("NAME")){guid=text(ledger,"GUID");break;}}cache.put(name,guid);return guid;
    }
    static void writeHtml(List<Map<String,Object>> rows)throws Exception{
        StringBuilder html=new StringBuilder("<!doctype html><html><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>ERP / Tally PO comparison</title><style>body{font:16px system-ui;background:#f5f7fb;color:#182438;margin:24px}input{padding:12px;width:90%;max-width:600px}details{background:white;border:1px solid #ccd5df;border-radius:8px;padding:14px;margin:12px 0}summary{cursor:pointer;font-weight:bold}.MATCHING{border-left:6px solid #14965c}.DIFFERENCES{border-left:6px solid #cf3838}.NEEDS_REVIEW{border-left:6px solid #d79b19}.ROUNDING_ONLY{border-left:6px solid #287dca}pre{white-space:pre-wrap;font:14px system-ui;line-height:1.6}</style><h1>ERP / Tally purchase-order comparison</h1><p>Protected-voucher comparison captured 6 October 2026; retry results updated 7 October 2026. This comparison did not alter purchase orders in ERP or Tally. Scope: 937 existing POs without the ERP-import marker.</p><p>Checks: vendor identity, date, item names/quantities/units/rates/amounts, taxable amount, GST components and total. Expected GST uses the ERP item's current GST configuration; historical tax changes need Finance review. This report does not certify HSN, delivery terms or ledger posting configuration.</p>");
        Map<String,Integer> counts=new TreeMap<>();for(Map<String,Object> row:rows)counts.merge(row.get("status").toString(),1,Integer::sum);
        html.append("<p>Checked: ").append(rows.size()).append(" / 937 · ").append(xml(counts.toString())).append("</p><input id='q' placeholder='Search PO, vendor, status or difference' oninput='document.querySelectorAll(\"details\").forEach(x=>x.hidden=!x.textContent.toLowerCase().includes(this.value.toLowerCase()))'>");
        for(Map<String,Object> row:rows){html.append("<details class='").append(row.get("status")).append("'><summary>").append(xml(row.get("poNumber").toString())).append(" — ").append(row.get("status")).append("</summary><pre>");for(Map.Entry<String,Object> entry:row.entrySet())html.append(xml(entry.getKey()+": "+entry.getValue())).append("\n");html.append("</pre></details>");}
        java.nio.file.Path retryPath=Paths.get("backups","po-eligible-retry-20261007-1.json");
        if(Files.exists(retryPath)) {
            com.fasterxml.jackson.databind.JsonNode retry=new com.fasterxml.jackson.databind.ObjectMapper().readTree(retryPath.toFile());
            html.append("<h2>Eligible failed-PO retry, 7 October 2026</h2><p>This section is separate from the 937 protected-voucher comparison. Exported counts passed vendor, total and GST read-back verification.</p><p>Total: ").append(retry.path("total").asInt()).append(" · Verified exports: ").append(retry.path("imported").asInt()).append(" · Skipped: ").append(retry.path("skipped").asInt()).append(" · Failed: ").append(retry.path("failed").asInt()).append("</p>");
            for(com.fasterxml.jackson.databind.JsonNode record:retry.path("records")) html.append("<details><summary>").append(xml(record.path("poNumber").asText())).append(" — ").append(xml(record.path("status").asText())).append("</summary><p>").append(xml(record.path("message").asText())).append("</p></details>");
        }
        html.append("</html>");Files.write(Paths.get("docs","po-tally-comparison-20261006.html"),html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}


