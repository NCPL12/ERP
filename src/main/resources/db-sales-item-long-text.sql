-- Run before deploying to an existing MySQL database: Hibernate schema update
-- does not reliably widen existing VARCHAR columns.
ALTER TABLE tbl_sales_item MODIFY COLUMN description LONGTEXT NULL;
ALTER TABLE tbl_sales_item MODIFY COLUMN model_no LONGTEXT NULL;
