-- MySQL setup for MonthlyStockMovement. Run on a backed-up test database first.
-- Select the existing ERP database explicitly before running this script.
-- This creates a missing table only. It does NOT migrate an incompatible existing table.
CREATE TABLE IF NOT EXISTS tbl_monthly_stock_movement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    item_master_id VARCHAR(255) NOT NULL,
    report_date DATE NOT NULL,
    model_no VARCHAR(255) NULL,
    description VARCHAR(1000) NULL,
    closing_qty DECIMAL(19,6) NOT NULL,
    closing_rate DECIMAL(19,8) NOT NULL,
    closing_value DECIMAL(19,2) NOT NULL,
    frozen BIT(1) NOT NULL,
    source VARCHAR(100) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_monthly_stock_item_date UNIQUE (item_master_id, report_date)
) ENGINE=InnoDB;
SHOW CREATE TABLE tbl_monthly_stock_movement;
-- Read-only verification after importing the approved baseline:
SELECT report_date, COUNT(*) AS items, SUM(closing_qty) AS closing_quantity,
       SUM(closing_value) AS closing_value,
       SUM(CASE WHEN closing_qty < 0 THEN 1 ELSE 0 END) AS negative_closing_items,
       SUM(CASE WHEN closing_qty <> 0 AND closing_rate = 0 THEN 1 ELSE 0 END) AS unpriced_items
FROM tbl_monthly_stock_movement
GROUP BY report_date ORDER BY report_date;