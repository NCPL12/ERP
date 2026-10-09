package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.model.Invoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Read-only client for outstanding bill data exposed by TallyPrime's XML server. */
@Service
public class TallyLiveService {
    private static final DateTimeFormatter TALLY_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TALLY_FORMULA_DATE = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TALLY_REPORT_DATE = DateTimeFormatter.ofPattern("d-MMM-yy", Locale.ENGLISH);
    private static final Pattern CREDIT_PERIOD = Pattern.compile("(\\d+)\\s*(day|week|month|year)", Pattern.CASE_INSENSITIVE);


    @Value("${cashflow.tally.server.url:http://localhost:9000}")
    private String serverUrl;

    @Value("${cashflow.tally.company-name:}")
    private String companyName;

    public List<Invoice> fetchOutstandingBills() throws Exception {
        AdvanceLookup advanceLookup = fetchAdvanceLookup();
        String responseXml = post(buildOutstandingBillsRequest());
        Document document = parseXml(responseXml);
        String status = firstText(document.getDocumentElement(), "STATUS");
        if (status != null && !"1".equals(status.trim())) {
            throw new IllegalStateException("Tally returned unsuccessful status: " + status);
        }

        List<Invoice> invoices = new ArrayList<>();
        NodeList bills = document.getElementsByTagName("BILL");
        for (int i = 0; i < bills.getLength(); i++) {
            Element bill = (Element) bills.item(i);
            if ("Yes".equalsIgnoreCase(firstText(bill, "ISADVANCE"))) {
                continue;
            }

            double signedBalance = parseAmount(firstText(bill, "CLOSINGBALANCE"));
            if (Math.abs(signedBalance) < 0.005d) {
                continue;
            }

            String billName = firstNonBlank(firstText(bill, "NAME"), bill.getAttribute("NAME"));
            String partyName = firstText(bill, "PARENT");
            LocalDate billDate = parseDate(firstText(bill, "BILLDATE"));
            if (billName == null || partyName == null || billDate == null) {
                continue;
            }

            LedgerContext ledger = advanceLookup.ledgerByParty().get(normalize(partyName));
            if (ledger != null && classifyAdvance(
                    ledger.parentGroup(), signedBalance, advanceLookup.groupParents()) != null) {
                // Opposite-nature balances are advances, not outstanding invoices.
                continue;
            }

            LocalDate dueDate = calculateDueDate(billDate, firstText(bill, "BILLCREDITPERIOD"));
            Invoice.InvoiceType type = signedBalance < 0
                    ? Invoice.InvoiceType.INFLOW
                    : Invoice.InvoiceType.OUTFLOW;
            invoices.add(new Invoice(partyName, billName, billDate, dueDate,
                    Math.abs(signedBalance), type));
        }
        return invoices;
    }

    /** Detects opposite-nature pending balances, including excess payments not flagged ISADVANCE by Tally. */
    public AdvanceSnapshot fetchAdvanceBalances() throws Exception {
        AdvanceLookup advanceLookup = fetchAdvanceLookup();
        Document billDocument = parseXml(post(buildOutstandingBillsRequest()));

        Map<String, AdvanceAccumulator> advances = new LinkedHashMap<>();
        NodeList bills = billDocument.getElementsByTagName("BILL");
        for (int i = 0; i < bills.getLength(); i++) {
            Element bill = (Element) bills.item(i);
            String partyName = firstText(bill, "PARENT");
            LedgerContext ledger = advanceLookup.ledgerByParty().get(normalize(partyName));
            if (ledger == null) continue;
            double signedBalance = parseAmount(firstText(bill, "CLOSINGBALANCE"));
            AdvanceType type = classifyAdvance(
                    ledger.parentGroup(), signedBalance, advanceLookup.groupParents());
            if (type == null) continue;
            String key = type.name() + "|" + normalize(partyName);
            AdvanceAccumulator accumulator = advances.computeIfAbsent(key,
                    ignored -> new AdvanceAccumulator(ledger, type));
            accumulator.amount += Math.abs(signedBalance);
            String reference = firstNonBlank(firstText(bill, "NAME"), bill.getAttribute("NAME"));
            accumulator.references.add(new AdvanceReference(reference, parseDate(firstText(bill, "BILLDATE")),
                    Math.abs(signedBalance)));
        }

        List<AdvanceBalance> customerAdvances = advances.values().stream()
                .filter(item -> item.type == AdvanceType.CUSTOMER_ADVANCE_RECEIVED)
                .map(AdvanceAccumulator::toBalance).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        List<AdvanceBalance> supplierAdvances = advances.values().stream()
                .filter(item -> item.type == AdvanceType.SUPPLIER_ADVANCE_PAID)
                .map(AdvanceAccumulator::toBalance).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        Comparator<AdvanceBalance> byAmountDescending = Comparator.comparingDouble(AdvanceBalance::amount).reversed();
        customerAdvances.sort(byAmountDescending);
        supplierAdvances.sort(byAmountDescending);
        return new AdvanceSnapshot(customerAdvances, supplierAdvances,
                customerAdvances.stream().mapToDouble(AdvanceBalance::amount).sum(),
                supplierAdvances.stream().mapToDouble(AdvanceBalance::amount).sum());
    }

    private static final class LedgerContext {
        private final String partyName;
        private final String parentGroup;
        private final double signedClosingBalance;
        public LedgerContext(String partyName, String parentGroup, double signedClosingBalance) {
            this.partyName = partyName;
            this.parentGroup = parentGroup;
            this.signedClosingBalance = signedClosingBalance;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("partyName")
        public String partyName() { return partyName; }
        @com.fasterxml.jackson.annotation.JsonProperty("parentGroup")
        public String parentGroup() { return parentGroup; }
        @com.fasterxml.jackson.annotation.JsonProperty("signedClosingBalance")
        public double signedClosingBalance() { return signedClosingBalance; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof LedgerContext)) return false;
            LedgerContext that = (LedgerContext) other; return java.util.Objects.equals(partyName, that.partyName) && java.util.Objects.equals(parentGroup, that.parentGroup) && java.util.Objects.equals(signedClosingBalance, that.signedClosingBalance);
        }
        @Override public int hashCode() { return java.util.Objects.hash(partyName, parentGroup, signedClosingBalance); }
}
    private static final class AdvanceLookup {
        private final Map<String, String> groupParents;
        private final Map<String, LedgerContext> ledgerByParty;
        public AdvanceLookup(Map<String, String> groupParents,
                                 Map<String, LedgerContext> ledgerByParty) {
            this.groupParents = groupParents;
            this.ledgerByParty = ledgerByParty;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("groupParents")
        public Map<String, String> groupParents() { return groupParents; }
        @com.fasterxml.jackson.annotation.JsonProperty("ledgerByParty")
        public Map<String, LedgerContext> ledgerByParty() { return ledgerByParty; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof AdvanceLookup)) return false;
            AdvanceLookup that = (AdvanceLookup) other; return java.util.Objects.equals(groupParents, that.groupParents) && java.util.Objects.equals(ledgerByParty, that.ledgerByParty);
        }
        @Override public int hashCode() { return java.util.Objects.hash(groupParents, ledgerByParty); }
}

    private AdvanceLookup fetchAdvanceLookup() throws Exception {
        Document groupDocument = parseXml(post(buildGroupsRequest()));
        Document ledgerDocument = parseXml(post(buildLedgersRequest()));
        Map<String, String> groupParents = readGroupParents(groupDocument);
        Map<String, LedgerContext> ledgerByParty = new HashMap<>();
        NodeList ledgers = ledgerDocument.getElementsByTagName("LEDGER");
        for (int i = 0; i < ledgers.getLength(); i++) {
            Element ledger = (Element) ledgers.item(i);
            String partyName = firstNonBlank(ledger.getAttribute("NAME"), firstText(ledger, "NAME"));
            String parentGroup = firstText(ledger, "PARENT");
            double signedBalance = parseAmount(firstText(ledger, "CLOSINGBALANCE"));
            if (partyName != null) {
                ledgerByParty.put(normalize(partyName),
                        new LedgerContext(partyName, parentGroup, signedBalance));
            }
        }
        return new AdvanceLookup(groupParents, ledgerByParty);
    }

    private static final class AdvanceAccumulator {
        private final LedgerContext ledger;
        private final AdvanceType type;
        private final List<AdvanceReference> references = new ArrayList<>();
        private double amount;

        private AdvanceAccumulator(LedgerContext ledger, AdvanceType type) {
            this.ledger = ledger;
            this.type = type;
        }

        private AdvanceBalance toBalance() {
            references.sort(Comparator.comparing(AdvanceReference::billDate,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            return new AdvanceBalance(ledger.partyName(), ledger.parentGroup(), amount,
                    Math.abs(ledger.signedClosingBalance()), type,
                    "Opposite-nature pending balance under Sundry Debtors/Creditors",
                    references.size(), com.ncpl.sales.cashflow.util.Java8Collections.copyList(references));
        }
    }

    public AdvanceDetails fetchAdvanceDetails(String partyName, AdvanceType requestedType) throws Exception {
        if (partyName == null || partyName.codePoints().allMatch(Character::isWhitespace) || requestedType == null) {
            throw new IllegalArgumentException("Party name and advance type are required.");
        }
        AdvanceSnapshot snapshot = fetchAdvanceBalances();
        List<AdvanceBalance> candidates = requestedType == AdvanceType.CUSTOMER_ADVANCE_RECEIVED
                ? snapshot.customerAdvances() : snapshot.supplierAdvances();
        AdvanceBalance verifiedAdvance = candidates.stream()
                .filter(row -> normalize(row.partyName()).equals(normalize(partyName)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "The selected party is not present in the current Tally advance snapshot."));

        Document document = parseXml(post(buildAdvanceVoucherRequest(verifiedAdvance.partyName())));
        List<AdvanceTransaction> transactions = new ArrayList<>();
        NodeList vouchers = document.getElementsByTagName("VOUCHER");
        for (int i = 0; i < vouchers.getLength(); i++) {
            Element voucher = (Element) vouchers.item(i);
            String voucherType = firstText(voucher, "VOUCHERTYPENAME");
            LocalDate voucherDate = parseDate(firstText(voucher, "DATE"));
            if (voucherType == null && voucherDate == null) continue;

            List<BillAdjustment> adjustments = new ArrayList<>();
            NodeList billNodes = voucher.getElementsByTagName("BILLALLOCATIONS.LIST");
            for (int j = 0; j < billNodes.getLength(); j++) {
                Element bill = (Element) billNodes.item(j);
                adjustments.add(new BillAdjustment(firstText(bill, "NAME"), firstText(bill, "BILLTYPE"),
                        parseAmount(firstText(bill, "AMOUNT"))));
            }

            List<CostCentreAllocation> costCentres = new ArrayList<>();
            NodeList costNodes = voucher.getElementsByTagName("COSTCENTREALLOCATIONS.LIST");
            for (int j = 0; j < costNodes.getLength(); j++) {
                Element cost = (Element) costNodes.item(j);
                String name = firstText(cost, "NAME");
                if (name != null) costCentres.add(new CostCentreAllocation(name,
                        parseAmount(firstText(cost, "AMOUNT"))));
            }

            double amount = voucherPartyAmount(voucher, verifiedAdvance.partyName());
            transactions.add(new AdvanceTransaction(firstText(voucher, "MASTERID"), firstText(voucher, "GUID"),
                    voucherType, firstText(voucher, "VOUCHERNUMBER"), firstText(voucher, "REFERENCE"), voucherDate,
                    Math.abs(amount), firstText(voucher, "NARRATION"), adjustments, costCentres));
        }
        transactions.sort(Comparator.comparing(AdvanceTransaction::voucherDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return new AdvanceDetails(verifiedAdvance, transactions);
    }

    /** Loads the Cost Centre master hierarchy from the company currently open in Tally. */
    public CostCentreSnapshot fetchCostCentres() throws Exception {
        Document document = parseXml(post(buildSimpleCollectionRequest(
                "CashflowCostCentres", "CostCentre", "Name,Parent,Category")));
        Map<String, List<CostCentreNode>> childrenByParent = new LinkedHashMap<>();
        List<CostCentreNode> standalone = new ArrayList<>();
        NodeList nodes = document.getElementsByTagName("COSTCENTRE");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String name = firstNonBlank(element.getAttribute("NAME"), firstText(element, "NAME"));
            if (name == null) continue;
            String parent = cleanCostCentreParent(firstText(element, "PARENT"));
            CostCentreNode node = new CostCentreNode(name, parent, firstText(element, "CATEGORY"),
                    classifyCostCentre(name));
            if (parent == null) standalone.add(node);
            else childrenByParent.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(node);
        }

        List<CostCentreSite> sites = new ArrayList<>();
        childrenByParent.forEach((parent, children) -> {
            children.sort(Comparator.comparing(CostCentreNode::classification)
                    .thenComparing(CostCentreNode::name, String.CASE_INSENSITIVE_ORDER));
            sites.add(new CostCentreSite(parent, com.ncpl.sales.cashflow.util.Java8Collections.copyList(children)));
        });
        Set<String> representedParents = new HashSet<>();
        childrenByParent.keySet().forEach(parent -> representedParents.add(normalize(parent)));
        standalone.stream().filter(node -> !representedParents.contains(normalize(node.name())))
                .forEach(node -> sites.add(new CostCentreSite(node.name(), com.ncpl.sales.cashflow.util.Java8Collections.list(node))));
        sites.sort(Comparator.comparing(CostCentreSite::siteName, String.CASE_INSENSITIVE_ORDER));
        long allocationCentres = sites.stream().mapToLong(site -> site.costCentres().size()).sum();
        return new CostCentreSnapshot(com.ncpl.sales.cashflow.util.Java8Collections.copyList(sites), sites.size(), allocationCentres);
    }

    /** Loads vouchers in a bounded period and returns allocations for one exact Cost Centre. */
    public CostCentreTransactions fetchCostCentreTransactions(
            String costCentreName, LocalDate fromDate, LocalDate toDate) throws Exception {
        if (costCentreName == null || costCentreName.codePoints().allMatch(Character::isWhitespace) || fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Cost Centre, from date and to date are required.");
        }
        if (toDate.isBefore(fromDate)) throw new IllegalArgumentException("To date cannot be before from date.");
        if (fromDate.plusYears(2).isBefore(toDate)) {
            throw new IllegalArgumentException("Select a period of two years or less.");
        }

        List<CostCentreTransaction> transactions = new ArrayList<>();
        Document document = parseXml(post(buildAllCostCentreVoucherRequest()));
        collectCostCentreTransactions(document, costCentreName, fromDate, toDate, transactions);
        transactions.sort(Comparator.comparing(CostCentreTransaction::voucherDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        double total = transactions.stream().mapToDouble(CostCentreTransaction::allocatedAmount).sum();
        long vendors = transactions.stream().map(CostCentreTransaction::partyName)
                .filter(name -> !"Internal journal / No party specified".equals(name))
                .map(this::normalize).distinct().count();
        return new CostCentreTransactions(costCentreName, fromDate, toDate, total, vendors,
                com.ncpl.sales.cashflow.util.Java8Collections.copyList(transactions));
    }

    /** Builds a workbook-style site P&L from all leaf Cost Centres below a selected site. */
    public SiteProfitability fetchSiteProfitability(String siteName, LocalDate fromDate, LocalDate toDate) throws Exception {
        if (siteName == null || siteName.codePoints().allMatch(Character::isWhitespace) || fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Site, from date and to date are required.");
        }
        if (toDate.isBefore(fromDate)) throw new IllegalArgumentException("To date cannot be before from date.");
        CostCentreSnapshot snapshot = fetchCostCentres();
        Map<String, CostCentreSite> sitesByName = new HashMap<>();
        snapshot.sites().forEach(site -> sitesByName.put(normalize(site.siteName()), site));
        Map<String, String> leafClassifications = new LinkedHashMap<>();
        collectLeafCostCentres(siteName, sitesByName, leafClassifications, new HashSet<>());
        if (leafClassifications.isEmpty()) {
            leafClassifications.put(normalize(siteName), classifyCostCentre(siteName));
        }

        Map<String, FinancialNature> ledgerNatures = fetchLedgerFinancialNatures();
        List<SiteAllocation> detailedAllocations = new ArrayList<>();
        Document document = parseXml(post(buildAllCostCentreVoucherRequest()));
        collectSiteAllocations(document, leafClassifications, ledgerNatures, fromDate, toDate, detailedAllocations);
        Map<String, String> reportCentres = new LinkedHashMap<>();
        collectReportCostCentres(siteName, sitesByName, reportCentres, new HashSet<>());
        List<SiteAllocation> allocations = reconcileNativeWithDetailed(
                fetchNativeCostCentreVoucherRows(reportCentres, ledgerNatures, fromDate, toDate),
                detailedAllocations);
        NativeCostCentreSummary nativeSummary = fetchNativeCostCentreSummary(siteName, fromDate, toDate);
        NativePnlSummary nativePnl = fetchNativeCostCentrePnl(siteName, fromDate, toDate);
        allocations.sort(Comparator.comparing(SiteAllocation::voucherDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        double revenue = nativePnl.revenue();
        double expense = nativePnl.expense();
        return new SiteProfitability(siteName, fromDate, toDate, leafClassifications.size(),
                nativeSummary.debitTransactions(), nativeSummary.creditTransactions(), nativeSummary.closingBalance(),
                revenue, expense,
                revenue - expense, revenue == 0 ? 0 : (revenue - expense) / revenue,
                nativePnl.lines(), com.ncpl.sales.cashflow.util.Java8Collections.copyList(allocations));
    }

    private NativePnlSummary fetchNativeCostCentrePnl(String siteName,
                                                       LocalDate fromDate,
                                                       LocalDate toDate) throws Exception {
        Document document = parseXml(post(buildCostCentreBreakupRequest(siteName, fromDate, toDate)));
        return summarizeNativeCostCentrePnl(document);
    }

    NativePnlSummary summarizeNativeCostCentrePnl(Document document) {
        NodeList names = document.getElementsByTagName("DSPACCNAME");
        NodeList accountInfo = document.getElementsByTagName("DSPACCINFO");
        List<SitePnlLine> lines = new ArrayList<>();
        double revenue = 0d;
        double expense = 0d;
        int count = Math.min(names.getLength(), accountInfo.getLength());
        for (int i = 0; i < count; i++) {
            Element nameElement = (Element) names.item(i);
            Element info = (Element) accountInfo.item(i);
            String particular = firstText(nameElement, "DSPDISPNAME");
            if (particular == null || particular.codePoints().allMatch(Character::isWhitespace)) continue;
            double debit = Math.abs(parseAmount(firstText(info, "DSPDRAMTA")));
            double credit = Math.abs(parseAmount(firstText(info, "DSPCRAMTA")));
            double closing = parseAmount(firstText(info, "DSPCLAMTA"));
            lines.add(new SitePnlLine(particular, debit, credit, Math.abs(closing)));
            String normalized = normalize(particular);
            if (normalized.equals("SALES ACCOUNTS") || normalized.equals("DIRECT INCOMES")
                    || normalized.equals("INDIRECT INCOMES")) {
                revenue += Math.max(0d, credit - debit);
            } else if (normalized.equals("PURCHASE ACCOUNTS") || normalized.equals("DIRECT EXPENSES")
                    || normalized.equals("INDIRECT EXPENSES")) {
                expense += Math.max(0d, debit - credit);
            }
        }
        return new NativePnlSummary(revenue, expense, com.ncpl.sales.cashflow.util.Java8Collections.copyList(lines));
    }

    private NativeCostCentreSummary fetchNativeCostCentreSummary(String siteName,
                                                                  LocalDate fromDate,
                                                                  LocalDate toDate) throws Exception {
        Document document = parseXml(post(buildCostCentreSummaryRequest(siteName, fromDate, toDate)));
        return summarizeNativeCostCentre(document);
    }

    NativeCostCentreSummary summarizeNativeCostCentre(Document document) {
        NodeList accountInfo = document.getElementsByTagName("DSPACCINFO");
        double debit = 0d;
        double credit = 0d;
        double closing = 0d;
        for (int i = 0; i < accountInfo.getLength(); i++) {
            Element info = (Element) accountInfo.item(i);
            String debitText = firstText(info, "DSPDRAMTA");
            String creditText = firstText(info, "DSPCRAMTA");
            String closingText = firstText(info, "DSPCLAMTA");
            if (debitText != null && !debitText.codePoints().allMatch(Character::isWhitespace)) debit += Math.abs(parseAmount(debitText));
            if (creditText != null && !creditText.codePoints().allMatch(Character::isWhitespace)) credit += Math.abs(parseAmount(creditText));
            if (closingText != null && !closingText.codePoints().allMatch(Character::isWhitespace)) closing += parseAmount(closingText);
        }
        return new NativeCostCentreSummary(debit, credit, Math.abs(closing));
    }

    private void collectLeafCostCentres(String name, Map<String, CostCentreSite> sitesByName,
                                        Map<String, String> leaves, Set<String> visited) {
        String key = normalize(name);
        if (!visited.add(key)) return;
        CostCentreSite site = sitesByName.get(key);
        if (site == null || site.costCentres().isEmpty()) {
            leaves.put(key, classifyCostCentre(name));
            return;
        }
        for (CostCentreNode child : site.costCentres()) {
            CostCentreSite nested = sitesByName.get(normalize(child.name()));
            if (nested != null && nested.costCentres().stream()
                    .anyMatch(item -> !normalize(item.name()).equals(normalize(child.name())))) {
                collectLeafCostCentres(child.name(), sitesByName, leaves, visited);
            } else {
                leaves.put(normalize(child.name()), child.classification());
            }
        }
    }

    private void collectReportCostCentres(String name, Map<String, CostCentreSite> sitesByName,
                                          Map<String, String> centres, Set<String> visited) {
        String key = normalize(name);
        if (!visited.add(key)) return;
        centres.put(name, classifyCostCentre(name));
        CostCentreSite site = sitesByName.get(key);
        if (site == null) return;
        for (CostCentreNode child : site.costCentres()) {
            centres.put(child.name(), child.classification());
            if (sitesByName.containsKey(normalize(child.name()))) {
                collectReportCostCentres(child.name(), sitesByName, centres, visited);
            }
        }
    }

    private void collectSiteAllocations(Document document, Map<String, String> leafClassifications,
                                        Map<String, FinancialNature> ledgerNatures,
                                        LocalDate fromDate, LocalDate toDate,
                                        List<SiteAllocation> allocations) {
        NodeList vouchers = document.getElementsByTagName("VOUCHER");
        for (int i = 0; i < vouchers.getLength(); i++) {
            Element voucher = (Element) vouchers.item(i);
            LocalDate voucherDate = parseDate(firstText(voucher, "DATE"));
            if (voucherDate == null || voucherDate.isBefore(fromDate) || voucherDate.isAfter(toDate)) continue;
            if ("Yes".equalsIgnoreCase(firstText(voucher, "ISCANCELLED"))
                    || "Yes".equalsIgnoreCase(firstText(voucher, "ISOPTIONAL"))) continue;
            String voucherType = firstText(voucher, "VOUCHERTYPENAME");
            String party = firstNonBlank(firstText(voucher, "PARTYLEDGERNAME"), firstText(voucher, "PARTYNAME"));
            if (party == null) party = "Internal journal / No party specified";
            for (Element ledger : financialAllocationNodes(voucher)) {
                String ledgerName = firstText(ledger, "LEDGERNAME");
                FinancialNature financialNature = ledgerNatures.getOrDefault(normalize(ledgerName), FinancialNature.NON_REVENUE);
                if (financialNature == FinancialNature.NON_REVENUE) continue;
                NodeList costNodes = ledger.getElementsByTagName("COSTCENTREALLOCATIONS.LIST");
                for (int k = 0; k < costNodes.getLength(); k++) {
                    Element cost = (Element) costNodes.item(k);
                    String centre = firstText(cost, "NAME");
                    String classification = leafClassifications.get(normalize(centre));
                    if (classification == null) continue;
                    double signedAmount = parseAmount(firstText(cost, "AMOUNT"));
                    double reportAmount = toReportAmount(financialNature, signedAmount);
                    if (Math.abs(reportAmount) < 0.000001d) continue;
                    boolean revenue = financialNature == FinancialNature.REVENUE;
                    allocations.add(new SiteAllocation(voucherDate, voucherType,
                            firstText(voucher, "VOUCHERNUMBER"), firstText(voucher, "REFERENCE"), party,
                            ledgerName, centre, revenue ? "REVENUE" : "EXPENSE",
                            revenue ? "REVENUE" : expandCostClassification(classification, centre, ledgerName),
                            reportAmount, firstText(voucher, "NARRATION")));
                }
            }
        }
    }

    private List<Element> financialAllocationNodes(Element voucher) {
        List<Element> result = new ArrayList<>();
        NodeList ledgerNodes = voucher.getElementsByTagName("ALLLEDGERENTRIES.LIST");
        if (ledgerNodes.getLength() == 0) ledgerNodes = voucher.getElementsByTagName("LEDGERENTRIES.LIST");
        for (int i = 0; i < ledgerNodes.getLength(); i++) result.add((Element) ledgerNodes.item(i));
        NodeList inventoryAccounting = voucher.getElementsByTagName("ACCOUNTINGALLOCATIONS.LIST");
        for (int i = 0; i < inventoryAccounting.getLength(); i++) {
            result.add((Element) inventoryAccounting.item(i));
        }
        return result;
    }

    private List<SiteAllocation> fetchNativeCostCentreVoucherRows(Map<String, String> leafClassifications,
                                                                   Map<String, FinancialNature> ledgerNatures,
                                                                   LocalDate fromDate, LocalDate toDate) throws Exception {
        List<SiteAllocation> rows = new ArrayList<>();
        for (Map.Entry<String, String> leaf : leafClassifications.entrySet()) {
            String centreName = leaf.getKey();
            Document document = parseXml(post(buildNativeCostCentreReportRequest(
                    "CC Vouchers", centreName, fromDate, toDate)));
            collectNativeCostCentreVoucherRows(document, centreName, leaf.getValue(), ledgerNatures, rows);
        }
        return rows;
    }

    void collectNativeCostCentreVoucherRows(Document document, String centreName, String classification,
                                             Map<String, FinancialNature> ledgerNatures,
                                             List<SiteAllocation> rows) {
        NodeList dates = document.getElementsByTagName("DSPVCHDATE");
        NodeList ledgers = document.getElementsByTagName("DSPVCHLEDACCOUNT");
        NodeList types = document.getElementsByTagName("DSPVCHTYPE");
        NodeList debits = document.getElementsByTagName("DSPVCHDRAMT");
        NodeList credits = document.getElementsByTagName("DSPVCHCRAMT");
        int count = Math.min(dates.getLength(), Math.min(ledgers.getLength(),
                Math.min(debits.getLength(), credits.getLength())));
        for (int i = 0; i < count; i++) {
            LocalDate date = parseReportDate(dates.item(i).getTextContent());
            String ledgerName = ledgers.item(i).getTextContent().trim();
            FinancialNature nature = ledgerNatures.getOrDefault(normalize(ledgerName), FinancialNature.NON_REVENUE);
            if (date == null || nature == FinancialNature.NON_REVENUE) continue;
            double debit = Math.abs(parseAmount(debits.item(i).getTextContent()));
            double credit = Math.abs(parseAmount(credits.item(i).getTextContent()));
            double signedAmount = credit - debit;
            double reportAmount = toReportAmount(nature, signedAmount);
            if (Math.abs(reportAmount) < 0.000001d) continue;
            boolean revenue = nature == FinancialNature.REVENUE;
            String voucherType = i < types.getLength() ? types.item(i).getTextContent().trim() : "";
            rows.add(new SiteAllocation(date, voucherType, "", "",
                    "Open in Tally for party/vendor", ledgerName, centreName,
                    revenue ? "REVENUE" : "EXPENSE",
                    revenue ? "REVENUE" : expandCostClassification(classification, centreName, ledgerName),
                    reportAmount, "Native Tally Cost Centre voucher"));
        }
    }

    private List<SiteAllocation> reconcileNativeWithDetailed(List<SiteAllocation> nativeRows,
                                                              List<SiteAllocation> detailed) {
        Map<String, java.util.ArrayDeque<SiteAllocation>> detailedByKey = new HashMap<>();
        for (SiteAllocation row : detailed) {
            detailedByKey.computeIfAbsent(allocationMatchKey(row), ignored -> new java.util.ArrayDeque<>()).add(row);
        }
        List<SiteAllocation> reconciled = new ArrayList<>(nativeRows.size());
        for (SiteAllocation row : nativeRows) {
            java.util.ArrayDeque<SiteAllocation> matches = detailedByKey.get(allocationMatchKey(row));
            reconciled.add(matches == null || matches.isEmpty() ? row : matches.removeFirst());
        }
        return reconciled;
    }

    private String allocationMatchKey(SiteAllocation row) {
        return row.voucherDate() + "|" + normalize(row.ledgerName()) + "|"
                + normalize(row.costCentreName()) + "|" + Math.round(row.amount() * 100d);
    }

    private Map<String, FinancialNature> fetchLedgerFinancialNatures() throws Exception {
        Document groupsDocument = parseXml(post(buildSimpleCollectionRequest(
                "CashflowFinancialGroups", "Group", "Name,Parent,ReservedName")));
        Map<String, String> groupParents = new HashMap<>();
        Map<String, String> groupNames = new HashMap<>();
        NodeList groups = groupsDocument.getElementsByTagName("GROUP");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            String name = firstNonBlank(group.getAttribute("NAME"), firstText(group, "NAME"));
            if (name == null) continue;
            String key = normalize(name);
            groupNames.put(key, name);
            groupParents.put(key, normalize(firstText(group, "PARENT")));
        }

        Document ledgersDocument = parseXml(post(buildSimpleCollectionRequest(
                "CashflowFinancialLedgers", "Ledger", "Name,Parent")));
        Map<String, FinancialNature> result = new HashMap<>();
        NodeList ledgers = ledgersDocument.getElementsByTagName("LEDGER");
        for (int i = 0; i < ledgers.getLength(); i++) {
            Element ledger = (Element) ledgers.item(i);
            String name = firstNonBlank(ledger.getAttribute("NAME"), firstText(ledger, "NAME"));
            if (name == null) continue;
            result.put(normalize(name), resolveFinancialNature(firstText(ledger, "PARENT"), groupParents, groupNames));
        }
        return result;
    }

    FinancialNature resolveFinancialNature(String parent, Map<String, String> groupParents,
                                            Map<String, String> groupNames) {
        String group = normalize(parent);
        Set<String> visited = new HashSet<>();
        while (!group.codePoints().allMatch(Character::isWhitespace) && visited.add(group)) {
            String name = normalize(groupNames.getOrDefault(group, group));
            if (name.equals("SALES ACCOUNTS") || name.equals("DIRECT INCOMES") || name.equals("INDIRECT INCOMES")) {
                return FinancialNature.REVENUE;
            }
            if (name.equals("PURCHASE ACCOUNTS") || name.equals("DIRECT EXPENSES") || name.equals("INDIRECT EXPENSES")) {
                return FinancialNature.EXPENSE;
            }
            group = groupParents.getOrDefault(group, "");
        }
        return FinancialNature.NON_REVENUE;
    }

    static double toReportAmount(FinancialNature nature, double signedAmount) {
        if (nature == FinancialNature.REVENUE) return signedAmount;
        if (nature == FinancialNature.EXPENSE) return -signedAmount;
        return 0d;
    }

    private String expandCostClassification(String classification, String centre, String ledger) {
        String combined = normalize(centre) + " " + normalize(ledger);
        if (combined.contains("BACKEND") && combined.contains("SALARY")) return "BACKEND_SALARY";
        if (combined.contains("SALARY") || combined.contains("WAGES")) return "STAFF_SALARY";
        if (combined.contains("RENT") || combined.contains("SCAFF") || combined.contains("PG ")) return "RENTAL";
        if (combined.contains("CONTRACT") && combined.contains("LABOUR")) return "CONTRACT_LABOUR";
        if (combined.contains("REPAIR") || combined.contains("MAINTENANCE")) return "REPAIRS_MAINTENANCE";
        if (combined.contains("INSURANCE")) return "INSURANCE";
        return classification;
    }

    private void collectCostCentreTransactions(Document document, String costCentreName,
                                               LocalDate fromDate, LocalDate toDate,
                                               List<CostCentreTransaction> transactions) {
        NodeList vouchers = document.getElementsByTagName("VOUCHER");
        for (int i = 0; i < vouchers.getLength(); i++) {
            Element voucher = (Element) vouchers.item(i);
            LocalDate voucherDate = parseDate(firstText(voucher, "DATE"));
            if (voucherDate == null || voucherDate.isBefore(fromDate) || voucherDate.isAfter(toDate)) continue;
            if ("Yes".equalsIgnoreCase(firstText(voucher, "ISCANCELLED"))
                    || "Yes".equalsIgnoreCase(firstText(voucher, "ISOPTIONAL"))) continue;
            String party = firstNonBlank(firstText(voucher, "PARTYLEDGERNAME"), firstText(voucher, "PARTYNAME"));
            if (party == null) party = "Internal journal / No party specified";
            for (Element ledger : financialAllocationNodes(voucher)) {
                String ledgerName = firstText(ledger, "LEDGERNAME");
                NodeList allocations = ledger.getElementsByTagName("COSTCENTREALLOCATIONS.LIST");
                for (int k = 0; k < allocations.getLength(); k++) {
                    Element allocation = (Element) allocations.item(k);
                    String allocatedCentre = firstText(allocation, "NAME");
                    if (!normalize(allocatedCentre).equals(normalize(costCentreName))) continue;
                    transactions.add(new CostCentreTransaction(
                            firstText(voucher, "MASTERID"), firstText(voucher, "GUID"),
                            voucherDate, firstText(voucher, "VOUCHERTYPENAME"),
                            firstText(voucher, "VOUCHERNUMBER"), firstText(voucher, "REFERENCE"), party,
                            ledgerName, allocatedCentre, Math.abs(parseAmount(firstText(allocation, "AMOUNT"))),
                            firstText(voucher, "NARRATION")));
                }
            }
        }
    }

    private String cleanCostCentreParent(String parent) {
        if (parent == null) return null;
        String cleaned = parent.replaceAll("[\\x00-\\x1F]", "").trim();
        return cleaned.codePoints().allMatch(Character::isWhitespace) || "PRIMARY".equalsIgnoreCase(cleaned) ? null : cleaned;
    }

    private String classifyCostCentre(String name) {
        String value = normalize(name).replace('-', ' ');
        if (value.matches(".*\\bNON\\s*BILLABLE\\b.*") || value.matches(".*\\bNB\\b.*")) return "NON_BILLABLE";
        if (value.matches(".*\\bBILLABLE\\b.*")) return "BILLABLE";
        if (value.contains("TRANSPORT")) return "TRANSPORTATION";
        if (value.contains("MATERIAL")) return "MATERIAL";
        if (value.contains("SITE") && value.contains("EXPENSE")) return "SITE_EXPENSES";
        return "OTHER";
    }

    private double voucherPartyAmount(Element voucher, String partyName) {
        NodeList ledgerNodes = voucher.getElementsByTagName("ALLLEDGERENTRIES.LIST");
        if (ledgerNodes.getLength() == 0) ledgerNodes = voucher.getElementsByTagName("LEDGERENTRIES.LIST");
        for (int i = 0; i < ledgerNodes.getLength(); i++) {
            Element ledger = (Element) ledgerNodes.item(i);
            if (normalize(firstText(ledger, "LEDGERNAME")).equals(normalize(partyName))) {
                return parseAmount(firstText(ledger, "AMOUNT"));
            }
        }
        return 0d;
    }

    static AdvanceType classifyAdvance(String parentGroup, double signedBalance, Map<String, String> groupParents) {
        if (signedBalance > 0.005d && isUnderGroup(parentGroup, "Sundry Debtors", groupParents)) {
            return AdvanceType.CUSTOMER_ADVANCE_RECEIVED;
        }
        if (signedBalance < -0.005d && isUnderGroup(parentGroup, "Sundry Creditors", groupParents)) {
            return AdvanceType.SUPPLIER_ADVANCE_PAID;
        }
        return null;
    }

    private static boolean isUnderGroup(String group, String root, Map<String, String> groupParents) {
        String current = normalizeGroup(group);
        String wantedRoot = normalizeGroup(root);
        Set<String> visited = new HashSet<>();
        while (!current.codePoints().allMatch(Character::isWhitespace) && visited.add(current)) {
            if (current.equals(wantedRoot)) return true;
            current = groupParents.getOrDefault(current, "");
        }
        return false;
    }

    private Map<String, String> readGroupParents(Document document) {
        Map<String, String> result = new HashMap<>();
        NodeList groups = document.getElementsByTagName("GROUP");
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            String name = firstNonBlank(group.getAttribute("NAME"), firstText(group, "NAME"));
            if (name != null) result.put(normalizeGroup(name), normalizeGroup(firstText(group, "PARENT")));
        }
        return result;
    }

    private static String normalizeGroup(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    /**
     * Loads a voucher read-only from Tally. Outstanding bill names are not guaranteed
     * to equal voucher numbers. Candidates are restricted to a small window around
     * the stored bill date and matched in Java against voucher number, reference,
     * nested bill allocations and party. The window tolerates the one-day shift that
     * older JDBC date mappings can introduce while persisting a Tally date.
     */
    public List<PurchaseVoucherMatch> fetchPurchaseVouchers(LocalDate fromDate, LocalDate toDate) throws Exception {
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("A valid Purchase voucher date range is required.");
        }
        Document document = parseXml(post(buildPurchaseVoucherRequest(fromDate, toDate)));
        String status = firstText(document.getDocumentElement(), "STATUS");
        if (status != null && !"1".equals(status.trim())) {
            throw new IllegalStateException("Tally returned unsuccessful status: " + status);
        }
        List<PurchaseVoucherMatch> result = new ArrayList<>();
        NodeList vouchers = document.getElementsByTagName("VOUCHER");
        for (int i = 0; i < vouchers.getLength(); i++) {
            Element voucher = (Element) vouchers.item(i);
            List<String> billReferences = new ArrayList<>();
            NodeList allocations = voucher.getElementsByTagName("BILLALLOCATIONS.LIST");
            for (int j = 0; j < allocations.getLength(); j++) {
                String name = firstText((Element) allocations.item(j), "NAME");
                if (name != null && !name.trim().isEmpty() && !billReferences.contains(name.trim())) {
                    billReferences.add(name.trim());
                }
            }
            double positive = 0d;
            double negative = 0d;
            NodeList ledgers = voucher.getElementsByTagName("ALLLEDGERENTRIES.LIST");
            if (ledgers.getLength() == 0) ledgers = voucher.getElementsByTagName("LEDGERENTRIES.LIST");
            for (int j = 0; j < ledgers.getLength(); j++) {
                double amount = parseAmount(firstText((Element) ledgers.item(j), "AMOUNT"));
                if (amount >= 0) positive += amount; else negative += Math.abs(amount);
            }
            result.add(new PurchaseVoucherMatch(
                    firstText(voucher, "MASTERID"),
                    firstText(voucher, "VOUCHERNUMBER"),
                    firstText(voucher, "REFERENCE"),
                    parseDate(firstText(voucher, "DATE")),
                    firstNonBlank(firstText(voucher, "PARTYLEDGERNAME"), firstText(voucher, "PARTYNAME")),
                    firstText(voucher, "NARRATION"),
                    Math.max(positive, negative),
                    billReferences));
        }
        return result;
    }

    /** Sales invoices are read across the company, because billing may follow the GRN period. */
    public List<SalesVoucherLink> fetchSalesVoucherLinks() throws Exception {
        String request = buildSimpleCollectionRequest("CashflowSalesLinks", "Voucher",
                "MasterID,Date,VoucherTypeName,VoucherNumber,Reference,PartyLedgerName,Narration,IsCancelled,IsOptional,AllInventoryEntries.OrderNo");
        request = request.replace("<TYPE>Voucher</TYPE>",
                "<TYPE>Voucher</TYPE><FILTER>CashflowSalesLinkFilter</FILTER>");
        request = request.replace("</TDLMESSAGE>",
                "<SYSTEM TYPE=\"Formulae\" NAME=\"CashflowSalesLinkFilter\">$$IsSales:$VoucherTypeName</SYSTEM></TDLMESSAGE>");
        Document document = parseXml(post(request));
        String status = firstText(document.getDocumentElement(), "STATUS");
        if (status != null && !"1".equals(status.trim())) {
            throw new IllegalStateException("Tally Sales lookup returned unsuccessful status: " + status);
        }
        if (document.getElementsByTagName("LINEERROR").getLength() > 0) {
            throw new IllegalStateException("Tally Sales lookup returned an error.");
        }
        List<SalesVoucherLink> result = new ArrayList<>();
        NodeList vouchers = document.getElementsByTagName("VOUCHER");
        for (int i = 0; i < vouchers.getLength(); i++) {
            Element voucher = (Element) vouchers.item(i);
            if ("Yes".equalsIgnoreCase(firstText(voucher, "ISCANCELLED"))
                    || "Yes".equalsIgnoreCase(firstText(voucher, "ISOPTIONAL"))) continue;
            List<String> references = new ArrayList<>();
            for (String tag : new String[]{"REFERENCE", "NARRATION", "ORDERNO"}) {
                NodeList values = voucher.getElementsByTagName(tag);
                for (int j = 0; j < values.getLength(); j++) {
                    String value = values.item(j).getTextContent();
                    if (value != null && !value.trim().isEmpty()) references.add(value.trim());
                }
            }
            String number = firstText(voucher, "VOUCHERNUMBER");
            if (number == null || number.trim().isEmpty()) continue;
            result.add(new SalesVoucherLink(firstText(voucher, "MASTERID"), number,
                    parseDate(firstText(voucher, "DATE")), firstText(voucher, "PARTYLEDGERNAME"), references));
        }
        return result;
    }

    public static final class SalesVoucherLink {
        public final String masterId;
        public final String voucherNumber;
        public final LocalDate voucherDate;
        public final String partyName;
        public final List<String> references;
        public SalesVoucherLink(String masterId, String voucherNumber, LocalDate voucherDate,
                                String partyName, List<String> references) {
            this.masterId = masterId;
            this.voucherNumber = voucherNumber;
            this.voucherDate = voucherDate;
            this.partyName = partyName;
            this.references = references;
        }
    }

    public VoucherDetails fetchVoucherDetails(String billNumber, String partyName, LocalDate billDate) throws Exception {
        if (billNumber == null || billNumber.codePoints().allMatch(Character::isWhitespace) || billDate == null) {
            return VoucherDetails.notFound("Bill number and bill date are required for a safe Tally lookup.");
        }

        Document document = parseXml(post(buildVoucherDetailsRequest(
                billDate.minusDays(2), billDate.plusDays(2))));
        String status = firstText(document.getDocumentElement(), "STATUS");
        if (status != null && !"1".equals(status.trim())) {
            throw new IllegalStateException("Tally returned unsuccessful status: " + status);
        }

        Element best = null;
        int bestScore = 0;
        int bestMatchCount = 0;
        NodeList vouchers = document.getElementsByTagName("VOUCHER");
        for (int i = 0; i < vouchers.getLength(); i++) {
            Element voucher = (Element) vouchers.item(i);
            int score = voucherMatchScore(voucher, billNumber, partyName, billDate);
            if (score > bestScore) {
                best = voucher;
                bestScore = score;
                bestMatchCount = 1;
            } else if (score == bestScore && score > 0) {
                bestMatchCount++;
            }
        }

        if (best == null || bestScore < 5) {
            return VoucherDetails.notFound("No matching voucher was found in the open Tally company.");
        }
        if (bestMatchCount > 1) {
            return VoucherDetails.notFound(
                    "Multiple Tally vouchers match this reference and date. Open the voucher in Tally to avoid showing the wrong finance transaction.");
        }
        return parseVoucherDetails(best, bestScore >= 10 ? "Exact bill and party match" : "Bill/reference match");
    }

    private int voucherMatchScore(Element voucher, String billNumber, String partyName, LocalDate billDate) {
        String wantedBill = normalize(billNumber);
        String wantedParty = normalize(partyName);
        int score = 0;
        LocalDate voucherDate = parseDate(firstText(voucher, "DATE"));
        if (billDate.equals(voucherDate)) score += 2;
        if (equalsNormalized(firstText(voucher, "VOUCHERNUMBER"), wantedBill)) score += 6;
        if (equalsNormalized(firstText(voucher, "REFERENCE"), wantedBill)) score += 6;
        if (containsBillAllocation(voucher, wantedBill)) score += 8;
        String voucherParty = firstNonBlank(firstText(voucher, "PARTYLEDGERNAME"), firstText(voucher, "PARTYNAME"));
        if (!wantedParty.codePoints().allMatch(Character::isWhitespace) && equalsNormalized(voucherParty, wantedParty)) score += 4;
        return score;
    }

    private boolean containsBillAllocation(Element voucher, String wantedBill) {
        NodeList allocations = voucher.getElementsByTagName("BILLALLOCATIONS.LIST");
        for (int i = 0; i < allocations.getLength(); i++) {
            if (equalsNormalized(firstText((Element) allocations.item(i), "NAME"), wantedBill)) return true;
        }
        return false;
    }

    private VoucherDetails parseVoucherDetails(Element voucher, String matchedBy) {
        List<LedgerLine> ledgers = new ArrayList<>();
        List<BillAdjustment> adjustments = new ArrayList<>();
        double positive = 0d;
        double negative = 0d;
        NodeList ledgerNodes = voucher.getElementsByTagName("ALLLEDGERENTRIES.LIST");
        if (ledgerNodes.getLength() == 0) ledgerNodes = voucher.getElementsByTagName("LEDGERENTRIES.LIST");
        for (int i = 0; i < ledgerNodes.getLength(); i++) {
            Element ledger = (Element) ledgerNodes.item(i);
            String name = firstText(ledger, "LEDGERNAME");
            double amount = parseAmount(firstText(ledger, "AMOUNT"));
            if (name != null) {
                boolean tax = isTaxLedger(name);
                ledgers.add(new LedgerLine(name, amount, tax));
                if (amount >= 0) positive += amount; else negative += Math.abs(amount);
            }
            NodeList billNodes = ledger.getElementsByTagName("BILLALLOCATIONS.LIST");
            for (int j = 0; j < billNodes.getLength(); j++) {
                Element bill = (Element) billNodes.item(j);
                adjustments.add(new BillAdjustment(
                        firstText(bill, "NAME"), firstText(bill, "BILLTYPE"),
                        parseAmount(firstText(bill, "AMOUNT"))));
            }
        }

        List<InventoryLine> inventory = new ArrayList<>();
        NodeList inventoryNodes = voucher.getElementsByTagName("ALLINVENTORYENTRIES.LIST");
        if (inventoryNodes.getLength() == 0) inventoryNodes = voucher.getElementsByTagName("INVENTORYENTRIES.LIST");
        for (int i = 0; i < inventoryNodes.getLength(); i++) {
            Element item = (Element) inventoryNodes.item(i);
            String name = firstText(item, "STOCKITEMNAME");
            if (name != null) {
                inventory.add(new InventoryLine(name, firstText(item, "BILLEDQTY"),
                        firstText(item, "RATE"), parseAmount(firstText(item, "AMOUNT"))));
            }
        }

        double originalValue = Math.max(positive, negative);
        Map<String, Double> taxSummary = new LinkedHashMap<>();
        ledgers.stream().filter(LedgerLine::tax).forEach(line ->
                taxSummary.merge(line.name(), Math.abs(line.amount()), Double::sum));

        return new VoucherDetails(true, "Voucher details loaded from Tally.", matchedBy,
                firstText(voucher, "MASTERID"), firstText(voucher, "GUID"),
                firstText(voucher, "VOUCHERTYPENAME"), firstText(voucher, "VOUCHERNUMBER"),
                firstText(voucher, "REFERENCE"), parseDate(firstText(voucher, "DATE")),
                firstNonBlank(firstText(voucher, "PARTYLEDGERNAME"), firstText(voucher, "PARTYNAME")),
                firstText(voucher, "NARRATION"), originalValue, ledgers, inventory, adjustments, taxSummary);
    }

    private boolean isTaxLedger(String name) {
        String value = normalize(name);
        return value.contains("GST") || value.contains("CGST") || value.contains("SGST")
                || value.contains("IGST") || value.contains("TAX") || value.contains("TDS");
    }

    public boolean isAvailable() {
        try { request(null, 5000); return true; } catch (Exception e) { return false; }
    }
    public String getServerUrl() { return serverUrl; }
    private String post(String xml) throws Exception { return request(xml, 60000); }
    private String request(String xml, int timeout) throws Exception {
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(serverUrl).openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(timeout);
        try {
            if (xml != null) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                connection.setDoOutput(true);
                try (java.io.OutputStream out = connection.getOutputStream()) {
                    out.write(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            if (status != 200) throw new IllegalStateException("Tally HTTP request failed with status " + status);
            try (java.io.InputStream in = connection.getInputStream(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int length;
                while ((length = in.read(buffer)) != -1) out.write(buffer, 0, length);
                return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            }
        } finally { connection.disconnect(); }
    }

    private String buildOutstandingBillsRequest() {
        String companyVariable = companyName == null || companyName.codePoints().allMatch(Character::isWhitespace) ? "" :
                "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
        return String.format("<ENVELOPE>\n  <HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>CashflowOutstandingBills</ID></HEADER>\n  <BODY><DESC><STATICVARIABLES><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>%s</STATICVARIABLES>\n    <TDL><TDLMESSAGE><COLLECTION NAME=\"CashflowOutstandingBills\" ISMODIFY=\"No\">\n      <TYPE>Bill</TYPE>\n      <FETCH>Name,Parent,BillDate,BillCreditPeriod,IsAdvance,ClosingBalance</FETCH>\n    </COLLECTION></TDLMESSAGE></TDL>\n  </DESC></BODY>\n</ENVELOPE>\n", companyVariable);
    }

    private String buildGroupsRequest() {
        return buildSimpleCollectionRequest("CashflowAdvanceGroups", "Group", "Name,Parent");
    }

    private String buildLedgersRequest() {
        return buildSimpleCollectionRequest("CashflowAdvanceLedgers", "Ledger", "Name,Parent,ClosingBalance");
    }

    private String buildAdvanceVoucherRequest(String partyName) {
        String companyVariable = companyName == null || companyName.codePoints().allMatch(Character::isWhitespace) ? "" :
                "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
        String formulaParty = escapeXml(partyName.replace("\"", "\"\""));
        return String.format("<ENVELOPE>\n  <HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>CashflowAdvanceVouchers</ID></HEADER>\n  <BODY><DESC><STATICVARIABLES><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>%s</STATICVARIABLES>\n    <TDL><TDLMESSAGE>\n      <COLLECTION NAME=\"CashflowAdvanceVouchers\" ISMODIFY=\"No\">\n        <TYPE>Voucher</TYPE><FILTER>CashflowAdvanceParty</FILTER>\n        <FETCH>MasterID,GUID,Date,VoucherTypeName,VoucherNumber,Reference,PartyLedgerName,Narration,AllLedgerEntries.*</FETCH>\n      </COLLECTION>\n      <SYSTEM TYPE=\"Formulae\" NAME=\"CashflowAdvanceParty\">$PartyLedgerName = \"%s\"</SYSTEM>\n    </TDLMESSAGE></TDL>\n  </DESC></BODY>\n</ENVELOPE>\n", companyVariable, formulaParty);
    }

    private String buildSimpleCollectionRequest(String collectionName, String type, String fetch) {
        String companyVariable = companyName == null || companyName.codePoints().allMatch(Character::isWhitespace) ? "" :
                "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
        return String.format("<ENVELOPE>\n  <HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>%s</ID></HEADER>\n  <BODY><DESC><STATICVARIABLES><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>%s</STATICVARIABLES>\n    <TDL><TDLMESSAGE><COLLECTION NAME=\"%s\" ISMODIFY=\"No\">\n      <TYPE>%s</TYPE><FETCH>%s</FETCH>\n    </COLLECTION></TDLMESSAGE></TDL>\n  </DESC></BODY>\n</ENVELOPE>\n", collectionName, companyVariable, collectionName, type, fetch);
    }

    private String buildVoucherDetailsRequest(LocalDate fromDate, LocalDate toDate) {
        String companyVariable = companyName == null || companyName.codePoints().allMatch(Character::isWhitespace) ? "" :
                "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
        return String.format("<ENVELOPE>\n  <HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>CashflowVoucherDetails</ID></HEADER>\n  <BODY><DESC><STATICVARIABLES><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>%s</STATICVARIABLES>\n    <TDL><TDLMESSAGE>\n      <COLLECTION NAME=\"CashflowVoucherDetails\" ISMODIFY=\"No\">\n        <TYPE>Voucher</TYPE><FILTER>CashflowVoucherDate</FILTER>\n        <FETCH>MasterID,AlterID,GUID,Date,VoucherTypeName,VoucherNumber,Reference,PartyLedgerName,PartyName,Narration,AllLedgerEntries.*,AllInventoryEntries.*</FETCH>\n      </COLLECTION>\n      <SYSTEM TYPE=\"Formulae\" NAME=\"CashflowVoucherDate\">$Date &gt;= $$Date:\"%s\" AND $Date &lt;= $$Date:\"%s\"</SYSTEM>\n    </TDLMESSAGE></TDL>\n  </DESC></BODY>\n</ENVELOPE>\n", companyVariable,
                fromDate.format(TALLY_FORMULA_DATE), toDate.format(TALLY_FORMULA_DATE));
    }

    private String buildPurchaseVoucherRequest(LocalDate fromDate, LocalDate toDate) {
        String companyVariable = companyName == null || companyName.codePoints().allMatch(Character::isWhitespace) ? "" :
                "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
        return String.format("<ENVELOPE>\n  <HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>CashflowPurchasePipeline</ID></HEADER>\n"
                + "  <BODY><DESC><STATICVARIABLES><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>%s</STATICVARIABLES>\n"
                + "    <TDL><TDLMESSAGE><COLLECTION NAME=\"CashflowPurchasePipeline\" ISMODIFY=\"No\">\n"
                + "      <TYPE>Voucher</TYPE><FILTER>CashflowPurchasePipelinePeriod</FILTER>\n"
                + "      <FETCH>MasterID,Date,VoucherTypeName,VoucherNumber,Reference,PartyLedgerName,PartyName,Narration,AllLedgerEntries.*</FETCH>\n"
                + "    </COLLECTION><SYSTEM TYPE=\"Formulae\" NAME=\"CashflowPurchasePipelinePeriod\">$Date &gt;= $$Date:\"%s\" AND $Date &lt;= $$Date:\"%s\" AND $VoucherTypeName = \"Purchase\"</SYSTEM>\n"
                + "    </TDLMESSAGE></TDL></DESC></BODY></ENVELOPE>\n",
                companyVariable, fromDate.format(TALLY_FORMULA_DATE), toDate.format(TALLY_FORMULA_DATE));
    }

    private String buildCostCentreVoucherRequest(LocalDate fromDate, LocalDate toDate) {
        String companyVariable = companyName == null || companyName.codePoints().allMatch(Character::isWhitespace) ? "" :
                "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
        return String.format("<ENVELOPE>\n  <HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>CashflowCostCentreVouchers</ID></HEADER>\n  <BODY><DESC><STATICVARIABLES><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>%s</STATICVARIABLES>\n    <TDL><TDLMESSAGE>\n      <COLLECTION NAME=\"CashflowCostCentreVouchers\" ISMODIFY=\"No\">\n        <TYPE>Voucher</TYPE><FILTER>CashflowCostCentrePeriod</FILTER>\n        <FETCH>MasterID,GUID,Date,VoucherTypeName,VoucherNumber,Reference,PartyLedgerName,PartyName,Narration,AllLedgerEntries.LedgerName,AllLedgerEntries.CategoryAllocations.*</FETCH>\n      </COLLECTION>\n      <SYSTEM TYPE=\"Formulae\" NAME=\"CashflowCostCentrePeriod\">$Date &gt;= $$Date:\"%s\" AND $Date &lt;= $$Date:\"%s\"</SYSTEM>\n    </TDLMESSAGE></TDL>\n  </DESC></BODY>\n</ENVELOPE>\n", companyVariable, fromDate.format(TALLY_FORMULA_DATE),
                toDate.format(TALLY_FORMULA_DATE));
    }

    private String buildAllCostCentreVoucherRequest() {
        String companyVariable = companyName == null || companyName.codePoints().allMatch(Character::isWhitespace) ? "" :
                "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
        return String.format("<ENVELOPE>\n  <HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Collection</TYPE><ID>CashflowAllCostCentreVouchers</ID></HEADER>\n  <BODY><DESC><STATICVARIABLES><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>%s</STATICVARIABLES>\n    <TDL><TDLMESSAGE>\n      <COLLECTION NAME=\"CashflowAllCostCentreVouchers\" ISMODIFY=\"No\">\n        <TYPE>Voucher</TYPE>\n        <FETCH>MasterID,GUID,Date,VoucherTypeName,VoucherNumber,Reference,PartyLedgerName,PartyName,Narration,IsCancelled,IsOptional,AllLedgerEntries.LedgerName,AllLedgerEntries.CategoryAllocations.*,AllInventoryEntries.AccountingAllocations.LedgerName,AllInventoryEntries.AccountingAllocations.CategoryAllocations.*</FETCH>\n      </COLLECTION>\n    </TDLMESSAGE></TDL>\n  </DESC></BODY>\n</ENVELOPE>\n", companyVariable);
    }

    String buildCostCentreSummaryRequest(String costCentre, LocalDate fromDate, LocalDate toDate) {
        String selectedCentre = escapeXml(costCentre);
        return "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST>"
                + "<TYPE>Data</TYPE><ID>Cost Centre Summary</ID></HEADER><BODY><DESC><STATICVARIABLES>"
                + "<SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>"
                + tallyCompanyVariable()
                + "<SVFROMDATE TYPE=\"Date\">" + fromDate.format(TALLY_FORMULA_DATE) + "</SVFROMDATE>"
                + "<SVTODATE TYPE=\"Date\">" + toDate.format(TALLY_FORMULA_DATE) + "</SVTODATE>"
                + "<SVCurrentCostCentre>" + selectedCentre + "</SVCurrentCostCentre>"
                + "<COSTCENTRENAME>" + selectedCentre + "</COSTCENTRENAME>"
                + "</STATICVARIABLES></DESC></BODY></ENVELOPE>";
    }

    private String buildCostCentreBreakupRequest(String costCentre, LocalDate fromDate, LocalDate toDate) {
        return buildNativeCostCentreReportRequest("Cost Centre Breakup", costCentre, fromDate, toDate);
    }

    String buildNativeCostCentreReportRequest(String report, String costCentre,
                                               LocalDate fromDate, LocalDate toDate) {
        String selectedCentre = escapeXml(costCentre);
        return "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST>"
                + "<TYPE>Data</TYPE><ID>" + escapeXml(report) + "</ID></HEADER><BODY><DESC><STATICVARIABLES>"
                + "<SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT>"
                + tallyCompanyVariable()
                + "<SVFROMDATE TYPE=\"Date\">" + fromDate.format(TALLY_FORMULA_DATE) + "</SVFROMDATE>"
                + "<SVTODATE TYPE=\"Date\">" + toDate.format(TALLY_FORMULA_DATE) + "</SVTODATE>"
                + "<SVCurrentCostCentre>" + selectedCentre + "</SVCurrentCostCentre>"
                + "<COSTCENTRENAME>" + selectedCentre + "</COSTCENTRENAME>"
                + "</STATICVARIABLES></DESC></BODY></ENVELOPE>";
    }

    private String tallyCompanyVariable() {
        return companyName == null || companyName.codePoints().allMatch(Character::isWhitespace)
                ? ""
                : "<SVCURRENTCOMPANY>" + escapeXml(companyName) + "</SVCURRENTCOMPANY>";
    }

    private Document parseXml(String xml) throws Exception {
        String safeXml = sanitizeInvalidXmlCharacters(xml);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(safeXml)));
    }

    private String sanitizeInvalidXmlCharacters(String xml) {
        if (xml == null) return "";
        return xml
                .replaceAll("(?i)&#x(?:0*[0-8BCEF]|0*1[0-9A-F]);", "")
                .replaceAll("&#0*(?:[0-8]|1[1-9]|2[0-9]|3[01]);", "")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }

    private String firstText(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        Node node = nodes.item(0);
        String value = node.getTextContent();
        return value == null || value.codePoints().allMatch(Character::isWhitespace) ? null : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.codePoints().allMatch(Character::isWhitespace)) return first.trim();
        return second == null || second.codePoints().allMatch(Character::isWhitespace) ? null : second.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private boolean equalsNormalized(String actual, String alreadyNormalizedExpected) {
        return normalize(actual).equals(alreadyNormalizedExpected);
    }

    private double parseAmount(String value) {
        if (value == null) return 0d;
        String cleaned = value.replace(",", "").replaceAll("[^0-9.\\-]", "");
        if (cleaned.codePoints().allMatch(Character::isWhitespace) || "-".equals(cleaned)) return 0d;
        return Double.parseDouble(cleaned);
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null ? null : LocalDate.parse(value.trim(), TALLY_DATE);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate parseReportDate(String value) {
        try {
            return value == null ? null : LocalDate.parse(value.trim(), TALLY_REPORT_DATE);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate calculateDueDate(LocalDate billDate, String creditPeriod) {
        if (creditPeriod == null || creditPeriod.codePoints().allMatch(Character::isWhitespace)) return billDate;
        Matcher matcher = CREDIT_PERIOD.matcher(creditPeriod.toLowerCase(Locale.ROOT));
        if (!matcher.find()) return billDate;
        int amount = Integer.parseInt(matcher.group(1));
        switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
            case "week": return billDate.plusWeeks(amount);
            case "month": return billDate.plusMonths(amount);
            case "year": return billDate.plusYears(amount);
            default: return billDate.plusDays(amount);
        }
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    public static final class LedgerLine {
        private final String name;
        private final double amount;
        private final boolean tax;
        public LedgerLine(String name, double amount, boolean tax) {
            this.name = name;
            this.amount = amount;
            this.tax = tax;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("name")
        public String name() { return name; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @com.fasterxml.jackson.annotation.JsonProperty("tax")
        public boolean tax() { return tax; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof LedgerLine)) return false;
            LedgerLine that = (LedgerLine) other; return java.util.Objects.equals(name, that.name) && java.util.Objects.equals(amount, that.amount) && java.util.Objects.equals(tax, that.tax);
        }
        @Override public int hashCode() { return java.util.Objects.hash(name, amount, tax); }
}
    public static final class InventoryLine {
        private final String name;
        private final String quantity;
        private final String rate;
        private final double amount;
        public InventoryLine(String name, String quantity, String rate, double amount) {
            this.name = name;
            this.quantity = quantity;
            this.rate = rate;
            this.amount = amount;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("name")
        public String name() { return name; }
        @com.fasterxml.jackson.annotation.JsonProperty("quantity")
        public String quantity() { return quantity; }
        @com.fasterxml.jackson.annotation.JsonProperty("rate")
        public String rate() { return rate; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof InventoryLine)) return false;
            InventoryLine that = (InventoryLine) other; return java.util.Objects.equals(name, that.name) && java.util.Objects.equals(quantity, that.quantity) && java.util.Objects.equals(rate, that.rate) && java.util.Objects.equals(amount, that.amount);
        }
        @Override public int hashCode() { return java.util.Objects.hash(name, quantity, rate, amount); }
}
    enum FinancialNature { REVENUE, EXPENSE, NON_REVENUE }
    public static final class BillAdjustment {
        private final String billName;
        private final String billType;
        private final double amount;
        public BillAdjustment(String billName, String billType, double amount) {
            this.billName = billName;
            this.billType = billType;
            this.amount = amount;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("billName")
        public String billName() { return billName; }
        @com.fasterxml.jackson.annotation.JsonProperty("billType")
        public String billType() { return billType; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof BillAdjustment)) return false;
            BillAdjustment that = (BillAdjustment) other; return java.util.Objects.equals(billName, that.billName) && java.util.Objects.equals(billType, that.billType) && java.util.Objects.equals(amount, that.amount);
        }
        @Override public int hashCode() { return java.util.Objects.hash(billName, billType, amount); }
}
    public enum AdvanceType { CUSTOMER_ADVANCE_RECEIVED, SUPPLIER_ADVANCE_PAID }
    public static final class AdvanceReference {
        private final String referenceNumber;
        private final LocalDate billDate;
        private final double amount;
        public AdvanceReference(String referenceNumber, LocalDate billDate, double amount) {
            this.referenceNumber = referenceNumber;
            this.billDate = billDate;
            this.amount = amount;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("referenceNumber")
        public String referenceNumber() { return referenceNumber; }
        @com.fasterxml.jackson.annotation.JsonProperty("billDate")
        public LocalDate billDate() { return billDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof AdvanceReference)) return false;
            AdvanceReference that = (AdvanceReference) other; return java.util.Objects.equals(referenceNumber, that.referenceNumber) && java.util.Objects.equals(billDate, that.billDate) && java.util.Objects.equals(amount, that.amount);
        }
        @Override public int hashCode() { return java.util.Objects.hash(referenceNumber, billDate, amount); }
}
    public static final class AdvanceBalance {
        private final String partyName;
        private final String ledgerGroup;
        private final double amount;
        private final double netLedgerBalance;
        private final AdvanceType type;
        private final String detectionBasis;
        private final int referenceCount;
        private final List<AdvanceReference> advanceReferences;
        public AdvanceBalance(String partyName, String ledgerGroup, double amount, double netLedgerBalance,
                                 AdvanceType type, String detectionBasis, int referenceCount,
                                 List<AdvanceReference> advanceReferences) {
            this.partyName = partyName;
            this.ledgerGroup = ledgerGroup;
            this.amount = amount;
            this.netLedgerBalance = netLedgerBalance;
            this.type = type;
            this.detectionBasis = detectionBasis;
            this.referenceCount = referenceCount;
            this.advanceReferences = advanceReferences;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("partyName")
        public String partyName() { return partyName; }
        @com.fasterxml.jackson.annotation.JsonProperty("ledgerGroup")
        public String ledgerGroup() { return ledgerGroup; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @com.fasterxml.jackson.annotation.JsonProperty("netLedgerBalance")
        public double netLedgerBalance() { return netLedgerBalance; }
        @com.fasterxml.jackson.annotation.JsonProperty("type")
        public AdvanceType type() { return type; }
        @com.fasterxml.jackson.annotation.JsonProperty("detectionBasis")
        public String detectionBasis() { return detectionBasis; }
        @com.fasterxml.jackson.annotation.JsonProperty("referenceCount")
        public int referenceCount() { return referenceCount; }
        @com.fasterxml.jackson.annotation.JsonProperty("advanceReferences")
        public List<AdvanceReference> advanceReferences() { return advanceReferences; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof AdvanceBalance)) return false;
            AdvanceBalance that = (AdvanceBalance) other; return java.util.Objects.equals(partyName, that.partyName) && java.util.Objects.equals(ledgerGroup, that.ledgerGroup) && java.util.Objects.equals(amount, that.amount) && java.util.Objects.equals(netLedgerBalance, that.netLedgerBalance) && java.util.Objects.equals(type, that.type) && java.util.Objects.equals(detectionBasis, that.detectionBasis) && java.util.Objects.equals(referenceCount, that.referenceCount) && java.util.Objects.equals(advanceReferences, that.advanceReferences);
        }
        @Override public int hashCode() { return java.util.Objects.hash(partyName, ledgerGroup, amount, netLedgerBalance, type, detectionBasis, referenceCount, advanceReferences); }
}
    public static final class AdvanceSnapshot {
        private final List<AdvanceBalance> customerAdvances;
        private final List<AdvanceBalance> supplierAdvances;
        private final double customerAdvanceTotal;
        private final double supplierAdvanceTotal;
        public AdvanceSnapshot(List<AdvanceBalance> customerAdvances, List<AdvanceBalance> supplierAdvances,
                                  double customerAdvanceTotal, double supplierAdvanceTotal) {
            this.customerAdvances = customerAdvances;
            this.supplierAdvances = supplierAdvances;
            this.customerAdvanceTotal = customerAdvanceTotal;
            this.supplierAdvanceTotal = supplierAdvanceTotal;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("customerAdvances")
        public List<AdvanceBalance> customerAdvances() { return customerAdvances; }
        @com.fasterxml.jackson.annotation.JsonProperty("supplierAdvances")
        public List<AdvanceBalance> supplierAdvances() { return supplierAdvances; }
        @com.fasterxml.jackson.annotation.JsonProperty("customerAdvanceTotal")
        public double customerAdvanceTotal() { return customerAdvanceTotal; }
        @com.fasterxml.jackson.annotation.JsonProperty("supplierAdvanceTotal")
        public double supplierAdvanceTotal() { return supplierAdvanceTotal; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof AdvanceSnapshot)) return false;
            AdvanceSnapshot that = (AdvanceSnapshot) other; return java.util.Objects.equals(customerAdvances, that.customerAdvances) && java.util.Objects.equals(supplierAdvances, that.supplierAdvances) && java.util.Objects.equals(customerAdvanceTotal, that.customerAdvanceTotal) && java.util.Objects.equals(supplierAdvanceTotal, that.supplierAdvanceTotal);
        }
        @Override public int hashCode() { return java.util.Objects.hash(customerAdvances, supplierAdvances, customerAdvanceTotal, supplierAdvanceTotal); }
}
    public static final class CostCentreAllocation {
        private final String name;
        private final double amount;
        public CostCentreAllocation(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("name")
        public String name() { return name; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof CostCentreAllocation)) return false;
            CostCentreAllocation that = (CostCentreAllocation) other; return java.util.Objects.equals(name, that.name) && java.util.Objects.equals(amount, that.amount);
        }
        @Override public int hashCode() { return java.util.Objects.hash(name, amount); }
}
    public static final class CostCentreNode {
        private final String name;
        private final String parent;
        private final String category;
        private final String classification;
        public CostCentreNode(String name, String parent, String category, String classification) {
            this.name = name;
            this.parent = parent;
            this.category = category;
            this.classification = classification;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("name")
        public String name() { return name; }
        @com.fasterxml.jackson.annotation.JsonProperty("parent")
        public String parent() { return parent; }
        @com.fasterxml.jackson.annotation.JsonProperty("category")
        public String category() { return category; }
        @com.fasterxml.jackson.annotation.JsonProperty("classification")
        public String classification() { return classification; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof CostCentreNode)) return false;
            CostCentreNode that = (CostCentreNode) other; return java.util.Objects.equals(name, that.name) && java.util.Objects.equals(parent, that.parent) && java.util.Objects.equals(category, that.category) && java.util.Objects.equals(classification, that.classification);
        }
        @Override public int hashCode() { return java.util.Objects.hash(name, parent, category, classification); }
}
    public static final class CostCentreSite {
        private final String siteName;
        private final List<CostCentreNode> costCentres;
        public CostCentreSite(String siteName, List<CostCentreNode> costCentres) {
            this.siteName = siteName;
            this.costCentres = costCentres;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("siteName")
        public String siteName() { return siteName; }
        @com.fasterxml.jackson.annotation.JsonProperty("costCentres")
        public List<CostCentreNode> costCentres() { return costCentres; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof CostCentreSite)) return false;
            CostCentreSite that = (CostCentreSite) other; return java.util.Objects.equals(siteName, that.siteName) && java.util.Objects.equals(costCentres, that.costCentres);
        }
        @Override public int hashCode() { return java.util.Objects.hash(siteName, costCentres); }
}
    public static final class CostCentreSnapshot {
        private final List<CostCentreSite> sites;
        private final int siteCount;
        private final long costCentreCount;
        public CostCentreSnapshot(List<CostCentreSite> sites, int siteCount, long costCentreCount) {
            this.sites = sites;
            this.siteCount = siteCount;
            this.costCentreCount = costCentreCount;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("sites")
        public List<CostCentreSite> sites() { return sites; }
        @com.fasterxml.jackson.annotation.JsonProperty("siteCount")
        public int siteCount() { return siteCount; }
        @com.fasterxml.jackson.annotation.JsonProperty("costCentreCount")
        public long costCentreCount() { return costCentreCount; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof CostCentreSnapshot)) return false;
            CostCentreSnapshot that = (CostCentreSnapshot) other; return java.util.Objects.equals(sites, that.sites) && java.util.Objects.equals(siteCount, that.siteCount) && java.util.Objects.equals(costCentreCount, that.costCentreCount);
        }
        @Override public int hashCode() { return java.util.Objects.hash(sites, siteCount, costCentreCount); }
}
    public static final class CostCentreTransaction {
        private final String masterId;
        private final String guid;
        private final LocalDate voucherDate;
        private final String voucherType;
        private final String voucherNumber;
        private final String reference;
        private final String partyName;
        private final String ledgerName;
        private final String costCentreName;
        private final double allocatedAmount;
        private final String narration;
        public CostCentreTransaction(String masterId, String guid, LocalDate voucherDate, String voucherType,
                                        String voucherNumber, String reference, String partyName, String ledgerName,
                                        String costCentreName, double allocatedAmount, String narration) {
            this.masterId = masterId;
            this.guid = guid;
            this.voucherDate = voucherDate;
            this.voucherType = voucherType;
            this.voucherNumber = voucherNumber;
            this.reference = reference;
            this.partyName = partyName;
            this.ledgerName = ledgerName;
            this.costCentreName = costCentreName;
            this.allocatedAmount = allocatedAmount;
            this.narration = narration;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("masterId")
        public String masterId() { return masterId; }
        @com.fasterxml.jackson.annotation.JsonProperty("guid")
        public String guid() { return guid; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherDate")
        public LocalDate voucherDate() { return voucherDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherType")
        public String voucherType() { return voucherType; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherNumber")
        public String voucherNumber() { return voucherNumber; }
        @com.fasterxml.jackson.annotation.JsonProperty("reference")
        public String reference() { return reference; }
        @com.fasterxml.jackson.annotation.JsonProperty("partyName")
        public String partyName() { return partyName; }
        @com.fasterxml.jackson.annotation.JsonProperty("ledgerName")
        public String ledgerName() { return ledgerName; }
        @com.fasterxml.jackson.annotation.JsonProperty("costCentreName")
        public String costCentreName() { return costCentreName; }
        @com.fasterxml.jackson.annotation.JsonProperty("allocatedAmount")
        public double allocatedAmount() { return allocatedAmount; }
        @com.fasterxml.jackson.annotation.JsonProperty("narration")
        public String narration() { return narration; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof CostCentreTransaction)) return false;
            CostCentreTransaction that = (CostCentreTransaction) other; return java.util.Objects.equals(masterId, that.masterId) && java.util.Objects.equals(guid, that.guid) && java.util.Objects.equals(voucherDate, that.voucherDate) && java.util.Objects.equals(voucherType, that.voucherType) && java.util.Objects.equals(voucherNumber, that.voucherNumber) && java.util.Objects.equals(reference, that.reference) && java.util.Objects.equals(partyName, that.partyName) && java.util.Objects.equals(ledgerName, that.ledgerName) && java.util.Objects.equals(costCentreName, that.costCentreName) && java.util.Objects.equals(allocatedAmount, that.allocatedAmount) && java.util.Objects.equals(narration, that.narration);
        }
        @Override public int hashCode() { return java.util.Objects.hash(masterId, guid, voucherDate, voucherType, voucherNumber, reference, partyName, ledgerName, costCentreName, allocatedAmount, narration); }
}
    public static final class CostCentreTransactions {
        private final String costCentreName;
        private final LocalDate fromDate;
        private final LocalDate toDate;
        private final double totalAllocated;
        private final long vendorCount;
        private final List<CostCentreTransaction> transactions;
        public CostCentreTransactions(String costCentreName, LocalDate fromDate, LocalDate toDate,
                                         double totalAllocated, long vendorCount,
                                         List<CostCentreTransaction> transactions) {
            this.costCentreName = costCentreName;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.totalAllocated = totalAllocated;
            this.vendorCount = vendorCount;
            this.transactions = transactions;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("costCentreName")
        public String costCentreName() { return costCentreName; }
        @com.fasterxml.jackson.annotation.JsonProperty("fromDate")
        public LocalDate fromDate() { return fromDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("toDate")
        public LocalDate toDate() { return toDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("totalAllocated")
        public double totalAllocated() { return totalAllocated; }
        @com.fasterxml.jackson.annotation.JsonProperty("vendorCount")
        public long vendorCount() { return vendorCount; }
        @com.fasterxml.jackson.annotation.JsonProperty("transactions")
        public List<CostCentreTransaction> transactions() { return transactions; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof CostCentreTransactions)) return false;
            CostCentreTransactions that = (CostCentreTransactions) other; return java.util.Objects.equals(costCentreName, that.costCentreName) && java.util.Objects.equals(fromDate, that.fromDate) && java.util.Objects.equals(toDate, that.toDate) && java.util.Objects.equals(totalAllocated, that.totalAllocated) && java.util.Objects.equals(vendorCount, that.vendorCount) && java.util.Objects.equals(transactions, that.transactions);
        }
        @Override public int hashCode() { return java.util.Objects.hash(costCentreName, fromDate, toDate, totalAllocated, vendorCount, transactions); }
}
    public static final class SiteAllocation {
        private final LocalDate voucherDate;
        private final String voucherType;
        private final String voucherNumber;
        private final String reference;
        private final String partyName;
        private final String ledgerName;
        private final String costCentreName;
        private final String nature;
        private final String classification;
        private final double amount;
        private final String narration;
        public SiteAllocation(LocalDate voucherDate, String voucherType, String voucherNumber, String reference,
                                 String partyName, String ledgerName, String costCentreName, String nature,
                                 String classification, double amount, String narration) {
            this.voucherDate = voucherDate;
            this.voucherType = voucherType;
            this.voucherNumber = voucherNumber;
            this.reference = reference;
            this.partyName = partyName;
            this.ledgerName = ledgerName;
            this.costCentreName = costCentreName;
            this.nature = nature;
            this.classification = classification;
            this.amount = amount;
            this.narration = narration;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherDate")
        public LocalDate voucherDate() { return voucherDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherType")
        public String voucherType() { return voucherType; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherNumber")
        public String voucherNumber() { return voucherNumber; }
        @com.fasterxml.jackson.annotation.JsonProperty("reference")
        public String reference() { return reference; }
        @com.fasterxml.jackson.annotation.JsonProperty("partyName")
        public String partyName() { return partyName; }
        @com.fasterxml.jackson.annotation.JsonProperty("ledgerName")
        public String ledgerName() { return ledgerName; }
        @com.fasterxml.jackson.annotation.JsonProperty("costCentreName")
        public String costCentreName() { return costCentreName; }
        @com.fasterxml.jackson.annotation.JsonProperty("nature")
        public String nature() { return nature; }
        @com.fasterxml.jackson.annotation.JsonProperty("classification")
        public String classification() { return classification; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @com.fasterxml.jackson.annotation.JsonProperty("narration")
        public String narration() { return narration; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof SiteAllocation)) return false;
            SiteAllocation that = (SiteAllocation) other; return java.util.Objects.equals(voucherDate, that.voucherDate) && java.util.Objects.equals(voucherType, that.voucherType) && java.util.Objects.equals(voucherNumber, that.voucherNumber) && java.util.Objects.equals(reference, that.reference) && java.util.Objects.equals(partyName, that.partyName) && java.util.Objects.equals(ledgerName, that.ledgerName) && java.util.Objects.equals(costCentreName, that.costCentreName) && java.util.Objects.equals(nature, that.nature) && java.util.Objects.equals(classification, that.classification) && java.util.Objects.equals(amount, that.amount) && java.util.Objects.equals(narration, that.narration);
        }
        @Override public int hashCode() { return java.util.Objects.hash(voucherDate, voucherType, voucherNumber, reference, partyName, ledgerName, costCentreName, nature, classification, amount, narration); }
}
    static final class NativeCostCentreSummary {
        private final double debitTransactions;
        private final double creditTransactions;
        private final double closingBalance;
        public NativeCostCentreSummary(double debitTransactions, double creditTransactions,
                                   double closingBalance) {
            this.debitTransactions = debitTransactions;
            this.creditTransactions = creditTransactions;
            this.closingBalance = closingBalance;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("debitTransactions")
        public double debitTransactions() { return debitTransactions; }
        @com.fasterxml.jackson.annotation.JsonProperty("creditTransactions")
        public double creditTransactions() { return creditTransactions; }
        @com.fasterxml.jackson.annotation.JsonProperty("closingBalance")
        public double closingBalance() { return closingBalance; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof NativeCostCentreSummary)) return false;
            NativeCostCentreSummary that = (NativeCostCentreSummary) other; return java.util.Objects.equals(debitTransactions, that.debitTransactions) && java.util.Objects.equals(creditTransactions, that.creditTransactions) && java.util.Objects.equals(closingBalance, that.closingBalance);
        }
        @Override public int hashCode() { return java.util.Objects.hash(debitTransactions, creditTransactions, closingBalance); }
}
    static final class NativePnlSummary {
        private final double revenue;
        private final double expense;
        private final List<SitePnlLine> lines;
        public NativePnlSummary(double revenue, double expense, List<SitePnlLine> lines) {
            this.revenue = revenue;
            this.expense = expense;
            this.lines = lines;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("revenue")
        public double revenue() { return revenue; }
        @com.fasterxml.jackson.annotation.JsonProperty("expense")
        public double expense() { return expense; }
        @com.fasterxml.jackson.annotation.JsonProperty("lines")
        public List<SitePnlLine> lines() { return lines; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof NativePnlSummary)) return false;
            NativePnlSummary that = (NativePnlSummary) other; return java.util.Objects.equals(revenue, that.revenue) && java.util.Objects.equals(expense, that.expense) && java.util.Objects.equals(lines, that.lines);
        }
        @Override public int hashCode() { return java.util.Objects.hash(revenue, expense, lines); }
}
    public static final class SitePnlLine {
        private final String particular;
        private final double debit;
        private final double credit;
        private final double closingBalance;
        public SitePnlLine(String particular, double debit, double credit, double closingBalance) {
            this.particular = particular;
            this.debit = debit;
            this.credit = credit;
            this.closingBalance = closingBalance;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("particular")
        public String particular() { return particular; }
        @com.fasterxml.jackson.annotation.JsonProperty("debit")
        public double debit() { return debit; }
        @com.fasterxml.jackson.annotation.JsonProperty("credit")
        public double credit() { return credit; }
        @com.fasterxml.jackson.annotation.JsonProperty("closingBalance")
        public double closingBalance() { return closingBalance; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof SitePnlLine)) return false;
            SitePnlLine that = (SitePnlLine) other; return java.util.Objects.equals(particular, that.particular) && java.util.Objects.equals(debit, that.debit) && java.util.Objects.equals(credit, that.credit) && java.util.Objects.equals(closingBalance, that.closingBalance);
        }
        @Override public int hashCode() { return java.util.Objects.hash(particular, debit, credit, closingBalance); }
}
    public static final class SiteProfitability {
        private final String siteName;
        private final LocalDate fromDate;
        private final LocalDate toDate;
        private final int leafCostCentreCount;
        private final double debitTransactions;
        private final double creditTransactions;
        private final double closingBalance;
        private final double revenue;
        private final double expense;
        private final double profit;
        private final double margin;
        private final List<SitePnlLine> pnlLines;
        private final List<SiteAllocation> allocations;
        public SiteProfitability(String siteName, LocalDate fromDate, LocalDate toDate, int leafCostCentreCount,
                                    double debitTransactions, double creditTransactions, double closingBalance,
                                    double revenue, double expense, double profit, double margin,
                                    List<SitePnlLine> pnlLines, List<SiteAllocation> allocations) {
            this.siteName = siteName;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.leafCostCentreCount = leafCostCentreCount;
            this.debitTransactions = debitTransactions;
            this.creditTransactions = creditTransactions;
            this.closingBalance = closingBalance;
            this.revenue = revenue;
            this.expense = expense;
            this.profit = profit;
            this.margin = margin;
            this.pnlLines = pnlLines;
            this.allocations = allocations;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("siteName")
        public String siteName() { return siteName; }
        @com.fasterxml.jackson.annotation.JsonProperty("fromDate")
        public LocalDate fromDate() { return fromDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("toDate")
        public LocalDate toDate() { return toDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("leafCostCentreCount")
        public int leafCostCentreCount() { return leafCostCentreCount; }
        @com.fasterxml.jackson.annotation.JsonProperty("debitTransactions")
        public double debitTransactions() { return debitTransactions; }
        @com.fasterxml.jackson.annotation.JsonProperty("creditTransactions")
        public double creditTransactions() { return creditTransactions; }
        @com.fasterxml.jackson.annotation.JsonProperty("closingBalance")
        public double closingBalance() { return closingBalance; }
        @com.fasterxml.jackson.annotation.JsonProperty("revenue")
        public double revenue() { return revenue; }
        @com.fasterxml.jackson.annotation.JsonProperty("expense")
        public double expense() { return expense; }
        @com.fasterxml.jackson.annotation.JsonProperty("profit")
        public double profit() { return profit; }
        @com.fasterxml.jackson.annotation.JsonProperty("margin")
        public double margin() { return margin; }
        @com.fasterxml.jackson.annotation.JsonProperty("pnlLines")
        public List<SitePnlLine> pnlLines() { return pnlLines; }
        @com.fasterxml.jackson.annotation.JsonProperty("allocations")
        public List<SiteAllocation> allocations() { return allocations; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof SiteProfitability)) return false;
            SiteProfitability that = (SiteProfitability) other; return java.util.Objects.equals(siteName, that.siteName) && java.util.Objects.equals(fromDate, that.fromDate) && java.util.Objects.equals(toDate, that.toDate) && java.util.Objects.equals(leafCostCentreCount, that.leafCostCentreCount) && java.util.Objects.equals(debitTransactions, that.debitTransactions) && java.util.Objects.equals(creditTransactions, that.creditTransactions) && java.util.Objects.equals(closingBalance, that.closingBalance) && java.util.Objects.equals(revenue, that.revenue) && java.util.Objects.equals(expense, that.expense) && java.util.Objects.equals(profit, that.profit) && java.util.Objects.equals(margin, that.margin) && java.util.Objects.equals(pnlLines, that.pnlLines) && java.util.Objects.equals(allocations, that.allocations);
        }
        @Override public int hashCode() { return java.util.Objects.hash(siteName, fromDate, toDate, leafCostCentreCount, debitTransactions, creditTransactions, closingBalance, revenue, expense, profit, margin, pnlLines, allocations); }
}
    public static final class AdvanceTransaction {
        private final String masterId;
        private final String guid;
        private final String voucherType;
        private final String voucherNumber;
        private final String reference;
        private final LocalDate voucherDate;
        private final double amount;
        private final String narration;
        private final List<BillAdjustment> billAdjustments;
        private final List<CostCentreAllocation> costCentreAllocations;
        public AdvanceTransaction(String masterId, String guid, String voucherType, String voucherNumber,
                                     String reference, LocalDate voucherDate, double amount, String narration,
                                     List<BillAdjustment> billAdjustments,
                                     List<CostCentreAllocation> costCentreAllocations) {
            this.masterId = masterId;
            this.guid = guid;
            this.voucherType = voucherType;
            this.voucherNumber = voucherNumber;
            this.reference = reference;
            this.voucherDate = voucherDate;
            this.amount = amount;
            this.narration = narration;
            this.billAdjustments = billAdjustments;
            this.costCentreAllocations = costCentreAllocations;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("masterId")
        public String masterId() { return masterId; }
        @com.fasterxml.jackson.annotation.JsonProperty("guid")
        public String guid() { return guid; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherType")
        public String voucherType() { return voucherType; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherNumber")
        public String voucherNumber() { return voucherNumber; }
        @com.fasterxml.jackson.annotation.JsonProperty("reference")
        public String reference() { return reference; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherDate")
        public LocalDate voucherDate() { return voucherDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("amount")
        public double amount() { return amount; }
        @com.fasterxml.jackson.annotation.JsonProperty("narration")
        public String narration() { return narration; }
        @com.fasterxml.jackson.annotation.JsonProperty("billAdjustments")
        public List<BillAdjustment> billAdjustments() { return billAdjustments; }
        @com.fasterxml.jackson.annotation.JsonProperty("costCentreAllocations")
        public List<CostCentreAllocation> costCentreAllocations() { return costCentreAllocations; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof AdvanceTransaction)) return false;
            AdvanceTransaction that = (AdvanceTransaction) other; return java.util.Objects.equals(masterId, that.masterId) && java.util.Objects.equals(guid, that.guid) && java.util.Objects.equals(voucherType, that.voucherType) && java.util.Objects.equals(voucherNumber, that.voucherNumber) && java.util.Objects.equals(reference, that.reference) && java.util.Objects.equals(voucherDate, that.voucherDate) && java.util.Objects.equals(amount, that.amount) && java.util.Objects.equals(narration, that.narration) && java.util.Objects.equals(billAdjustments, that.billAdjustments) && java.util.Objects.equals(costCentreAllocations, that.costCentreAllocations);
        }
        @Override public int hashCode() { return java.util.Objects.hash(masterId, guid, voucherType, voucherNumber, reference, voucherDate, amount, narration, billAdjustments, costCentreAllocations); }
}
    public static final class AdvanceDetails {
        private final AdvanceBalance advance;
        private final List<AdvanceTransaction> transactions;
        public AdvanceDetails(AdvanceBalance advance, List<AdvanceTransaction> transactions) {
            this.advance = advance;
            this.transactions = transactions;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("advance")
        public AdvanceBalance advance() { return advance; }
        @com.fasterxml.jackson.annotation.JsonProperty("transactions")
        public List<AdvanceTransaction> transactions() { return transactions; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof AdvanceDetails)) return false;
            AdvanceDetails that = (AdvanceDetails) other; return java.util.Objects.equals(advance, that.advance) && java.util.Objects.equals(transactions, that.transactions);
        }
        @Override public int hashCode() { return java.util.Objects.hash(advance, transactions); }
}
    public static final class PurchaseVoucherMatch {
        private final String masterId;
        private final String voucherNumber;
        private final String reference;
        private final LocalDate voucherDate;
        private final String partyName;
        private final String narration;
        private final double amount;
        private final List<String> billReferences;

        public PurchaseVoucherMatch(String masterId, String voucherNumber, String reference,
                                    LocalDate voucherDate, String partyName, String narration,
                                    double amount, List<String> billReferences) {
            this.masterId = masterId;
            this.voucherNumber = voucherNumber;
            this.reference = reference;
            this.voucherDate = voucherDate;
            this.partyName = partyName;
            this.narration = narration;
            this.amount = amount;
            this.billReferences = billReferences;
        }
        public String masterId() { return masterId; }
        public String voucherNumber() { return voucherNumber; }
        public String reference() { return reference; }
        public LocalDate voucherDate() { return voucherDate; }
        public String partyName() { return partyName; }
        public String narration() { return narration; }
        public double amount() { return amount; }
        public List<String> billReferences() { return billReferences; }
    }

    public static final class VoucherDetails {
        private final boolean found;
        private final String message;
        private final String matchedBy;
        private final String masterId;
        private final String guid;
        private final String voucherType;
        private final String voucherNumber;
        private final String reference;
        private final LocalDate voucherDate;
        private final String partyName;
        private final String narration;
        private final double originalInvoiceValue;
        private final List<LedgerLine> ledgerEntries;
        private final List<InventoryLine> inventoryEntries;
        private final List<BillAdjustment> billAdjustments;
        private final Map<String, Double> taxSummary;
        public VoucherDetails(boolean found, String message, String matchedBy, String masterId, String guid,
                                 String voucherType, String voucherNumber, String reference, LocalDate voucherDate,
                                 String partyName, String narration, double originalInvoiceValue,
                                 List<LedgerLine> ledgerEntries, List<InventoryLine> inventoryEntries,
                                 List<BillAdjustment> billAdjustments, Map<String, Double> taxSummary) {
            this.found = found;
            this.message = message;
            this.matchedBy = matchedBy;
            this.masterId = masterId;
            this.guid = guid;
            this.voucherType = voucherType;
            this.voucherNumber = voucherNumber;
            this.reference = reference;
            this.voucherDate = voucherDate;
            this.partyName = partyName;
            this.narration = narration;
            this.originalInvoiceValue = originalInvoiceValue;
            this.ledgerEntries = ledgerEntries;
            this.inventoryEntries = inventoryEntries;
            this.billAdjustments = billAdjustments;
            this.taxSummary = taxSummary;
        }
        @com.fasterxml.jackson.annotation.JsonProperty("found")
        public boolean found() { return found; }
        @com.fasterxml.jackson.annotation.JsonProperty("message")
        public String message() { return message; }
        @com.fasterxml.jackson.annotation.JsonProperty("matchedBy")
        public String matchedBy() { return matchedBy; }
        @com.fasterxml.jackson.annotation.JsonProperty("masterId")
        public String masterId() { return masterId; }
        @com.fasterxml.jackson.annotation.JsonProperty("guid")
        public String guid() { return guid; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherType")
        public String voucherType() { return voucherType; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherNumber")
        public String voucherNumber() { return voucherNumber; }
        @com.fasterxml.jackson.annotation.JsonProperty("reference")
        public String reference() { return reference; }
        @com.fasterxml.jackson.annotation.JsonProperty("voucherDate")
        public LocalDate voucherDate() { return voucherDate; }
        @com.fasterxml.jackson.annotation.JsonProperty("partyName")
        public String partyName() { return partyName; }
        @com.fasterxml.jackson.annotation.JsonProperty("narration")
        public String narration() { return narration; }
        @com.fasterxml.jackson.annotation.JsonProperty("originalInvoiceValue")
        public double originalInvoiceValue() { return originalInvoiceValue; }
        @com.fasterxml.jackson.annotation.JsonProperty("ledgerEntries")
        public List<LedgerLine> ledgerEntries() { return ledgerEntries; }
        @com.fasterxml.jackson.annotation.JsonProperty("inventoryEntries")
        public List<InventoryLine> inventoryEntries() { return inventoryEntries; }
        @com.fasterxml.jackson.annotation.JsonProperty("billAdjustments")
        public List<BillAdjustment> billAdjustments() { return billAdjustments; }
        @com.fasterxml.jackson.annotation.JsonProperty("taxSummary")
        public Map<String, Double> taxSummary() { return taxSummary; }
        @Override public boolean equals(Object other) {
            if (this == other) return true; if (!(other instanceof VoucherDetails)) return false;
            VoucherDetails that = (VoucherDetails) other; return java.util.Objects.equals(found, that.found) && java.util.Objects.equals(message, that.message) && java.util.Objects.equals(matchedBy, that.matchedBy) && java.util.Objects.equals(masterId, that.masterId) && java.util.Objects.equals(guid, that.guid) && java.util.Objects.equals(voucherType, that.voucherType) && java.util.Objects.equals(voucherNumber, that.voucherNumber) && java.util.Objects.equals(reference, that.reference) && java.util.Objects.equals(voucherDate, that.voucherDate) && java.util.Objects.equals(partyName, that.partyName) && java.util.Objects.equals(narration, that.narration) && java.util.Objects.equals(originalInvoiceValue, that.originalInvoiceValue) && java.util.Objects.equals(ledgerEntries, that.ledgerEntries) && java.util.Objects.equals(inventoryEntries, that.inventoryEntries) && java.util.Objects.equals(billAdjustments, that.billAdjustments) && java.util.Objects.equals(taxSummary, that.taxSummary);
        }
        @Override public int hashCode() { return java.util.Objects.hash(found, message, matchedBy, masterId, guid, voucherType, voucherNumber, reference, voucherDate, partyName, narration, originalInvoiceValue, ledgerEntries, inventoryEntries, billAdjustments, taxSummary); }

        static VoucherDetails notFound(String message) {
            return new VoucherDetails(false, message, null, null, null, null, null, null, null,
                    null, null, 0d, com.ncpl.sales.cashflow.util.Java8Collections.list(), com.ncpl.sales.cashflow.util.Java8Collections.list(), com.ncpl.sales.cashflow.util.Java8Collections.list(), com.ncpl.sales.cashflow.util.Java8Collections.map());
        }
    }
}
