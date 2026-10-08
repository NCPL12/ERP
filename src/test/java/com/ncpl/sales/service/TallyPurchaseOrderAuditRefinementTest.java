package com.ncpl.sales.service;
import java.util.*;import java.nio.file.*;import java.math.*;import org.junit.jupiter.api.Test;import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
@EnabledIfSystemProperty(named="tally.po.audit.refine",matches="true")
class TallyPurchaseOrderAuditRefinementTest {
 @Test void explainRecordedRoundoffDifferencesWithoutContactingTally() throws Exception {
  com.fasterxml.jackson.databind.ObjectMapper json=new com.fasterxml.jackson.databind.ObjectMapper();
  List<Map<String,Object>> rows=json.readValue(Paths.get("backups/po-tally-comparison-20261006.json").toFile(),new com.fasterxml.jackson.core.type.TypeReference<List<Map<String,Object>>>(){});
  java.lang.reflect.Method parser=TallyPurchaseOrderAuditTest.method("parseXml",String.class);
  TallyPurchaseOrderService service=new TallyPurchaseOrderService(null);
  Map<String,org.w3c.dom.Element> vouchers=new HashMap<>();
  try(java.util.stream.Stream<Path> files=Files.list(Paths.get("backups"))){for(Path file:(Iterable<Path>)files.filter(p->p.getFileName().toString().startsWith("po-readonly-audit-batch-"))::iterator){org.w3c.dom.Document doc=(org.w3c.dom.Document)parser.invoke(service,Files.readString(file));org.w3c.dom.NodeList list=doc.getElementsByTagName("VOUCHER");for(int i=0;i<list.getLength();i++){org.w3c.dom.Element v=(org.w3c.dom.Element)list.item(i);vouchers.put(TallyPurchaseOrderAuditTest.text(v,"VOUCHERNUMBER"),v);}}}
  int explained=0;
  for(Map<String,Object> row:rows){
   if(!"DIFFERENCES".equals(row.get("status")))continue;
   List<String> differences=(List<String>)row.get("differences");if(differences.size()!=1 || !differences.get(0).startsWith("Total:"))continue;
   org.w3c.dom.Element voucher=vouchers.get(row.get("poNumber"));if(voucher==null)continue;
   BigDecimal party=BigDecimal.ZERO,roundoff=BigDecimal.ZERO;
   String vendor=TallyPurchaseOrderAuditTest.text(voucher,"PARTYLEDGERNAME");
   for(org.w3c.dom.Element ledger:TallyPurchaseOrderAuditTest.children(voucher,"ALLLEDGERENTRIES.LIST")){
    String name=TallyPurchaseOrderAuditTest.text(ledger,"LEDGERNAME");BigDecimal amount=TallyPurchaseOrderAuditTest.number(TallyPurchaseOrderAuditTest.text(ledger,"AMOUNT"));
    if("Yes".equalsIgnoreCase(TallyPurchaseOrderAuditTest.text(ledger,"ISPARTYLEDGER"))||TallyPurchaseOrderAuditTest.norm(name).equals(TallyPurchaseOrderAuditTest.norm(vendor)))party=party.add(amount);
    else if(TallyPurchaseOrderAuditTest.norm(name).contains("ROUND"))roundoff=roundoff.add(amount);
   }
   BigDecimal expected=new BigDecimal(row.get("erpTotal").toString());
   if(roundoff.signum()!=0 && roundoff.abs().compareTo(BigDecimal.ONE)<=0 && party.add(roundoff).abs().compareTo(expected)==0){row.put("status","ROUNDING_ONLY");row.put("tallyRoundoffLedgerAmount",roundoff);differences.add("Difference is fully explained by the Tally round-off ledger; items, vendor, date and GST otherwise match.");explained++;}
  }
  json.writerWithDefaultPrettyPrinter().writeValue(Paths.get("backups/po-tally-comparison-20261006.json").toFile(),rows);TallyPurchaseOrderAuditTest.writeHtml(rows);
  Map<String,Integer> counts=new TreeMap<>();for(Map<String,Object> row:rows)counts.merge(row.get("status").toString(),1,Integer::sum);System.out.println("AUDIT SUMMARY: checked="+rows.size()+"; "+counts+"; rounding explained this pass="+explained);
 }
}
