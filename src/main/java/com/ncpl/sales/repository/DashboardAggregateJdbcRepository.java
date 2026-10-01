package com.ncpl.sales.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.DashboardCountDto;

/**
 * Fetches all dashboard totals in one DB round-trip (MySQL). For large datasets,
 * add indexes on join columns (e.g. tbl_dc.so_number, tbl_grn.po_number, tbl_sales_order.archive).
 */
@Repository
public class DashboardAggregateJdbcRepository {

    /**
     * One row, all scalar subqueries — same semantics as {@link SalesRepo} / {@link PurchaseRepo} count methods.
     */
    private static final String ALL_COUNTS_SQL = ""
            + "SELECT "
            + "  (SELECT COUNT(*) FROM tbl_sales_order so WHERE so.archive = 0 "
            + "     AND NOT EXISTS (SELECT 1 FROM tbl_dc d WHERE d.so_number = so.id)) "
            + "  + (SELECT COUNT(DISTINCT so.id) FROM tbl_sales_order so WHERE so.archive = 0 "
            + "     AND EXISTS (SELECT 1 FROM tbl_dc d WHERE d.so_number = so.id) "
            + "     AND (SELECT SUM(si.quantity) FROM tbl_sales_item si WHERE si.sales_order_id = so.id) > "
            + "         (SELECT COALESCE(SUM(di.todays_qty), 0) FROM tbl_dc d "
            + "          JOIN tbl_dc_items di ON d.dc_id = di.dc_id WHERE d.so_number = so.id)) "
            + "  AS sales_order_count, "
            + "  (SELECT COUNT(*) FROM tbl_purchase_order po WHERE po.archive = 0 "
            + "     AND NOT EXISTS (SELECT 1 FROM tbl_grn g WHERE g.po_number = po.po_number)) "
            + "  + (SELECT COUNT(DISTINCT po.po_number) FROM tbl_purchase_order po WHERE po.archive = 0 "
            + "     AND EXISTS (SELECT 1 FROM tbl_grn g WHERE g.po_number = po.po_number) "
            + "     AND (SELECT SUM(pi.quantity) FROM tbl_purchase_items pi WHERE pi.po_number = po.po_number) > "
            + "         (SELECT COALESCE(SUM(gi.received_quantity), 0) FROM tbl_grn g "
            + "          JOIN tbl_grn_items gi ON g.grn_id = gi.grn_id WHERE g.po_number = po.po_number)) "
            + "  AS purchase_order_count, "
            + "  (SELECT COUNT(*) FROM tbl_invoice) AS invoice_count, "
            + "  (SELECT COUNT(*) FROM tbl_tds_items td "
            + "     JOIN tbl_sales_item si ON td.description = si.id "
            + "     JOIN tbl_sales_order so ON si.sales_order_id = so.id "
            + "     WHERE td.tds_approved = 1 AND td.site_quantity > 0 "
            + "       AND NOT EXISTS (SELECT 1 FROM tbl_purchase_items pi WHERE pi.sales_item_id = si.id AND pi.model_no = td.model_number) "
            + "       AND NOT EXISTS (SELECT 1 FROM tbl_dc d WHERE d.so_number = so.id) "
            + "       AND so.archive = 0) AS tds_items_count, "
            + "  (SELECT COUNT(*) FROM tbl_sales_order WHERE archive = 0) AS active_sales_count, "
            + "  (SELECT COUNT(*) FROM tbl_work_order) AS work_order_count, "
            + "  (SELECT COUNT(DISTINCT so.id) FROM tbl_sales_order so WHERE so.id IN ( "
            + "       SELECT si.sales_order_id FROM tbl_sales_item si WHERE si.id NOT IN ( "
            + "         SELECT d.sales_item_id FROM sales_order_design d) "
            + "       AND si.units_id <> 20 AND si.quantity <> 0 AND si.unit_price > 0) "
            + "     AND so.archive = 0) AS sowithout_design_count, "
            + "  (SELECT COUNT(DISTINCT so.id) FROM tbl_sales_order so "
            + "     JOIN tbl_sales_item si ON si.sales_order_id = so.id "
            + "     JOIN sales_order_design d ON d.sales_item_id = si.id "
            + "     JOIN sales_order_design_items dit ON dit.design_id = d.design_id "
            + "     LEFT JOIN tbl_purchase_items pi ON pi.sales_item_id = d.sales_item_id AND pi.model_no = dit.item_id "
            + "     WHERE pi.purchase_item_id IS NULL AND si.units_id <> 20 AND si.quantity <> 0 "
            + "       AND si.unit_price > 0 AND so.archive = 0) AS sowith_design_count,"
            + "  (SELECT COUNT(*) FROM tbl_tds_items td "
            + "     JOIN tbl_sales_item si ON td.description = si.id "
            + "     JOIN tbl_sales_order so ON si.sales_order_id = so.id "
            + "     WHERE td.tds_approved = 1 "
            + "       AND NOT EXISTS (SELECT 1 FROM tbl_purchase_items pi WHERE pi.sales_item_id = si.id AND pi.model_no = td.model_number) "
            + "       AND NOT EXISTS (SELECT 1 FROM tbl_dc d WHERE d.so_number = so.id) "
            + "       AND so.archive = 0) AS tds_approved_pending_count";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DashboardCountDto fetchAllCounts() {
        return jdbcTemplate.queryForObject(ALL_COUNTS_SQL, (rs, rowNum) -> {
            DashboardCountDto dto = new DashboardCountDto();
            dto.setSalesOrderCount(rs.getLong("sales_order_count"));
            dto.setPurchaseOrderCount(rs.getLong("purchase_order_count"));
            dto.setInvoiceCount(rs.getLong("invoice_count"));
            dto.setTdsItemsCount(rs.getLong("tds_items_count"));
            dto.setSowithoutDesignCount(rs.getLong("sowithout_design_count"));
            dto.setSowithDesignCount(rs.getLong("sowith_design_count"));
            long activeSales = rs.getLong("active_sales_count");
            long workOrders = rs.getLong("work_order_count");
            dto.setProjectPreviewCount(workOrders + activeSales);
            dto.setTdsApprovedPendingCount(rs.getLong("tds_approved_pending_count"));
            return dto;
        });
    }
}
