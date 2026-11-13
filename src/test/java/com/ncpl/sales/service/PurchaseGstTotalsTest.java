package com.ncpl.sales.service;

import com.ncpl.sales.repository.PurchaseItemRepo;
import com.ncpl.sales.repository.PurchaseRepo;
import java.sql.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import static org.assertj.core.api.Assertions.*;

class PurchaseGstTotalsTest {
    @Test void allDashboardQueriesKeepSavedRatesAfterMasterChanges() throws Exception {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:po_gst;MODE=MySQL"); Statement sql = db.createStatement()) {
            sql.execute("CREATE TABLE tbl_purchase_order(po_number VARCHAR(30), party_id VARCHAR(30), created TIMESTAMP, version INT, archive INT)");
            sql.execute("CREATE TABLE tbl_party(id VARCHAR(30), party_name VARCHAR(30), city_id INT)");
            sql.execute("CREATE TABLE city(id INT, name VARCHAR(30))");
            sql.execute("CREATE TABLE tbl_item_master(id VARCHAR(30), gst INT)");
            sql.execute("CREATE TABLE tbl_purchase_items(po_number VARCHAR(30), model_no VARCHAR(30), amount DECIMAL(12,2), gst_rate DECIMAL(5,2))");
            sql.execute("INSERT INTO tbl_party VALUES ('V','Vendor',1)");
            sql.execute("INSERT INTO city VALUES (1,'City')");
            sql.execute("INSERT INTO tbl_item_master VALUES ('ITEM',18)");
            for (int archive=0; archive<2; archive++) {
                sql.execute("INSERT INTO tbl_purchase_order VALUES ('PO"+archive+"','V',CURRENT_TIMESTAMP,1,"+archive+")");
                for (String rate : new String[]{"18.00", "0.00", "0.25", "NULL"})
                    sql.execute("INSERT INTO tbl_purchase_items VALUES ('PO"+archive+"','ITEM',100,"+rate+")");
            }
            sql.execute("UPDATE tbl_item_master SET gst=12");
            for (String method : new String[]{"getActivePoTotals", "getArchivedPoTotals"}) {
                String query = PurchaseItemRepo.class.getMethod(method).getAnnotation(Query.class).value();
                try (ResultSet rows = sql.executeQuery(query)) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getBigDecimal("total")).isEqualByComparingTo("400");
                    assertThat(rows.getBigDecimal("gstTotal")).isEqualByComparingTo("30.25");
                    assertThat(rows.next()).isFalse();
                }
            }
            String query = PurchaseRepo.class.getMethod("findDashboardData").getAnnotation(Query.class).value();
            try (ResultSet rows = sql.executeQuery(query)) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getBigDecimal("gst_total")).isEqualByComparingTo("30.25");
                assertThat(rows.next()).isFalse();
            }
        }
    }
}
