package com.ncpl.sales.cashflow.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TallyLiveServiceSiteAccountingTest {
    private final TallyLiveService service = new TallyLiveService();

    @Test
    void resolvesCustomLedgerGroupsToTheirFinancialNature() {
        Map<String, String> parents = com.ncpl.sales.cashflow.util.Java8Collections.map(
                "PROJECT PURCHASES", "PURCHASE ACCOUNTS",
                "SITE OVERHEADS", "INDIRECT EXPENSES",
                "PROJECT SALES", "SALES ACCOUNTS");
        Map<String, String> names = com.ncpl.sales.cashflow.util.Java8Collections.map(
                "PROJECT PURCHASES", "Project Purchases",
                "PURCHASE ACCOUNTS", "Purchase Accounts",
                "SITE OVERHEADS", "Site Overheads",
                "INDIRECT EXPENSES", "Indirect Expenses",
                "PROJECT SALES", "Project Sales",
                "SALES ACCOUNTS", "Sales Accounts");

        assertEquals(TallyLiveService.FinancialNature.EXPENSE,
                service.resolveFinancialNature("Project Purchases", parents, names));
        assertEquals(TallyLiveService.FinancialNature.EXPENSE,
                service.resolveFinancialNature("Site Overheads", parents, names));
        assertEquals(TallyLiveService.FinancialNature.REVENUE,
                service.resolveFinancialNature("Project Sales", parents, names));
    }

    @Test
    void preservesCreditAndDebitAdjustmentsInsteadOfUsingAbsoluteValues() {
        assertEquals(1000d, TallyLiveService.toReportAmount(TallyLiveService.FinancialNature.REVENUE, 1000d));
        assertEquals(-100d, TallyLiveService.toReportAmount(TallyLiveService.FinancialNature.REVENUE, -100d));
        assertEquals(1000d, TallyLiveService.toReportAmount(TallyLiveService.FinancialNature.EXPENSE, -1000d));
        assertEquals(-100d, TallyLiveService.toReportAmount(TallyLiveService.FinancialNature.EXPENSE, 100d));
        assertEquals(0d, TallyLiveService.toReportAmount(TallyLiveService.FinancialNature.NON_REVENUE, 1000d));
    }
}
