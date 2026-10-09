package com.ncpl.sales.service;

import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Creates Tally Purchase Order vouchers from ERP purchase orders.
 * Missing vendor and stock-item masters are created using ERP data and existing Tally units.
 */
@Service
public class TallyPurchaseOrderService {
    private static final Logger log = LoggerFactory.getLogger(TallyPurchaseOrderService.class);
    private static final DateTimeFormatter TALLY_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TALLY_DUE_DATE = DateTimeFormatter.ofPattern("d-MMM-yy", Locale.ENGLISH);
    private static final DateTimeFormatter TALLY_IDENTIFIER_DATE = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
    private static final String PURCHASE_ACCOUNTS = "PURCHASE ACCOUNTS";

    private final ItemMasterService itemMasterService;
    @Value("${tally.po.sync.state-file:data/tally-po-sync.properties}")
    private String syncStateFile = "data/tally-po-sync.properties";
    private final Map<String, String> canonicalLedgerNames = new HashMap<>();
    private final java.util.Properties verifiedStates = new java.util.Properties();
    private final java.util.concurrent.atomic.AtomicBoolean exportRunning = new java.util.concurrent.atomic.AtomicBoolean();

    @Value("${cashflow.tally.server.url:http://localhost:9000}")
    private String serverUrl;

    @Value("${cashflow.tally.company-name:}")
    private String companyName;

    @Value("${cashflow.tally.default-stock-group:Billable Materials}")
    private String defaultStockGroup;

    public TallyPurchaseOrderService(ItemMasterService itemMasterService) {
        this.itemMasterService = itemMasterService;
    }

    public Options loadOptions() throws Exception {
        MasterSnapshot snapshot = loadMasterSnapshot(false);
        return new Options(serverUrl, blank(companyName) ? "Company currently open in Tally" : companyName,
                snapshot.purchaseLedgers);
    }

    public ExportResult exportAllPending(List<PurchaseOrder> purchaseOrders) throws Exception {
        return exportWithLock(purchaseOrders, false);
    }

    public ExportResult exportChangedOrders(List<PurchaseOrder> purchaseOrders) throws Exception {
        return exportWithLock(purchaseOrders, true);
    }

    public boolean isExportRunning() { return exportRunning.get(); }
    private volatile Map<String,Object> progress = Collections.emptyMap();
    private int progressOffset;
    private List<ExportLine> completedProgress = new ArrayList<>();
    public Map<String,Object> getProgress() { return progress; }
    private void publishProgress(String stage, int total, List<ExportLine> current) {
        Map<String,Object> next = new LinkedHashMap<>();
        int imported=0, updated=0, skipped=0, failed=0;
        List<ExportLine> all = new ArrayList<>(completedProgress); all.addAll(current);
        for (ExportLine line:all) {
            if ("IMPORTED".equals(line.getStatus())) imported++;
            else if ("UPDATED".equals(line.getStatus())) updated++;
            else if ("FAILED".equals(line.getStatus())) failed++;
            else skipped++;
        }
        next.put("stage",stage); next.put("total",total); next.put("processed",progressOffset+current.size());
        next.put("imported",imported); next.put("updated",updated); next.put("skipped",skipped); next.put("failed",failed);
        next.put("running",exportRunning.get()); progress=Collections.unmodifiableMap(next);
    }

    private ExportResult exportWithLock(List<PurchaseOrder> purchaseOrders, boolean onlyChanged) throws Exception {
        if (!exportRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("A PO export is already running. Wait for its result before sending again.");
        }
        if (!TallySyncCoordinator.tryAcquire()) {
            exportRunning.set(false);
            throw new IllegalStateException("Tally refresh is running. PO synchronization will retry after it finishes.");
        }
        try {
            progressOffset=0; completedProgress=new ArrayList<>(); publishProgress("Preparing Tally masters", purchaseOrders == null ? 0 : purchaseOrders.size(), Collections.emptyList());
            loadVerifiedStates();
            canonicalLedgerNames.clear();
            if (purchaseOrders == null || purchaseOrders.size() <= 250) return exportOrders(purchaseOrders, onlyChanged);
            int imported = 0, skipped = 0, failed = 0;
            List<ExportLine> records = new ArrayList<>();
            for (int start = 0; start < purchaseOrders.size(); start += 250) {
                int end = Math.min(start + 250, purchaseOrders.size());
                ExportResult chunk = exportOrders(purchaseOrders.subList(start, end), onlyChanged);
                imported += chunk.getImported(); skipped += chunk.getSkipped(); failed += chunk.getFailed();
                records.addAll(chunk.getRecords()); completedProgress=new ArrayList<>(records); progressOffset=end;
                log.info("PO sync checkpoint: checked={}/{}, exported={}, skipped={}, failed={}",
                        end, purchaseOrders.size(), imported, skipped, failed);
            }
            return new ExportResult(purchaseOrders.size(), imported, skipped, failed, records);
        } catch (Exception error) {
            Map<String,Object> errorProgress=new LinkedHashMap<>(progress);
            errorProgress.put("stage", "Stopped: " + safeMessage(error));
            progress=Collections.unmodifiableMap(errorProgress);
            throw error;
        } finally {
            exportRunning.set(false);
            Map<String,Object> finalProgress=new LinkedHashMap<>(progress); finalProgress.put("running",false); if ("Batch verified".equals(finalProgress.get("stage"))) finalProgress.put("stage","Completed"); progress=Collections.unmodifiableMap(finalProgress);
            TallySyncCoordinator.release();
        }
    }

    private ExportResult exportOrders(List<PurchaseOrder> purchaseOrders, boolean onlyChanged) throws Exception {
        if (purchaseOrders == null || purchaseOrders.isEmpty()) {
            return new ExportResult(0, 0, 0, 0,
                    Collections.singletonList(new ExportLine("", "FAILED", "No active ERP purchase orders were found.")));
        }

        MasterSnapshot snapshot = loadMasterSnapshot(true);
        ensureRequiredMasters(purchaseOrders, snapshot);

        int imported = 0;
        int skipped = 0;
        int failed = 0;
        List<ExportLine> lines = new ArrayList<>();
        Map<Integer, PreparedOrder> acceptedOrders = new LinkedHashMap<>();
        Set<String> seenNumbers = new HashSet<>();

        for (PurchaseOrder purchaseOrder : purchaseOrders) {
            publishProgress("Sending (provisional until verification)", ((Number)progress.get("total")).intValue(), lines);
            String poNumber = trim(purchaseOrder == null ? null : purchaseOrder.getPoNumber());
            if (blank(poNumber)) {
                failed++;
                lines.add(new ExportLine("", "FAILED", "ERP PO number is missing."));
                continue;
            }
            String numberKey = normalize(poNumber);
            if (!seenNumbers.add(numberKey)) {
                skipped++;
                lines.add(new ExportLine(poNumber, "SKIPPED", "Repeated ERP PO number in this batch; already processed."));
                continue;
            }
            ExistingVoucher existingVoucher = snapshot.purchaseOrders.get(numberKey);
            boolean updateExistingErpImport = existingVoucher != null
                    && existingVoucher.narration.startsWith("Imported from ERP");
            if (existingVoucher != null && !updateExistingErpImport) {
                skipped++;
                lines.add(new ExportLine(poNumber, "SKIPPED", "A non-ERP Purchase Order with this number already exists in Tally."));
                continue;
            }

            PreparedOrder prepared;
            try {
                prepared = prepare(purchaseOrder, snapshot);
            } catch (IllegalArgumentException validationError) {
                failed++;
                lines.add(new ExportLine(poNumber, "FAILED", validationError.getMessage()));
                continue;
            }

            if (onlyChanged && existingVoucher != null
                    && verifiedState(prepared, existingVoucher.alterId).equals(verifiedStates.getProperty(stateKey(prepared.number)))) {
                skipped++;
                lines.add(new ExportLine(poNumber, "UNCHANGED", "Already verified in Tally; no ERP or Tally change."));
                continue;
            }
            try {
                String requestXml = buildImportRequest(prepared, existingVoucher);
                String responseXml = post(requestXml);
                ImportResponse response = parseImportResponse(responseXml);
                boolean accepted = response.errors == 0 && response.exceptions == 0
                        && (updateExistingErpImport ? response.altered > 0 : response.created > 0);
                if (accepted) {
                    acceptedOrders.put(lines.size(), prepared);
                    imported++;

                    lines.add(new ExportLine(poNumber, updateExistingErpImport ? "UPDATED" : "IMPORTED",
                            updateExistingErpImport ? "Existing ERP Purchase Order updated with GST details."
                                    : "Created in Tally with GST details."));
                } else {
                    log.warn("Tally rejected PO {}: {}", poNumber, responseXml);
                    failed++;
                    lines.add(new ExportLine(poNumber, "FAILED",
                            blank(response.lineError)
                                    ? "Tally rejected the voucher (created " + response.created + ", altered "
                                            + response.altered + ", ignored " + response.ignored
                                            + ", exceptions " + response.exceptions + ")."
                                    : response.lineError));
                }
            } catch (Exception importError) {
                failed++;
                lines.add(new ExportLine(poNumber, "FAILED", safeMessage(importError)));
            }
        }
        publishProgress("Verifying Tally results", ((Number)progress.get("total")).intValue(), lines);
        if (!acceptedOrders.isEmpty()) {
            List<Map.Entry<Integer,PreparedOrder>> accepted=new ArrayList<>(acceptedOrders.entrySet());
            for(int offset=0;offset<accepted.size();offset+=10) {
                List<Map.Entry<Integer,PreparedOrder>> entries=accepted.subList(offset,Math.min(offset+10,accepted.size()));
                List<PreparedOrder> requests=new ArrayList<>();for(Map.Entry<Integer,PreparedOrder> entry:entries)requests.add(entry.getValue());
                Document savedDocument=null;Exception readError=null;
                try { savedDocument=parseXml(post(buildVerificationRequest(requests))); } catch(Exception error) { readError=error; }
                for(Map.Entry<Integer,PreparedOrder> entry:entries) {
                    try {
                        if(readError!=null)throw readError;
                        verifySavedOrder(entry.getValue(),savedDocument);
                        Element saved=findSavedOrder(entry.getValue().number,savedDocument);
                        verifiedStates.setProperty(stateKey(entry.getValue().number),verifiedState(entry.getValue(),firstText(saved,"ALTERID")));
                    } catch(Exception error) {
                        imported--;failed++;
                        lines.set(entry.getKey(),new ExportLine(entry.getValue().number,"FAILED","Tally accepted this PO, but verification failed: "+safeMessage(error)));
                    }
                }
                publishProgress("Verifying Tally results",((Number)progress.get("total")).intValue(),lines);
            }
        }
        publishProgress("Batch verified", ((Number)progress.get("total")).intValue(), lines);
        saveVerifiedStates();
        return new ExportResult(purchaseOrders.size(), imported, skipped, failed, lines);
    }

    private void ensureRequiredMasters(List<PurchaseOrder> orders, MasterSnapshot snapshot) {
        Set<String> attemptedLedgers = new HashSet<>();
        Set<String> attemptedItems = new HashSet<>();
        for (PurchaseOrder order : orders) {
            if (order == null) continue;
            if (order.getParty() != null && !blank(order.getParty().getPartyName())) {
                String partyKey = normalize(order.getParty().getPartyName());
                if (!snapshot.ledgerNames.containsKey(partyKey) && attemptedLedgers.add(partyKey)) {
                    try {
                        ImportResponse response = parseImportResponse(post(buildLedgerMasterRequest(order)));
                        if ((response.created > 0 || response.altered > 0)
                                && response.errors == 0 && response.exceptions == 0) {
                            snapshot.ledgerNames.put(partyKey, order.getParty().getPartyName().trim());
                        }
                    } catch (Exception ignored) { }
                }
            }
            if (order.getItems() == null) continue;
            for (PurchaseItem purchaseItem : order.getItems()) {
                if (purchaseItem == null || blank(purchaseItem.getModelNo())) continue;
                Optional<ItemMaster> optional = itemMasterService.getItemById(purchaseItem.getModelNo());
                if (!optional.isPresent()) continue;
                ItemMaster itemMaster = optional.get();
                String tallyName = firstNonBlank(itemMaster.getModel(), itemMaster.getItemName());
                if (blank(tallyName) || firstStockItem(snapshot, tallyName, itemMaster.getItemName(), purchaseItem.getModelNo()) != null) continue;
                String itemKey = normalize(tallyName);
                if (!attemptedItems.add(itemKey)) continue;
                try {
                    TallyStockItem existingAlias = loadStockItemObject(tallyName);
                    if (existingAlias != null) {
                        snapshot.stockItems.put(itemKey, existingAlias);
                        continue;
                    }
                } catch (Exception error) {
                    log.warn("Unable to resolve Tally stock item alias {}", tallyName, error);
                }
                String unit = itemMaster.getItem_units() == null ? "" : trim(itemMaster.getItem_units().getName());
                String tallyUnit = resolveTallyUnit(unit, snapshot);
                if (blank(tallyUnit)) {
                    log.warn("Cannot create Tally stock item {} because ERP unit '{}' has no Tally equivalent",
                            tallyName, unit);
                    continue;
                }
                try {
                    String responseXml = post(buildStockItemMasterRequest(
                            tallyName, tallyUnit, firstNonBlank(purchaseItem.getHsnCode(), itemMaster.getHsnCode())));
                    ImportResponse response = parseImportResponse(responseXml);
                    if ((response.created > 0 || response.altered > 0)
                            && response.errors == 0 && response.exceptions == 0) {
                        snapshot.stockItems.put(itemKey, new TallyStockItem(tallyName, tallyUnit));
                    } else {
                        log.warn("Tally rejected stock item master {}: {}", tallyName, responseXml);
                        TallyStockItem recovered = loadStockItemObject(tallyName);
                        if (recovered != null) snapshot.stockItems.put(itemKey, recovered);
                    }
                } catch (Exception error) {
                    log.warn("Unable to create Tally stock item master {}", tallyName, error);
                }
            }
        }
    }

    private String resolveTallyUnit(String erpUnit, MasterSnapshot snapshot) {
        String exact = snapshot.unitNames.get(normalize(erpUnit));
        if (!blank(exact)) return exact;
        String key = normalize(erpUnit).replaceAll("[^A-Z0-9]", "");
        String[] numberAliases = { "NO", "NOS", "NUMBER", "NUMBERS", "PCS", "PC", "PIECE", "PIECES", "EACH" };
        for (String alias : numberAliases) {
            if (key.equals(alias)) {
                String nos = snapshot.unitNames.get(normalize("Nos"));
                if (!blank(nos)) return nos;
            }
        }
        String[][] equivalentUnits = {
            {"PAIR", "PAIRS"}, {"SET", "SETS"}, {"BOX", "BOXS", "BOXES"},
            {"ROLL", "ROLLS"}, {"MTR", "MTRS", "METER", "METERS", "METRE", "METRES"},
            {"KG", "KGS", "KILOGRAM", "KILOGRAMS"}, {"LTR", "LTRS", "LITER", "LITERS", "LITRE", "LITRES"}
        };
        for (String[] equivalents : equivalentUnits) {
            if (!java.util.Arrays.asList(equivalents).contains(key)) continue;
            for (String available : snapshot.unitNames.values()) {
                String availableKey = normalize(available).replaceAll("[^A-Z0-9]", "");
                if (java.util.Arrays.asList(equivalents).contains(availableKey)) return available;
            }
        }
        if (key.startsWith("PKT") || key.startsWith("PACK") || key.startsWith("PACKET")) {
            String packet = snapshot.unitNames.get(normalize("Pkt"));
            if (!blank(packet)) return packet;
        }
        return null;
    }

    private TallyStockItem loadStockItemObject(String requestedName) throws Exception {
        String responseXml = post(buildStockItemObjectRequest(requestedName));
        Document document = parseXml(responseXml);
        NodeList nodes = document.getElementsByTagName("STOCKITEM");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element stock = (Element) nodes.item(i);
            String canonicalName = masterName(stock);
            String unit = trim(firstText(stock, "BASEUNITS"));
            if (!blank(canonicalName) && !blank(unit)) return new TallyStockItem(canonicalName, unit);
        }
        log.debug("Tally did not resolve stock item name/alias {}: {}", requestedName, responseXml);
        return null;
    }

    private String buildStockItemObjectRequest(String requestedName) {
        return "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST>"
                + "<TYPE>Object</TYPE><SUBTYPE>Stock Item</SUBTYPE><ID TYPE=\"Name\">"
                + escapeXml(requestedName) + "</ID></HEADER><BODY><DESC><STATICVARIABLES>"
                + "<SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>" + companyVariable()
                + "</STATICVARIABLES><FETCHLIST><FETCH>Name</FETCH><FETCH>Parent</FETCH>"
                + "<FETCH>BaseUnits</FETCH></FETCHLIST></DESC></BODY></ENVELOPE>";
    }

    private String buildLedgerMasterRequest(PurchaseOrder order) {
        String name = order.getParty().getPartyName().trim();
        String state = order.getParty().getParty_city() == null || order.getParty().getParty_city().getState() == null
                ? "" : trim(order.getParty().getParty_city().getState().getName());
        String gstin = trim(order.getParty().getGst());
        StringBuilder master = new StringBuilder()
                .append("<LEDGER NAME=\"").append(escapeXml(name)).append("\" ACTION=\"Create\">")
                .append("<NAME.LIST TYPE=\"String\">").append(tag("NAME", name)).append("</NAME.LIST>")
                .append(tag("PARENT", "Sundry Creditors"))
                .append(tag("ISBILLWISEON", "Yes"))
                .append(tag("AFFECTSSTOCK", "No"));
        if (!blank(order.getParty().getAddr1()) || !blank(order.getParty().getAddr2())) {
            master.append("<ADDRESS.LIST TYPE=\"String\">")
                    .append(blank(order.getParty().getAddr1()) ? "" : tag("ADDRESS", order.getParty().getAddr1()))
                    .append(blank(order.getParty().getAddr2()) ? "" : tag("ADDRESS", order.getParty().getAddr2()))
                    .append("</ADDRESS.LIST>");
        }
        if (!blank(state)) master.append(tag("LEDSTATENAME", state));
        if (!blank(order.getParty().getPin())) master.append(tag("PINCODE", order.getParty().getPin()));
        if (!blank(gstin)) master.append(tag("PARTYGSTIN", gstin));
        master.append(tag("GSTREGISTRATIONTYPE", blank(gstin) ? "Unregistered" : "Regular"))
                .append("</LEDGER>");
        return buildMasterImportRequest(master.toString());
    }

    private String buildStockItemMasterRequest(String name, String unit, String hsn) {
        StringBuilder master = new StringBuilder()
                .append("<STOCKITEM NAME=\"").append(escapeXml(name)).append("\" ACTION=\"Create\">")
                .append("<NAME.LIST TYPE=\"String\">").append(tag("NAME", name)).append("</NAME.LIST>")
                .append(tag("PARENT", defaultStockGroup))
                .append(tag("BASEUNITS", unit));
        if (!blank(hsn)) master.append(tag("HSNCODE", hsn));
        master.append("</STOCKITEM>");
        return buildMasterImportRequest(master.toString());
    }

    private String buildMasterImportRequest(String masterXml) {
        return "<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER><BODY><IMPORTDATA>"
                + "<REQUESTDESC><REPORTNAME>All Masters</REPORTNAME><STATICVARIABLES>" + companyVariable()
                + "</STATICVARIABLES></REQUESTDESC><REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">"
                + masterXml + "</TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>";
    }

    private PreparedOrder prepare(PurchaseOrder order, MasterSnapshot snapshot) {
        if (order.getParty() == null || blank(order.getParty().getPartyName())) {
            throw new IllegalArgumentException("Vendor is missing in ERP.");
        }
        String party = snapshot.ledgerNames.get(normalize(order.getParty().getPartyName()));
        if (party == null) {
            throw new IllegalArgumentException("Vendor ledger not found in Tally: " + order.getParty().getPartyName());
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("PO has no items.");
        }

        Date sourceDate = order.getCreated();
        if (sourceDate == null) {
            throw new IllegalArgumentException("PO date is missing in ERP.");
        }
        LocalDate orderDate = toLocalDate(sourceDate);
        List<PreparedItem> items = new ArrayList<>();
        Map<String, BigDecimal> taxLedgers = new LinkedHashMap<>();
        BigDecimal taxableTotal = BigDecimal.ZERO;
        boolean interState = Boolean.parseBoolean(trim(order.getParty().getInterState()));

        for (PurchaseItem item : order.getItems()) {
            if (item == null || blank(item.getModelNo())) {
                throw new IllegalArgumentException("An ERP PO item has no item-master reference.");
            }
            Optional<ItemMaster> itemMasterOptional = itemMasterService.getItemById(item.getModelNo());
            if (!itemMasterOptional.isPresent()) {
                throw new IllegalArgumentException("ERP item master not found: " + item.getModelNo());
            }
            ItemMaster itemMaster = itemMasterOptional.get();
            TallyStockItem tallyItem = firstStockItem(snapshot,
                    itemMaster.getModel(), itemMaster.getItemName(), item.getModelNo());
            if (tallyItem == null) {
                throw new IllegalArgumentException("Stock item not found in Tally: " + firstNonBlank(itemMaster.getModel(), itemMaster.getItemName()));
            }
            if (blank(tallyItem.unit)) {
                throw new IllegalArgumentException("Tally stock item has no base unit: " + tallyItem.name);
            }
            BigDecimal quantity = decimal(item.getQuantity());
            BigDecimal rate = decimal(item.getUnitPrice());
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for " + tallyItem.name + ".");
            }
            if (rate.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Rate cannot be negative for " + tallyItem.name + ".");
            }
            BigDecimal amount = decimal(item.getAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                amount = quantity.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            }
            int gstRate = itemMaster.getGst();
            String purchaseLedger = resolvePurchaseLedger(interState, gstRate, snapshot);
            String hsnCode = firstNonBlank(item.getHsnCode(), itemMaster.getHsnCode());
            LocalDate dueDate = item.getDelivaryDate() == null ? orderDate : toLocalDate(item.getDelivaryDate());
            items.add(new PreparedItem(tallyItem.name, tallyItem.unit, quantity, rate, amount,
                    dueDate, purchaseLedger, hsnCode, gstRate));
            taxableTotal = taxableTotal.add(amount);
            addTaxAllocations(taxLedgers, interState, gstRate, amount, snapshot);
        }
        validateFinancialValue(order.getPoNumber(), taxableTotal);
        BigDecimal gstTotal = BigDecimal.ZERO;
        for (BigDecimal taxAmount : taxLedgers.values()) gstTotal = gstTotal.add(taxAmount);
        return new PreparedOrder(order.getPoNumber().trim(), orderDate, party,
                taxableTotal.add(gstTotal), items, taxLedgers);
    }

    static void validateFinancialValue(String number, BigDecimal taxableTotal) {
        if (taxableTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("PO " + number + " has zero financial value in ERP. Finance must confirm item pricing or handle it as an internal stock transaction; nothing was sent to Tally.");
        }
    }

    private String resolvePurchaseLedger(boolean interState, int gstRate, MasterSnapshot snapshot) {
        List<String> candidates = new ArrayList<>();
        if (interState) {
            if (gstRate == 5) candidates.add("Inter State Purchase 5%");
            if (gstRate == 12) candidates.add("Interstate Purchase@12%");
            if (gstRate == 18) candidates.add("Inter-State-Purchase @ 18%");
            if (gstRate == 28) candidates.add("Interstate Purchase@28%");
            if (gstRate == 0) candidates.add("Inter State Purchase@0%");
            if (gstRate == 3) candidates.add("Inter State Purchase@3%");
        } else {
            if (gstRate == 0) candidates.add("State Purchase@0%");
            if (gstRate == 3) candidates.add("State Purchase @3%");
            if (gstRate == 5) candidates.add("State Purchase 5%");
            if (gstRate == 12) candidates.add("State Purchase 12%");
            if (gstRate == 18) candidates.add("State Purchase @ 18%");
            if (gstRate == 28) candidates.add("State Purchase @ 28%");
        }
        for (String candidate : candidates) {
            String ledger = snapshot.ledgerNames.get(normalize(candidate));
            if (ledger != null && snapshot.purchaseLedgerKeys.contains(normalize(ledger))) return ledger;
        }
        throw new IllegalArgumentException((interState ? "Interstate" : "State")
                + " purchase ledger for " + gstRate + "% GST was not found in Tally.");
    }

    private void addTaxAllocations(Map<String, BigDecimal> taxLedgers, boolean interState, int gstRate,
                                   BigDecimal taxableAmount, MasterSnapshot snapshot) {
        if (gstRate <= 0) return;
        if (interState) {
            String ledger = requireLedger(snapshot, igstLedgerCandidates(gstRate), "Input IGST " + gstRate + "%");
            mergeAmount(taxLedgers, ledger, percentage(taxableAmount, gstRate));
            return;
        }
        String rateText = halfRateText(gstRate);
        String cgst = requireLedger(snapshot, cgstLedgerCandidates(gstRate), "Input CGST " + rateText + "%");
        String sgst = requireLedger(snapshot, sgstLedgerCandidates(gstRate), "Input SGST " + rateText + "%");
        BigDecimal halfTax = taxableAmount.multiply(BigDecimal.valueOf(gstRate))
                .divide(BigDecimal.valueOf(200), 2, RoundingMode.HALF_UP);
        mergeAmount(taxLedgers, cgst, halfTax);
        mergeAmount(taxLedgers, sgst, halfTax);
    }

    private String requireLedger(MasterSnapshot snapshot, String[] candidates, String label) {
        for (String candidate : candidates) {
            String ledger = snapshot.ledgerNames.get(normalize(candidate));
            if (ledger != null) return ledger;
        }
        throw new IllegalArgumentException(label + " ledger was not found in Tally.");
    }

    private String[] igstLedgerCandidates(int rate) {
        return new String[] { "Input Tax IGST@" + rate + "%", "Input Tax IGST @ " + rate + "%",
                "Input Tax IGST@ " + rate + " %" };
    }

    private String[] cgstLedgerCandidates(int rate) {
        String half = halfRateText(rate);
        return new String[] { "Input Tax-CGST @ " + half + "%", "Input Tax -CGST @ " + half + " %",
                "Input Tax- CGST @ " + half + "%", "Input- CGST @ " + half + "%" };
    }

    private String[] sgstLedgerCandidates(int rate) {
        String half = halfRateText(rate);
        return new String[] { "Input Tax-SGST @ " + half + "%", "Input Tax -SGST @ " + half + " %",
                "Input Tax- SGST @ " + half + "%", "Input - SGST @ " + half + "%" };
    }

    private String halfRateText(int rate) {
        BigDecimal half = BigDecimal.valueOf(rate).divide(BigDecimal.valueOf(2));
        return half.stripTrailingZeros().toPlainString();
    }

    private BigDecimal percentage(BigDecimal amount, int rate) {
        return amount.multiply(BigDecimal.valueOf(rate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private void mergeAmount(Map<String, BigDecimal> values, String key, BigDecimal amount) {
        values.put(key, values.containsKey(key) ? values.get(key).add(amount) : amount);
    }

    private TallyStockItem firstStockItem(MasterSnapshot snapshot, String... candidates) {
        for (String candidate : candidates) {
            TallyStockItem item = snapshot.stockItems.get(normalize(candidate));
            if (item != null) return item;
        }
        return null;
    }

    private MasterSnapshot loadMasterSnapshot(boolean includeOrders) throws Exception {
        Document groupsDocument = parseXml(post(buildCollectionRequest("ErpTallyGroups", "Group", "Name,Parent")));
        Document ledgersDocument = parseXml(post(buildCollectionRequest("ErpTallyLedgers", "Ledger", "Name,Parent")));
        Document stockDocument = parseXml(includeOrders ? post(buildCollectionRequest("ErpTallyStockItems", "StockItem", "Name,BaseUnits")) : "<ENVELOPE/>");
        Document unitsDocument = parseXml(includeOrders ? post(buildCollectionRequest("ErpTallyUnits", "Unit", "Name")) : "<ENVELOPE/>");

        Map<String, String> groupParents = new HashMap<>();
        NodeList groups = groupsDocument.getElementsByTagName("GROUP");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            String name = firstNonBlank(group.getAttribute("NAME"), firstText(group, "NAME"));
            if (!blank(name)) groupParents.put(normalize(name), normalize(firstText(group, "PARENT")));
        }

        Map<String, String> ledgerNames = new LinkedHashMap<>();
        Set<String> purchaseLedgerKeys = new HashSet<>();
        NodeList ledgers = ledgersDocument.getElementsByTagName("LEDGER");
        for (int i = 0; i < ledgers.getLength(); i++) {
            Element ledger = (Element) ledgers.item(i);
            String name = masterName(ledger);
            String parent = firstText(ledger, "PARENT");
            if (blank(name)) continue;
            String key = normalize(name);
            ledgerNames.put(key, name);
            if (isUnderPurchaseAccounts(parent, groupParents)) purchaseLedgerKeys.add(key);
        }
        List<String> purchaseLedgers = new ArrayList<>();
        for (String key : purchaseLedgerKeys) purchaseLedgers.add(ledgerNames.get(key));
        Collections.sort(purchaseLedgers, String.CASE_INSENSITIVE_ORDER);

        Map<String, TallyStockItem> stockItems = new LinkedHashMap<>();
        NodeList stockNodes = stockDocument.getElementsByTagName("STOCKITEM");
        for (int i = 0; i < stockNodes.getLength(); i++) {
            Element stock = (Element) stockNodes.item(i);
            String name = masterName(stock);
            // Preserve Tally's exact stock-item name. Some legacy masters contain intentional
            // leading whitespace; trimming it makes Tally treat the otherwise matching item as missing.
            if (!blank(name)) stockItems.put(normalize(name), new TallyStockItem(name, trim(firstText(stock, "BASEUNITS"))));
        }

        Map<String, ExistingVoucher> purchaseOrders = new HashMap<>();
        if (includeOrders) {
            Document ordersDocument = parseXml(post(buildPurchaseOrdersRequest()));
            NodeList vouchers = ordersDocument.getElementsByTagName("VOUCHER");
            for (int i = 0; i < vouchers.getLength(); i++) {
                Element voucher = (Element) vouchers.item(i);
                String number = firstText(voucher, "VOUCHERNUMBER");
                if (!blank(number)) {
                    purchaseOrders.put(normalize(number), new ExistingVoucher(
                            trim(voucher.getAttribute("REMOTEID")), trim(voucher.getAttribute("VCHKEY")),
                            firstText(voucher, "GUID"), firstText(voucher, "MASTERID"),
                            firstText(voucher, "ALTERID"), firstNonBlank(firstText(voucher, "NARRATION"), ""),
                            LocalDate.parse(firstText(voucher, "DATE"), TALLY_DATE)));
                }
            }
        }

        Map<String, String> unitNames = new HashMap<>();
        NodeList unitNodes = unitsDocument.getElementsByTagName("UNIT");
        for (int i = 0; i < unitNodes.getLength(); i++) {
            Element unit = (Element) unitNodes.item(i);
            String name = firstNonBlank(unit.getAttribute("NAME"), firstText(unit, "NAME"));
            if (!blank(name)) unitNames.put(normalize(name), name.trim());
        }
        return new MasterSnapshot(ledgerNames, purchaseLedgerKeys, purchaseLedgers, stockItems, unitNames, purchaseOrders);
    }

    private boolean isUnderPurchaseAccounts(String parent, Map<String, String> groupParents) {
        String current = normalize(parent);
        Set<String> visited = new HashSet<>();
        while (!blank(current) && visited.add(current)) {
            if (PURCHASE_ACCOUNTS.equals(current)) return true;
            current = groupParents.get(current);
        }
        return false;
    }

    private String buildCollectionRequest(String collection, String type, String fetch) {
        return "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST>"
                + "<TYPE>Collection</TYPE><ID>" + collection + "</ID></HEADER><BODY><DESC><STATICVARIABLES>"
                + "<SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>" + companyVariable()
                + "</STATICVARIABLES><TDL><TDLMESSAGE><COLLECTION NAME=\"" + collection + "\" ISMODIFY=\"No\">"
                + "<TYPE>" + type + "</TYPE><FETCH>" + fetch + "</FETCH></COLLECTION>"
                + "</TDLMESSAGE></TDL></DESC></BODY></ENVELOPE>";
    }

    private String buildPurchaseOrdersRequest() {
        return "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST>"
                + "<TYPE>Collection</TYPE><ID>ErpExistingPurchaseOrders</ID></HEADER><BODY><DESC><STATICVARIABLES>"
                + "<SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT><SVFROMDATE TYPE=\"Date\">1-Jan-2020</SVFROMDATE><SVTODATE TYPE=\"Date\">" + LocalDate.now().plusYears(1).format(TALLY_IDENTIFIER_DATE) + "</SVTODATE>" + companyVariable()
                + "</STATICVARIABLES><TDL><TDLMESSAGE><COLLECTION NAME=\"ErpExistingPurchaseOrders\" ISMODIFY=\"No\">"
                + "<TYPE>Voucher</TYPE><FILTER>ErpIsPurchaseOrder</FILTER>"
                + "<FETCH>VoucherNumber,Date,Narration,GUID,MasterID,AlterID</FETCH></COLLECTION>"
                + "<SYSTEM TYPE=\"Formulae\" NAME=\"ErpIsPurchaseOrder\">$VoucherTypeName = \"Purchase Order\"</SYSTEM>"
                + "</TDLMESSAGE></TDL></DESC></BODY></ENVELOPE>";
    }

    String buildImportRequest(PreparedOrder order) {
        return buildImportRequest(order, null);
    }

    String buildImportRequest(PreparedOrder order, ExistingVoucher existing) {
        StringBuilder xml = new StringBuilder(4096);
        String action = existing == null ? "Create" : "Alter";
        if (existing == null) {
            xml.append("<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER><BODY><IMPORTDATA>")
                    .append("<REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME><STATICVARIABLES>")
                    .append(companyVariable()).append("</STATICVARIABLES></REQUESTDESC><REQUESTDATA>");
        } else {
            xml.append("<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Import</TALLYREQUEST>")
                    .append("<TYPE>Data</TYPE><ID>Vouchers</ID></HEADER><BODY><DESC></DESC><DATA>");
        }
        xml.append("<TALLYMESSAGE xmlns:UDF=\"TallyUDF\"><VOUCHER")
                .append(existing == null ? "" : " DATE=\"" + (existing.date == null ? order.date : existing.date).format(TALLY_IDENTIFIER_DATE)
                        + "\" TAGNAME=\"Voucher Number\" TAGVALUE=\"" + escapeXml(order.number) + "\"")
                .append(" VCHTYPE=\"Purchase Order\" ACTION=\"").append(action)
                .append("\" OBJVIEW=\"Invoice Voucher View\">")
                .append(tag("DATE", order.date.format(TALLY_DATE)))
                .append(tag("EFFECTIVEDATE", order.date.format(TALLY_DATE)))
                .append(tag("VCHSTATUSDATE", order.date.format(TALLY_DATE)))
                .append(existing == null || blank(existing.guid) ? "" : tag("GUID", existing.guid))
                .append(tag("VOUCHERTYPENAME", "Purchase Order"))
                .append(tag("VOUCHERNUMBER", order.number))
                .append(tag("REFERENCE", order.number))
                .append(tag("PARTYLEDGERNAME", order.party))
                .append(tag("PARTYNAME", order.party))
                .append(tag("PARTYMAILINGNAME", order.party))
                .append(tag("BASICBASEPARTYNAME", order.party))
                .append(tag("PERSISTEDVIEW", "Invoice Voucher View"))
                .append(tag("ISINVOICE", "No"))
                .append(tag("ASORIGINAL", "No"))
                .append(tag("ISCANCELLED", "No"))
                .append(tag("NARRATION", "Imported from ERP Purchase Orders with GST breakdown"));

        xml.append("<LEDGERENTRIES.LIST>")
                .append(tag("LEDGERNAME", order.party))
                .append(tag("ISDEEMEDPOSITIVE", "No"))
                .append(tag("ISLASTDEEMEDPOSITIVE", "No"))
                .append(tag("ISPARTYLEDGER", "Yes"))
                .append(tag("AMOUNT", money(order.total)))
                .append("</LEDGERENTRIES.LIST>");
        for (PreparedItem item : order.items) {
            String quantity = quantity(item.quantity) + " " + item.unit;
            String rate = money(item.rate) + "/" + item.unit;
            String negativeAmount = "-" + money(item.amount);
            xml.append("<ALLINVENTORYENTRIES.LIST>")
                    .append(tag("STOCKITEMNAME", item.name))
                    .append(tag("ISDEEMEDPOSITIVE", "Yes"))
                    .append(blank(item.hsnCode) ? "" : tag("GSTOVRDNHSNCODE", item.hsnCode))
                    .append(tag("RATE", rate))
                    .append(tag("AMOUNT", negativeAmount))
                    .append(tag("ACTUALQTY", quantity))
                    .append(tag("BILLEDQTY", quantity))
                    .append("<BATCHALLOCATIONS.LIST>")
                    .append(tag("BATCHNAME", "Primary Batch"))
                    .append(tag("ORDERNO", order.number))
                    .append(tag("ORDERDUEDATE", item.dueDate.format(TALLY_DUE_DATE)))
                    .append(tag("AMOUNT", negativeAmount))
                    .append(tag("ACTUALQTY", quantity))
                    .append(tag("BILLEDQTY", quantity))
                    .append("</BATCHALLOCATIONS.LIST>")
                    .append("<ACCOUNTINGALLOCATIONS.LIST>")
                    .append(tag("LEDGERNAME", item.purchaseLedger))
                    .append(tag("ISDEEMEDPOSITIVE", "Yes"))
                    .append(tag("AMOUNT", negativeAmount))
                    .append("</ACCOUNTINGALLOCATIONS.LIST></ALLINVENTORYENTRIES.LIST>");
        }

        for (Map.Entry<String, BigDecimal> tax : order.taxLedgers.entrySet()) {
            xml.append("<LEDGERENTRIES.LIST>")
                    .append(tag("LEDGERNAME", tax.getKey()))
                    .append(tag("ISPARTYLEDGER", "No"))
                    .append(tag("ISLASTDEEMEDPOSITIVE", "Yes"))
                    .append(tag("ISDEEMEDPOSITIVE", "Yes"))
                    .append(tag("AMOUNT", "-" + money(tax.getValue())))
                    .append("</LEDGERENTRIES.LIST>");
        }

        xml.append("</VOUCHER></TALLYMESSAGE>")
                .append(existing == null
                        ? "</REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>"
                        : "</DATA></BODY></ENVELOPE>");
        return xml.toString();
    }

    private String buildVerificationRequest(java.util.Collection<PreparedOrder> orders) {
        StringBuilder selection = new StringBuilder(" AND (");
        for (PreparedOrder order : orders) {
            if (selection.length() > 6) selection.append(" OR ");
            selection.append("$VoucherNumber = &quot;").append(escapeXml(order.number)).append("&quot;");
        }
        selection.append(")");
        return buildPurchaseOrdersRequest()
                .replace("ErpExistingPurchaseOrders", "ErpVerifiedPurchaseOrders")
                .replace("ErpIsPurchaseOrder", "ErpVerifyPurchaseOrder")
                .replace("VoucherNumber,Date,Narration,GUID,MasterID,AlterID",
                        "VoucherNumber,Date,PartyLedgerName,MasterID,AlterID,AllLedgerEntries.*")
                .replace("</SYSTEM>", selection + "</SYSTEM>");
    }

    private void verifySavedOrder(PreparedOrder order) throws Exception {
        String request = buildPurchaseOrdersRequest()
                .replace("VoucherNumber,Date,Narration,GUID,MasterID,AlterID",
                        "VoucherNumber,Date,PartyLedgerName,MasterID,AlterID,AllLedgerEntries.*")
                .replace("</SYSTEM>", " AND $VoucherNumber = &quot;"
                        + escapeXml(order.number) + "&quot;</SYSTEM>");
        // Select the exact number in Java so invoice references cannot broaden a match.
        Document document = parseXml(post(request));
        verifySavedOrder(order, document);
    }

    void verifySavedOrder(PreparedOrder order, Document document) {
        verifySavedOrder(order, findSavedOrder(order.number, document));
    }

    private Element findSavedOrder(String number, Document document) {
        Element match = null;
        NodeList vouchers = document.getElementsByTagName("VOUCHER");
        for (int i = 0; i < vouchers.getLength(); i++) {
            Element voucher = (Element) vouchers.item(i);
            if (!number.equals(firstText(voucher, "VOUCHERNUMBER"))) continue;
            if (match != null) throw new IllegalStateException("Multiple saved POs have this number; review required.");
            match = voucher;
        }
        if (match == null) throw new IllegalStateException("Tally accepted the request, but the saved PO could not be verified.");
        return match;
    }

    public Set<String> previouslyExportedNumbers() {
        java.util.Properties states = new java.util.Properties();
        java.nio.file.Path path = java.nio.file.Paths.get(syncStateFile);
        try (InputStream input = java.nio.file.Files.newInputStream(path)) { states.load(input); }
        catch (Exception error) { return Collections.emptySet(); }
        String prefix = serverUrl + "|" + companyName + "|";
        Set<String> numbers = new HashSet<>();
        for (String key : states.stringPropertyNames()) if (key.startsWith(prefix)) numbers.add(key.substring(prefix.length()));
        return numbers;
    }

    private String stateKey(String number) { return serverUrl + "|" + companyName + "|" + number; }

    private String verifiedState(PreparedOrder order, String alterId) throws Exception {
        byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(buildImportRequest(order).getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte value : digest) hash.append(String.format("%02x", value & 255));
        return hash + ":" + trim(alterId);
    }

    private void loadVerifiedStates() {
        java.nio.file.Path path = java.nio.file.Paths.get(syncStateFile);
        if (!java.nio.file.Files.exists(path)) return;
        try (InputStream input = java.nio.file.Files.newInputStream(path)) { verifiedStates.load(input); }
        catch (Exception error) { log.warn("Unable to read PO sync state; saved POs will be checked again", error); }
    }

    private void saveVerifiedStates() {
        java.nio.file.Path path = java.nio.file.Paths.get(syncStateFile).toAbsolutePath();
        try {
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Path temporary = java.nio.file.Files.createTempFile(path.getParent(), "po-sync-", ".tmp");
            try (OutputStream output = java.nio.file.Files.newOutputStream(temporary)) {
                verifiedStates.store(output, "Only POs verified after Tally accepted the export");
            }
            java.nio.file.Files.move(temporary, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception error) { log.warn("Unable to save PO sync state; the next run may recheck POs", error); }
    }

    void verifySavedOrder(PreparedOrder order, Element voucher) {
        NodeList ledgers = voucher.getElementsByTagName("ALLLEDGERENTRIES.LIST");
        if (ledgers.getLength() == 0) throw new IllegalStateException("Saved PO has no ledger entries.");
        Element party = (Element) ledgers.item(0);
        String savedParty = firstText(party, "LEDGERNAME");
        boolean matchingParty = normalize(order.party).equals(normalize(savedParty));
        if (!matchingParty && "Yes".equalsIgnoreCase(firstText(party, "ISPARTYLEDGER"))) {
            // Verify aliases against Tally itself; never guess vendor equivalence from spelling.
            try {
                String canonical = canonicalLedgerNames.get(order.party);
                if (canonical == null) {
                    String request = "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST>"
                            + "<TYPE>Object</TYPE><SUBTYPE>Ledger</SUBTYPE><ID TYPE=\"Name\">" + escapeXml(order.party)
                            + "</ID></HEADER><BODY><DESC><STATICVARIABLES>" + companyVariable()
                            + "</STATICVARIABLES><FETCHLIST><FETCH>*</FETCH></FETCHLIST></DESC></BODY></ENVELOPE>";
                    Document ledgerDocument = parseXml(post(request));
                    Element ledger = null;
                    NodeList ledgerObjects = ledgerDocument.getElementsByTagName("LEDGER");
                    for (int i = 0; i < ledgerObjects.getLength(); i++) {
                        Element candidate = (Element) ledgerObjects.item(i);
                        if (candidate.hasAttribute("NAME")) { ledger = candidate; break; }
                    }
                    if (ledger != null) {
                        canonical = firstNonBlank(ledger.getAttribute("NAME"), firstText(ledger, "NAME"));
                        String requestedGuid = firstText(ledger, "GUID");
                        String savedRequest = request.replace("<ID TYPE=\"Name\">" + escapeXml(order.party) + "</ID>",
                                "<ID TYPE=\"Name\">" + escapeXml(savedParty) + "</ID>");
                        Document savedLedger = parseXml(post(savedRequest));
                        String savedGuid = firstText(savedLedger.getDocumentElement(), "GUID");
                        if (!blank(requestedGuid) && requestedGuid.equals(savedGuid)) canonical = savedParty;
                        canonicalLedgerNames.put(order.party, canonical);
                    }
                }
                matchingParty = canonical != null && normalize(canonical).equals(normalize(savedParty));
                if (!matchingParty) log.warn("Vendor identity mismatch: requested={}, saved={}, canonical={}", order.party, savedParty, canonical);
            } catch (Exception error) { log.warn("Could not verify Tally vendor alias {}", order.party); }
        }
        if (!matchingParty || !"Yes".equalsIgnoreCase(firstText(party, "ISPARTYLEDGER"))) {
            throw new IllegalStateException("Tally accepted the PO, but the vendor is not the first party entry; layout needs review.");
        }
        if (new BigDecimal(firstText(party, "AMOUNT")).compareTo(order.total) != 0)
            throw new IllegalStateException("Saved PO total does not match ERP.");
        for (Map.Entry<String, BigDecimal> tax : order.taxLedgers.entrySet()) {
            BigDecimal saved = BigDecimal.ZERO;
            for (int j = 0; j < ledgers.getLength(); j++) {
                Element ledger = (Element) ledgers.item(j);
                if (normalize(tax.getKey()).equals(normalize(firstText(ledger, "LEDGERNAME"))))
                    saved = saved.add(new BigDecimal(firstText(ledger, "AMOUNT")));
            }
            if (saved.compareTo(tax.getValue().negate()) != 0)
                throw new IllegalStateException("Saved PO GST amount differs for " + tax.getKey() + ".");
        }
    }
    private ImportResponse parseImportResponse(String responseXml) throws Exception {
        Document document = parseXml(responseXml);
        return new ImportResponse(integerText(document, "CREATED"), integerText(document, "ALTERED"),
                integerText(document, "ERRORS"), integerText(document, "EXCEPTIONS"), integerText(document, "IGNORED"),
                firstText(document.getDocumentElement(), "LINEERROR"));
    }

    private int integerText(Document document, String tag) {
        try {
            String value = firstText(document.getDocumentElement(), tag);
            return blank(value) ? 0 : Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String post(String xml) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl).openConnection();
        connection.setConnectTimeout(4000);
        connection.setReadTimeout(60000);
        connection.setRequestMethod("POST");
        boolean unicodeResponse = !xml.contains("<TALLYREQUEST>Import");
        connection.setRequestProperty("Content-Type", unicodeResponse ? "UTF-16" : "text/xml; charset=utf-8");
        connection.setDoOutput(true);
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(xml.getBytes(unicodeResponse ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            if (status != 200) throw new IllegalStateException("Tally HTTP request failed with status " + status + ".");
            try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
                return decodeTallyResponse(output.toByteArray());
            }
        } finally {
            connection.disconnect();
        }
    }

    static String decodeTallyResponse(byte[] bytes) {
        java.nio.charset.Charset charset = StandardCharsets.UTF_8;
        if (bytes.length >= 2) {
            if ((bytes[0] & 255) == 254 && (bytes[1] & 255) == 255) charset = StandardCharsets.UTF_16BE;
            else if (((bytes[0] & 255) == 255 && (bytes[1] & 255) == 254) || bytes[1] == 0) charset = StandardCharsets.UTF_16LE;
        }
        String text = new String(bytes, charset);
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    private Document parseXml(String xml) throws Exception {
        String safeXml = xml == null ? "" : xml
                .replaceAll("(?i)&#x(?:0*[0-8BCEF]|0*1[0-9A-F]);", "")
                .replaceAll("&#0*(?:[0-8]|1[124-9]|2[0-9]|3[01]);", "")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        java.util.regex.Matcher attributes=java.util.regex.Pattern.compile("=\"([^\"]*)\"").matcher(safeXml);
        StringBuffer preserved=new StringBuffer();
        while(attributes.find()) {
            String value=attributes.group(1).replace("\r","&#13;").replace("\n","&#10;").replace("\t","&#9;");
            attributes.appendReplacement(preserved,java.util.regex.Matcher.quoteReplacement("=\""+value+"\""));
        }
        attributes.appendTail(preserved); safeXml=preserved.toString();
        java.util.regex.Matcher textNodes=java.util.regex.Pattern.compile(">([^<]*)<").matcher(safeXml);
        StringBuffer preservedText=new StringBuffer();
        while(textNodes.find()) textNodes.appendReplacement(preservedText,java.util.regex.Matcher.quoteReplacement(">"+textNodes.group(1).replace("\r","&#13;")+"<"));
        textNodes.appendTail(preservedText); safeXml=preservedText.toString();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(safeXml)));
    }

    private String companyVariable() {
        return blank(companyName) ? "" : tag("SVCURRENTCOMPANY", companyName);
    }

    static String masterName(Element master) {
        String attribute = master.getAttribute("NAME");
        if (attribute != null && !attribute.trim().isEmpty()) {
            NodeList declaredNames=master.getElementsByTagName("NAME");
            if (declaredNames.getLength()>0) {
                String declared=declaredNames.item(0).getTextContent();
                if (attribute.trim().replaceAll("\\s+", " ").equals(declared.trim().replaceAll("\\s+", " ")) && (declared.contains("\r") || declared.contains("\n") || declared.contains("\t"))) return declared;
            }
            return attribute;
        }
        NodeList names = master.getElementsByTagName("NAME");
        return names.getLength() == 0 ? "" : names.item(0).getTextContent();
    }

    private String tag(String name, String value) {
        return "<" + name + ">" + escapeXml(value) + "</" + name + ">";
    }

    private String escapeXml(String value) {
        String raw=value==null?"":value;
        StringBuilder encoded=new StringBuilder();
        raw.codePoints().forEach(point -> { encoded.appendCodePoint(point); });
        String safe=encoded.toString();
        String escaped = safe.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;").replace("\r", "&#13;").replace("\n", "&#10;").replace("\t", "&#9;");
        StringBuilder unicodeSafe=new StringBuilder();
        escaped.codePoints().forEach(point -> { if(point>127)unicodeSafe.append("&#").append(point).append(";");else unicodeSafe.append((char)point); });
        return unicodeSafe.toString();
    }

    private String firstText(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        Node node = nodes.item(0);
        return trim(node == null ? null : node.getTextContent());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (!blank(value)) return value.trim();
        return "";
    }

    private String normalize(String value) {
        return trim(value).replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private static String trim(String value) { return value == null ? "" : value.trim(); }
    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    private static BigDecimal decimal(double value) { return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP); }
    private static String money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
    private static String quantity(BigDecimal value) { return value.stripTrailingZeros().toPlainString(); }
    private static LocalDate toLocalDate(Date date) { return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }
    private static String safeMessage(Exception error) {
        return error == null || blank(error.getMessage()) ? "Unable to communicate with Tally." : error.getMessage();
    }

    private static final class MasterSnapshot {
        private final Map<String, String> ledgerNames;
        private final Set<String> purchaseLedgerKeys;
        private final List<String> purchaseLedgers;
        private final Map<String, TallyStockItem> stockItems;
        private final Map<String, String> unitNames;
        private final Map<String, ExistingVoucher> purchaseOrders;
        private MasterSnapshot(Map<String, String> ledgerNames, Set<String> purchaseLedgerKeys,
                               List<String> purchaseLedgers, Map<String, TallyStockItem> stockItems,
                               Map<String, String> unitNames, Map<String, ExistingVoucher> purchaseOrders) {
            this.ledgerNames = ledgerNames;
            this.purchaseLedgerKeys = purchaseLedgerKeys;
            this.purchaseLedgers = purchaseLedgers;
            this.stockItems = stockItems;
            this.unitNames = unitNames;
            this.purchaseOrders = purchaseOrders;
        }
    }

    static final class ExistingVoucher {
        private final String remoteId;
        private final String vchKey;
        private final String guid;
        private final String masterId;
        private final String alterId;
        private final String narration;
        private final LocalDate date;
        ExistingVoucher(String remoteId, String vchKey, String guid, String masterId,
                        String alterId, String narration) {
            this(remoteId, vchKey, guid, masterId, alterId, narration, null);
        }
        ExistingVoucher(String remoteId, String vchKey, String guid, String masterId,
                        String alterId, String narration, LocalDate date) {
            this.date = date;
            this.remoteId = remoteId; this.vchKey = vchKey; this.guid = guid;
            this.masterId = masterId; this.alterId = alterId; this.narration = trim(narration);
        }
    }

    private static final class TallyStockItem {
        private final String name;
        private final String unit;
        private TallyStockItem(String name, String unit) { this.name = name; this.unit = unit; }
    }

    static final class PreparedOrder {
        private final String number;
        private final LocalDate date;
        private final String party;
        private final BigDecimal total;
        private final List<PreparedItem> items;
        private final Map<String, BigDecimal> taxLedgers;
        PreparedOrder(String number, LocalDate date, String party, BigDecimal total,
                      List<PreparedItem> items, Map<String, BigDecimal> taxLedgers) {
            this.number = number; this.date = date; this.party = party;
            this.total = total; this.items = items; this.taxLedgers = taxLedgers;
        }
    }

    static final class PreparedItem {
        private final String name;
        private final String unit;
        private final BigDecimal quantity;
        private final BigDecimal rate;
        private final BigDecimal amount;
        private final LocalDate dueDate;
        private final String purchaseLedger;
        private final String hsnCode;
        private final int gstRate;
        PreparedItem(String name, String unit, BigDecimal quantity, BigDecimal rate,
                     BigDecimal amount, LocalDate dueDate, String purchaseLedger,
                     String hsnCode, int gstRate) {
            this.name = name; this.unit = unit; this.quantity = quantity; this.rate = rate;
            this.amount = amount; this.dueDate = dueDate; this.purchaseLedger = purchaseLedger;
            this.hsnCode = hsnCode; this.gstRate = gstRate;
        }
    }

    private static final class ImportResponse {
        private final int created;
        private final int altered;
        private final int errors;
        private final int exceptions;
        private final int ignored;
        private final String lineError;
        private ImportResponse(int created, int altered, int errors, int exceptions, int ignored, String lineError) {
            this.created = created; this.altered = altered; this.errors = errors;
            this.exceptions = exceptions; this.ignored = ignored; this.lineError = lineError;
        }
    }

    public static final class Options {
        private final String serverUrl;
        private final String company;
        private final List<String> purchaseLedgers;
        public Options(String serverUrl, String company, List<String> purchaseLedgers) {
            this.serverUrl = serverUrl; this.company = company;
            this.purchaseLedgers = Collections.unmodifiableList(new ArrayList<>(purchaseLedgers));
        }
        public String getServerUrl() { return serverUrl; }
        public String getCompany() { return company; }
        public List<String> getPurchaseLedgers() { return purchaseLedgers; }
    }

    public static final class ExportResult {
        private final int total;
        private final int imported;
        private final int skipped;
        private final int failed;
        private final List<ExportLine> records;
        public ExportResult(int total, int imported, int skipped, int failed, List<ExportLine> records) {
            this.total = total; this.imported = imported; this.skipped = skipped; this.failed = failed;
            this.records = Collections.unmodifiableList(new ArrayList<>(records));
        }
        public int getTotal() { return total; }
        public int getImported() { return imported; }
        public int getSkipped() { return skipped; }
        public int getFailed() { return failed; }
        public List<ExportLine> getRecords() { return records; }
    }

    public static final class ExportLine {
        private final String poNumber;
        private final String status;
        private final String message;
        public ExportLine(String poNumber, String status, String message) {
            this.poNumber = poNumber; this.status = status; this.message = message;
        }
        public String getPoNumber() { return poNumber; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
}









