package com.ncpl.sales.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ncpl.sales.model.MonthlyStockMovement;
import com.ncpl.sales.repository.MonthlyStockMovementRepo;

/**
 * Builds the finance stock movement report from stable item-master ids.
 *
 * <p>Quantities come from Envers stock snapshots at the two cut-offs. This
 * intentionally makes net outward include DC issues, returns, damage,
 * transfers and manual stock adjustments. Current-period valuation uses the
 * average PO rate used by the GRN-by-date and DC-by-date Finance reports,
 * falling back to Supply Price and then the latest positive GRN price.</p>
 */
@Service
public class MonthlyStockMovementService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int CALC_SCALE = 8;

    @PersistenceContext
    private EntityManager entityManager;

    private final MonthlyStockMovementRepo movementRepository;

    public MonthlyStockMovementService(MonthlyStockMovementRepo movementRepository) {
        this.movementRepository = movementRepository;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map> generate(Timestamp fromInclusive, Timestamp toInclusive) {
        if (fromInclusive == null || toInclusive == null || toInclusive.before(fromInclusive)) {
            throw new IllegalArgumentException("A valid monthly date range is required");
        }

        Map<String, BigDecimal> openingQty = loadStockSnapshot(fromInclusive.getTime() - 1L);
        java.time.LocalDate reportEndDate = toInclusive.toLocalDateTime().toLocalDate();
        Map<String, BigDecimal> closingQty = reportEndDate.equals(java.time.LocalDate.now())
                ? loadCurrentStock() : loadStockSnapshot(toInclusive.getTime());
        Map<String, GrnSummary> grn = loadGrnSummary(fromInclusive, toInclusive);
        Map<String, DcSummary> dcMovements = loadDcSummary(fromInclusive, toInclusive);
        Map<String, BigDecimal> averagePoPrices = loadAveragePoPrices();
        Map<String, BigDecimal> latestSupplyPrices = loadLatestSupplyPrices();
        Map<String, BigDecimal> latestGrnPrices = loadLastGrnRateBefore(
                new Timestamp(toInclusive.getTime() + 1L));

        Map<String, BigDecimal> openingRates = new LinkedHashMap<String, BigDecimal>();
        Map<String, BigDecimal> openingValues = new LinkedHashMap<String, BigDecimal>();
        Map<String, BigDecimal> approvedClosingRates = new LinkedHashMap<String, BigDecimal>();
        Map<String, BigDecimal> approvedClosingValues = new LinkedHashMap<String, BigDecimal>();
        java.time.LocalDate previousClosingDate = fromInclusive.toLocalDateTime().toLocalDate().minusDays(1);
        List<MonthlyStockMovement> previousSnapshots = movementRepository.findByReportDate(previousClosingDate);
        if (!previousSnapshots.isEmpty()) {
            // An approved frozen close is authoritative. Items created in ERP
            // later still appear in the report, but start with zero opening.
            openingQty.clear();
        }
        for (MonthlyStockMovement previous : previousSnapshots) {
            openingQty.put(previous.getItemMasterId(), value(previous.getClosingQty()));
            openingRates.put(previous.getItemMasterId(), value(previous.getClosingRate()));
            openingValues.put(previous.getItemMasterId(), value(previous.getClosingValue()));
        }

        List<MonthlyStockMovement> approvedClosingSnapshots = movementRepository.findByReportDate(reportEndDate);
        if (!approvedClosingSnapshots.isEmpty()) {
            // For a Finance-approved historical month, the frozen month-end
            // snapshot is authoritative; Envers does not reliably reconstruct
            // stock balances in this legacy database.
            closingQty.clear();
            for (MonthlyStockMovement closing : approvedClosingSnapshots) {
                closingQty.put(closing.getItemMasterId(), value(closing.getClosingQty()));
                approvedClosingRates.put(closing.getItemMasterId(), value(closing.getClosingRate()));
                approvedClosingValues.put(closing.getItemMasterId(), value(closing.getClosingValue()));
            }
        }

        Query itemQuery = entityManager.createNativeQuery(
                // Finance's DC report includes tool/company-asset models. They must
                // remain in this population so its complete DC total can reconcile.
                "SELECT id, model, item_name FROM tbl_item_master ORDER BY model, id");
        List<Object[]> items = new java.util.ArrayList<Object[]>(itemQuery.getResultList());
        java.util.HashSet<String> knownItemIds = new java.util.HashSet<String>();
        for (Object[] item : items) knownItemIds.add(stringValue(item[0]));
        for (MonthlyStockMovement previous : previousSnapshots) {
            if (knownItemIds.add(previous.getItemMasterId())) {
                items.add(new Object[] { previous.getItemMasterId(), previous.getModelNo(), previous.getDescription() });
            }
        }
        for (MonthlyStockMovement closing : approvedClosingSnapshots) {
            if (knownItemIds.add(closing.getItemMasterId())) {
                items.add(new Object[] { closing.getItemMasterId(), closing.getModelNo(), closing.getDescription() });
            }
        }

        Map<String, Map> rows = new LinkedHashMap<String, Map>();
        for (Object[] item : items) {
            String itemId = stringValue(item[0]);
            String model = stringValue(item[1]);
            String description = stringValue(item[2]);

            BigDecimal openQty = quantity(value(openingQty.get(itemId)));
            BigDecimal closeQty = quantity(value(closingQty.get(itemId)));
            GrnSummary inward = grn.get(itemId);
            BigDecimal inwardQty = quantity(inward == null ? ZERO : inward.quantity);
            BigDecimal averagePoPrice = moneyRate(positiveValue(averagePoPrices.get(itemId)));
            BigDecimal supplyPrice = positiveValue(latestSupplyPrices.get(itemId));
            BigDecimal latestGrnPrice = positiveValue(latestGrnPrices.get(itemId));
            // A positive approved Supply Price remains authoritative. A blank/zero
            // Supply Price must still follow Finance's agreed latest-GRN fallback.
            boolean hasApprovedClosingRate = approvedClosingRates.containsKey(itemId);
            // GRN/DC reconciliation keeps Finance's agreed transaction valuation.
            // An approved month-end rate applies only to closing stock; using it
            // for DC value would break the independent DC-by-date tie-out.
            BigDecimal valuationRate = averagePoPrice.signum() > 0 ? averagePoPrice
                    : (supplyPrice.signum() > 0 ? supplyPrice : latestGrnPrice);
            String priceSource = averagePoPrice.signum() > 0 ? "AVERAGE PO PRICE"
                    : (supplyPrice.signum() > 0 ? "SUPPLY PRICE"
                            : (latestGrnPrice.signum() > 0 ? "LATEST GRN PRICE" : "MISSING"));

            // The first read-only version derives the opening rate from the
            // latest prior GRN occurrence. Once a month is approved/frozen,
            // its persisted closing value/rate becomes the next opening.
            BigDecimal openingRate = openingRates.containsKey(itemId)
                    ? openingRates.get(itemId) : valuationRate;
            BigDecimal openingValue = openingValues.containsKey(itemId)
                    ? openingValues.get(itemId) : openQty.multiply(openingRate);
            BigDecimal inwardValue = inward == null ? ZERO : inward.amount;

            // Net outward deliberately includes every stock-affecting event.
            BigDecimal outwardQty = openQty.add(inwardQty).subtract(closeQty);
            DcSummary dcMovement = dcMovements.get(itemId);
            BigDecimal dcQty = quantity(dcMovement == null ? ZERO : dcMovement.quantity);
            // Finance validates DC activity independently. The difference is the
            // net of returns, assignments, corrections and DCs that did not post
            // to stock. Keeping it explicit makes the stock roll-forward auditable.
            BigDecimal adjustmentQty = quantity(outwardQty.subtract(dcQty));
            // Keep the DC value at the same transaction-level rounding used by
            // DC Report By Date.  Multiplying an already-aggregated quantity by
            // the rate can otherwise introduce a small value difference.
            BigDecimal reportedDcValue = dcMovement == null ? ZERO : dcMovement.amount;
            boolean hasApprovedClosing = hasApprovedClosingRate && approvedClosingValues.containsKey(itemId);
            // Once Finance has approved/imported a closing snapshot, its rate is
            // authoritative even when it is zero.
            BigDecimal closingValue = hasApprovedClosing
                    ? approvedClosingValues.get(itemId)
                    : closeQty.multiply(valuationRate);
            BigDecimal closingRate = hasApprovedClosing
                    ? approvedClosingRates.get(itemId)
                    : valuationRate;
            BigDecimal outwardValue = openingValue.add(inwardValue).subtract(closingValue);
            BigDecimal outwardRate = outwardQty.signum() == 0
                    ? ZERO
                    : outwardValue.divide(outwardQty, CALC_SCALE, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("itemMasterId", itemId);
            boolean priceMissingForActiveItem = valuationRate.signum() == 0
                    && (openQty.signum() != 0 || inwardQty.signum() != 0 || closeQty.signum() != 0);
            priceMissingForActiveItem = priceMissingForActiveItem
                    || (hasApprovedClosing && closeQty.signum() != 0 && closingRate.signum() == 0);
            row.put("particulars", itemId + " / " + model + " / " + description
                    + (priceMissingForActiveItem ? " / PRICE MISSING" : ""));
            row.put("openQ1", openQty);
            row.put("openR1", openingRate);
            row.put("openV1", openingValue);
            row.put("grnQ1", inwardQty);
            row.put("grnR1", valuationRate);
            row.put("grnV1", inwardValue);
            row.put("dcQ1", outwardQty);
            row.put("dcR1", outwardRate);
            row.put("dcV1", outwardValue);
            row.put("clQ1", closeQty);
            row.put("clR1", closingRate);
            row.put("clV1", closingValue);
            row.put("reportedDcQty", dcQty);
            row.put("reportedDcRate", valuationRate);
            row.put("reportedDcValue", reportedDcValue);
            row.put("adjustmentQty", adjustmentQty);
            row.put("movementReconciled", adjustmentQty.signum() == 0);
            row.put("grnOccurrences", inward == null ? 0L : inward.occurrences);
            row.put("priceSource", priceSource);
            row.put("missingPrice", priceMissingForActiveItem);
            rows.put(itemId, row);
        }

        Map<String, Map> result = new LinkedHashMap<String, Map>();
        result.put("grnlist", rows);
        result.put("nogrnlist", Collections.<String, Map>emptyMap());
        return result;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public int freeze(Timestamp fromInclusive, Timestamp toInclusive) {
        java.time.LocalDate closingDate = toInclusive.toLocalDateTime().toLocalDate();
        if (movementRepository.existsByReportDate(closingDate)) {
            throw new IllegalStateException("A frozen monthly stock snapshot already exists for " + closingDate);
        }
        Map<String, Map> report = generate(fromInclusive, toInclusive);
        Map<String, Map> rows = report.get("grnlist");
        java.util.ArrayList<MonthlyStockMovement> snapshots = new java.util.ArrayList<MonthlyStockMovement>();
        for (Map.Entry<String, Map> entry : rows.entrySet()) {
            Map row = entry.getValue();
            MonthlyStockMovement snapshot = new MonthlyStockMovement();
            snapshot.setItemMasterId(entry.getKey());
            snapshot.setReportDate(closingDate);
            Object particulars = row.get("particulars");
            snapshot.setDescription(particulars == null ? "" : String.valueOf(particulars));
            snapshot.setClosingQty(decimal(row.get("clQ1")));
            snapshot.setClosingRate(decimal(row.get("clR1")));
            snapshot.setClosingValue(decimal(row.get("clV1")).setScale(2, RoundingMode.HALF_UP));
            snapshot.setFrozen(true);
            snapshot.setSource("ERP MONTHLY STOCK REPORT");
            snapshot.setCreatedAt(java.time.LocalDateTime.now());
            snapshots.add(snapshot);
        }
        movementRepository.saveAll(snapshots);
        return snapshots.size();
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> loadStockSnapshot(long cutoffEpochMillis) {
        String sql = "SELECT a.item_master_id, COALESCE(SUM(a.quantity), 0) "
                + "FROM tbl_stock_aud a "
                + "JOIN (SELECT sa.stock_id, MAX(sa.rev) AS max_rev "
                + "      FROM tbl_stock_aud sa JOIN revinfo r ON r.rev = sa.rev "
                + "      WHERE r.revtstmp <= :cutoff GROUP BY sa.stock_id) latest "
                + "  ON latest.stock_id = a.stock_id AND latest.max_rev = a.rev "
                + "WHERE COALESCE(a.revtype, 0) <> 2 AND a.item_master_id IS NOT NULL "
                + "GROUP BY a.item_master_id";
        List<Object[]> values = entityManager.createNativeQuery(sql)
                .setParameter("cutoff", cutoffEpochMillis).getResultList();
        Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();
        for (Object[] row : values) result.put(stringValue(row[0]), decimal(row[1]));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> loadCurrentStock() {
        List<Object[]> values = entityManager.createNativeQuery(
                "SELECT item_master_id, COALESCE(SUM(quantity), 0) FROM tbl_stock "
                + "WHERE item_master_id IS NOT NULL GROUP BY item_master_id").getResultList();
        Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();
        for (Object[] row : values) result.put(stringValue(row[0]), decimal(row[1]));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, GrnSummary> loadGrnSummary(Timestamp fromInclusive, Timestamp toInclusive) {
        Map<String, BigDecimal> rates = loadAveragePoPrices();
        List<Object[]> values = entityManager.createNativeQuery(
                "SELECT pi.model_no, gi.received_quantity FROM tbl_grn_items gi "
                + "JOIN tbl_purchase_items pi ON pi.purchase_item_id = CAST(gi.po_item_id AS UNSIGNED) "
                + "JOIN tbl_item_master im ON im.id=pi.model_no "
                + "WHERE gi.updated >= :fromDate AND gi.updated <= :toDate "
                + "AND gi.po_item_id REGEXP '^[0-9]+$'")
                .setParameter("fromDate", fromInclusive).setParameter("toDate", toInclusive).getResultList();
        Map<String, GrnSummary> result = new LinkedHashMap<String, GrnSummary>();
        for (Object[] row : values) {
            String id=stringValue(row[0]); float qty=((Number)row[1]).floatValue();
            BigDecimal amount=StockReportMoney.amount(qty, rates.get(id));
            GrnSummary previous=result.get(id);
            result.put(id,new GrnSummary(StockReportMoney.source(qty).add(previous==null?ZERO:previous.quantity),
                    previous==null?1L:previous.occurrences+1L, amount.add(previous==null?ZERO:previous.amount)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, DcSummary> loadDcSummary(Timestamp fromInclusive, Timestamp toInclusive) {
        Map<String, BigDecimal> rates = loadAveragePoPrices();
        List<Object[]> values=entityManager.createNativeQuery(
                "SELECT di.item_id, dci.todays_qty FROM tbl_dc_items dci "
                + "JOIN sales_order_design sd ON sd.sales_item_id=dci.description "
                + "JOIN sales_order_design_items di ON di.design_id=sd.design_id "
                + "JOIN tbl_sales_item si ON si.id=dci.description "
                + "JOIN tbl_item_master im ON im.id=di.item_id "
                + "WHERE dci.created >= :fromDate AND dci.created <= :toDate "
                + "AND dci.todays_qty<>0 AND im.model IS NOT NULL AND im.model<>''")
                .setParameter("fromDate",fromInclusive).setParameter("toDate",toInclusive).getResultList();
        Map<String, DcSummary> result=new LinkedHashMap<String, DcSummary>();
        for(Object[] row:values){
            String id=stringValue(row[0]); if(!rates.containsKey(id))continue;
            float qty=((Number)row[1]).floatValue(); BigDecimal amount=StockReportMoney.amount(qty,rates.get(id));
            DcSummary previous=result.get(id);
            result.put(id,new DcSummary(StockReportMoney.source(qty).add(previous==null?ZERO:previous.quantity),
                    amount.add(previous==null?ZERO:previous.amount)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> loadLatestSupplyPrices() {
        String sql = "SELECT s.item_master_id, s.cost_price FROM tbl_supplier s "
                + "WHERE s.item_master_id IS NOT NULL AND s.supplier_id = ("
                + "  SELECT s2.supplier_id FROM tbl_supplier s2 "
                + "  WHERE s2.item_master_id = s.item_master_id "
                + "  ORDER BY s2.updated DESC, s2.supplier_id DESC LIMIT 1)";
        List<Object[]> values = entityManager.createNativeQuery(sql).getResultList();
        Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();
        for (Object[] row : values) result.put(stringValue(row[0]), decimal(row[1]));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> loadAveragePoPrices() {
        List<Object[]> values=entityManager.createNativeQuery(
                "SELECT model_no,unit_price FROM tbl_purchase_items WHERE model_no IS NOT NULL").getResultList();
        Map<String,BigDecimal> sums=new LinkedHashMap<String,BigDecimal>();
        Map<String,Integer> counts=new LinkedHashMap<String,Integer>();
        for(Object[] row:values){String id=stringValue(row[0]);
            BigDecimal value=StockReportMoney.source(((Number)row[1]).floatValue());
            sums.put(id,value.add(sums.containsKey(id)?sums.get(id):ZERO));
            counts.put(id,counts.containsKey(id)?counts.get(id)+1:1);
        }
        Map<String,BigDecimal> result=new LinkedHashMap<String,BigDecimal>();
        for(String id:sums.keySet())result.put(id,sums.get(id).divide(BigDecimal.valueOf(counts.get(id)),2,RoundingMode.HALF_UP));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> loadLastGrnRateBefore(Timestamp beforeExclusive) {
        String sql = "SELECT pi.model_no, gi.unit_price FROM tbl_grn_items gi "
                + "JOIN tbl_purchase_items pi ON pi.purchase_item_id = CAST(gi.po_item_id AS UNSIGNED) "
                + "JOIN (SELECT pi2.model_no, MAX(gi2.updated) max_updated "
                + "      FROM tbl_grn_items gi2 "
                + "      JOIN tbl_purchase_items pi2 ON pi2.purchase_item_id = CAST(gi2.po_item_id AS UNSIGNED) "
                + "      WHERE gi2.updated < :beforeDate AND gi2.received_quantity > 0 "
                + "      GROUP BY pi2.model_no) latest "
                + "  ON latest.model_no = pi.model_no AND latest.max_updated = gi.updated "
                + "WHERE gi.updated < :beforeDate AND gi.received_quantity > 0";
        List<Object[]> values = entityManager.createNativeQuery(sql)
                .setParameter("beforeDate", beforeExclusive).getResultList();
        Map<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();
        for (Object[] row : values) result.put(stringValue(row[0]), decimal(row[1]));
        return result;
    }

    private static BigDecimal value(BigDecimal input) {
        return input == null ? ZERO : input;
    }

    private static BigDecimal positiveValue(BigDecimal input) {
        BigDecimal result = value(input);
        return result.signum() > 0 ? result : ZERO;
    }

    private static BigDecimal quantity(BigDecimal input) {
        return value(input).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal moneyRate(BigDecimal input) {
        return value(input).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(Object input) {
        if (input == null) return ZERO;
        return new BigDecimal(String.valueOf(input));
    }

    private static String stringValue(Object input) {
        return input == null ? "" : String.valueOf(input).trim();
    }

    private static final class GrnSummary {
        private final BigDecimal quantity;
        private final long occurrences;
		private final BigDecimal amount;

		private GrnSummary(BigDecimal quantity, long occurrences, BigDecimal amount) {
            this.quantity = quantity;
            this.occurrences = occurrences;
			this.amount = amount;
        }
    }

    private static final class DcSummary {
        private final BigDecimal quantity;
        private final BigDecimal amount;

        private DcSummary(BigDecimal quantity, BigDecimal amount) {
            this.quantity = quantity;
            this.amount = amount;
        }
    }
}
