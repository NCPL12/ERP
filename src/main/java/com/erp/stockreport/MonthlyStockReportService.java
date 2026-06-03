package com.erp.stockreport;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MonthlyStockReportService {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/may26?sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Pass@123";

    public static void main(String[] args) {
        MonthlyStockReportService service = new MonthlyStockReportService();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (args.length == 0) {
            LocalDate today = LocalDate.now();
            LocalDate from = today.withDayOfMonth(1);
            service.generateMonthlyStockReport(from, today);
        } else if (args.length == 2) {
            try {
                LocalDate from = LocalDate.parse(args[0], fmt);
                LocalDate to = LocalDate.parse(args[1], fmt);
                service.generateMonthlyStockReport(from, to);
            } catch (Exception e) {
                System.out.println("Invalid date format. Use dd-MM-yyyy");
                System.out.println("Usage: java com.erp.stockreport.MonthlyStockReportService [fromDate toDate]");
                System.out.println("Example: java com.erp.stockreport.MonthlyStockReportService 01-05-2026 27-05-2026");
                return;
            }
        } else {
            System.out.println("Usage: java com.erp.stockreport.MonthlyStockReportService [fromDate toDate]");
            System.out.println("Example: java com.erp.stockreport.MonthlyStockReportService 01-05-2026 27-05-2026");
            return;
        }
    }

    public void generateMonthlyStockReport(LocalDate fromDate, LocalDate toDate) {

        Connection connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            createTableIfNotExists(connection);

            String monthStartStr = fromDate.toString() + " 00:00:00";
            String todayEndStr = toDate.toString() + " 23:59:59";

            clearByDateRange(connection, fromDate, toDate);

            System.out.println("Generating report from " + fromDate + " to " + toDate);

            String itemQuery = "SELECT id, model FROM tbl_item_master";
            PreparedStatement itemStmt = connection.prepareStatement(itemQuery);
            ResultSet itemRs = itemStmt.executeQuery();

            while (itemRs.next()) {

                String itemId = itemRs.getString("id");
                String modelNo = itemRs.getString("model");

                double currentStock = getCurrentStock(connection, itemId);
                double supplyPrice = getSupplyPrice(connection, itemId);

                double dcQty = getDCQuantity(connection, modelNo, monthStartStr, todayEndStr);
                double grnQty = getGRNQuantity(connection, modelNo, monthStartStr, todayEndStr);

                double outstandingQty = currentStock + dcQty - grnQty;
                double outstandingValue = outstandingQty * supplyPrice;

                insertMonthlyReport(connection, itemId, outstandingValue, toDate);

                System.out.println("Model No : " + modelNo +
                        " | Current Stock : " + currentStock +
                        " | DC Qty : " + dcQty +
                        " | GRN Qty : " + grnQty +
                        " | Supply Price : " + supplyPrice +
                        " | Outstanding Value : " + outstandingValue);
            }

            System.out.println("Monthly stock report generated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createTableIfNotExists(Connection connection) throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS tbl_monthly_report_stock (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "item_master_id VARCHAR(255) NOT NULL, " +
                "report_date DATE NOT NULL, " +
                "outstanding_value DECIMAL(18,2) DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_monthly_report_item FOREIGN KEY (item_master_id) REFERENCES tbl_item_master(id)" +
                ")";
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
        stmt.close();
        System.out.println("Table tbl_monthly_report_stock verified/created.");
    }

    private void clearByDateRange(Connection connection, LocalDate fromDate, LocalDate toDate) throws Exception {
        String sql = "DELETE FROM tbl_monthly_report_stock WHERE report_date >= ? AND report_date <= ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setDate(1, Date.valueOf(fromDate));
        stmt.setDate(2, Date.valueOf(toDate));
        stmt.executeUpdate();
        stmt.close();
    }

    private double getCurrentStock(Connection connection, String itemId) {
        double stockQty = 0;
        try {
            String stockQuery = "SELECT COALESCE(SUM(quantity), 0) AS total_stock FROM tbl_stock WHERE item_master_id = ?";
            PreparedStatement stmt = connection.prepareStatement(stockQuery);
            stmt.setString(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stockQty = rs.getDouble("total_stock");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockQty;
    }

    private double getDCQuantity(Connection connection, String modelNo, String fromDate, String toDate) {
        double dcQty = 0;
        try {
            String dcQuery = "SELECT COALESCE(SUM(todays_qty), 0) AS total_dc " +
                    "FROM tbl_dc_items WHERE so_model_no = ? " +
                    "AND updated >= ? AND updated <= ?";
            PreparedStatement stmt = connection.prepareStatement(dcQuery);
            stmt.setString(1, modelNo);
            stmt.setString(2, fromDate);
            stmt.setString(3, toDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                dcQty = rs.getDouble("total_dc");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dcQty;
    }

    private double getGRNQuantity(Connection connection, String modelNo, String fromDate, String toDate) {
        double grnQty = 0;
        try {
            String grnQuery = "SELECT COALESCE(SUM(gi.received_quantity), 0) AS total_grn " +
                    "FROM tbl_grn_items gi " +
                    "JOIN tbl_purchase_items pi ON gi.po_item_id = pi.purchase_item_id " +
                    "WHERE pi.model_no = ? " +
                    "AND gi.updated >= ? AND gi.updated <= ?";
            PreparedStatement stmt = connection.prepareStatement(grnQuery);
            stmt.setString(1, modelNo);
            stmt.setString(2, fromDate);
            stmt.setString(3, toDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                grnQty = rs.getDouble("total_grn");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return grnQty;
    }

    private double getSupplyPrice(Connection connection, String itemId) {
        double supplyPrice = 0;
        try {
            String sql = "SELECT cost_price FROM tbl_supplier " +
                    "WHERE item_master_id = ? " +
                    "ORDER BY CASE WHEN preferred = 'yes' THEN 0 ELSE 1 END " +
                    "LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                supplyPrice = rs.getDouble("cost_price");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return supplyPrice;
    }

    private void insertMonthlyReport(Connection connection, String itemId, double outstandingStock, LocalDate today) {
        try {
            String insertQuery = "INSERT INTO tbl_monthly_report_stock " +
                    "(item_master_id, report_date, outstanding_value) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(insertQuery);
            stmt.setString(1, itemId);
            stmt.setDate(2, Date.valueOf(today));
            stmt.setDouble(3, outstandingStock);
            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}