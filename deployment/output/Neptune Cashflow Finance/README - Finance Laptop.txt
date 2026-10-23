NEPTUNE CASHFLOW ANALYZER - FINANCE LAPTOP
==========================================

INSTALLATION
1. Extract the full "Neptune Cashflow Finance" folder to C:\Neptune Cashflow Finance.
2. Open TallyPrime on this same laptop and load the required company.
3. Ensure TallyPrime HTTP/XML connectivity is enabled on port 9000.
4. Double-click "Install On This Laptop.cmd" once.
5. The setup window creates the desktop icon and asks you to create the application administrator username and password.
6. The login page opens automatically after setup.
7. For later use, double-click the "Neptune Cashflow" desktop icon.

DATABASE
- The database is embedded in the application.
- Finance does not need MySQL, an SQL login, Maven, Eclipse, or a separate Java installation.
- Application data is stored in data\cashflow_db.mv.db.
- Use "Backup Finance Data.cmd" after stopping the application.

TALLY
- TallyPrime is on the configured network client at `http://192.168.0.183:9000`.
- Configured company: `Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)`.
- Keep that company loaded in TallyPrime and ensure the client is reachable from the Finance laptop.
- Automatic refresh runs every 60 seconds; Sync Now is also available.

ERP-LINKED INVOICE PIPELINE
- Tally-only cash flow, receivables, payables, aging, voucher details and Finance Cash Plan work locally.
- Invoice Pipeline also opens locally and checks Tally Purchase vouchers.
- Its GRN, PO, SO and client-invoice rows come from ERP operational data. In this portable local database those ERP row counts are zero; use the integrated ERP version for the complete linked pipeline.

FILES
- Logs: logs\application.log
- Exports download through the browser window.
- Keep the entire application folder together.
