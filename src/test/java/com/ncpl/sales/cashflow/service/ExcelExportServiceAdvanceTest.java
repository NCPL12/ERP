package com.ncpl.sales.cashflow.service;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExcelExportServiceAdvanceTest {

    @Test
    void exportsEachPendingAdvanceReferenceWithFinancialContext() throws Exception {
        TallyLiveService.AdvanceBalance advance = new TallyLiveService.AdvanceBalance(
                "Example Customer", "Sundry Debtors", 1500d, 900d,
                TallyLiveService.AdvanceType.CUSTOMER_ADVANCE_RECEIVED,
                "Tally pending bills marked ISADVANCE", 2,
                com.ncpl.sales.cashflow.util.Java8Collections.list(
                        new TallyLiveService.AdvanceReference("ADV-1", LocalDate.of(2026, 8, 1), 1000d),
                        new TallyLiveService.AdvanceReference("ADV-2", LocalDate.of(2026, 8, 2), 500d)));

        byte[] bytes = new ExcelExportService().exportAdvances(com.ncpl.sales.cashflow.util.Java8Collections.list(advance), "Customer Advances Received");
        try (org.apache.poi.ss.usermodel.Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("Advances");
            assertNotNull(sheet);
            assertEquals("Advance Reference", sheet.getRow(2).getCell(3).getStringCellValue());
            assertEquals("ADV-1", sheet.getRow(3).getCell(3).getStringCellValue());
            assertEquals(1000d, sheet.getRow(3).getCell(5).getNumericCellValue());
            assertEquals("ADV-2", sheet.getRow(4).getCell(3).getStringCellValue());
        }
    }

    @Test
    void exportsOnlySiteReportSheetsSupportedByTallyRows() throws Exception {
        TallyLiveService.SiteProfitability report = new TallyLiveService.SiteProfitability(
                "JSS Site", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 8, 26),
                2, 6000d, 6800d, 800d, 1000d, 300d, 700d, 0.7d,
                com.ncpl.sales.cashflow.util.Java8Collections.list(new TallyLiveService.SitePnlLine("Sales Accounts", 0d, 1000d, 1000d),
                        new TallyLiveService.SitePnlLine("Purchase Accounts", 300d, 0d, 300d)),
                com.ncpl.sales.cashflow.util.Java8Collections.list(
                        new TallyLiveService.SiteAllocation(LocalDate.of(2026, 8, 1), "Sales", "S-1", "",
                                "Customer", "Sales Ledger", "JSS-SALES", "REVENUE", "REVENUE", 1000d, ""),
                        new TallyLiveService.SiteAllocation(LocalDate.of(2026, 8, 2), "Purchase", "P-1", "",
                                "Vendor", "Purchase Ledger", "JSS-BILLABLE", "EXPENSE", "BILLABLE", 300d, "")));

        byte[] bytes = new ExcelExportService().exportSiteReport(report);
        try (org.apache.poi.ss.usermodel.Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertNotNull(workbook.getSheet("P&L"));
            assertNotNull(workbook.getSheet("Costs"));
            assertNotNull(workbook.getSheet("Sales"));
            assertNull(workbook.getSheet("Salary"));
            assertEquals("Customer", workbook.getSheet("Sales").getRow(1).getCell(3).getStringCellValue());
        }
    }
}
