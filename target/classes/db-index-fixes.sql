-- =============================================================
-- Performance Fix: Indexes for DC Report By Date
-- Run these against your production database
-- =============================================================

-- Index for date-range queries on tbl_dc_items (DC Report By Date filters on created)
CREATE INDEX IF NOT EXISTS idx_dc_items_created ON tbl_dc_items (created);

-- Index for description lookups (SalesItemId joins in N+1 queries)
CREATE INDEX IF NOT EXISTS idx_dc_items_description ON tbl_dc_items (description);

-- Composite index for the most common query pattern: date range + description
CREATE INDEX IF NOT EXISTS idx_dc_items_created_desc ON tbl_dc_items (created, description);

-- Index for so_number lookups on tbl_dc (used in getDcListBySoId)
CREATE INDEX IF NOT EXISTS idx_dc_so_number ON tbl_dc (so_number);

-- =============================================================
-- Fix: action_performed timezone issue in audit table
-- MySQL TIMESTAMP converts to/from UTC, causing wrong time.
-- DATETIME stores values as-is, preserving the correct time.
-- =============================================================
ALTER TABLE tbl_sales_order_audit MODIFY action_performed DATETIME NOT NULL;
