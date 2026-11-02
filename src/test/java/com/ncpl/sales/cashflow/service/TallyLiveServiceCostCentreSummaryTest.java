package com.ncpl.sales.cashflow.service;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TallyLiveServiceCostCentreSummaryTest {
    @Test
    void nativeCostCentreReportsUseTheCurrentlyOpenCompanyWhenNoCompanyIsConfigured() throws Exception {
        TallyLiveService service = new TallyLiveService();
        setCompanyName(service, "");

        String summary = service.buildCostCentreSummaryRequest(
                "SITE A", LocalDate.of(2024, 4, 1), LocalDate.of(2026, 9, 26));
        String breakup = service.buildNativeCostCentreReportRequest(
                "Cost Centre Breakup", "SITE A", LocalDate.of(2024, 4, 1), LocalDate.of(2026, 9, 26));

        assertFalse(summary.contains("<SVCURRENTCOMPANY>"));
        assertFalse(breakup.contains("<SVCURRENTCOMPANY>"));
    }

    @Test
    void nativeCostCentreReportsIncludeAndEscapeAnExplicitCompany() throws Exception {
        TallyLiveService service = new TallyLiveService();
        setCompanyName(service, "Neptune & Controls");

        String summary = service.buildCostCentreSummaryRequest(
                "SITE A", LocalDate.of(2024, 4, 1), LocalDate.of(2026, 9, 26));

        assertTrue(summary.contains("<SVCURRENTCOMPANY>Neptune &amp; Controls</SVCURRENTCOMPANY>"));
    }

    @Test
    void sumsNativeTallyCostCentreDebitCreditAndClosingAmounts() throws Exception {
        String xml = "<ENVELOPE>"
                + "<DSPACCINFO><DSPDRAMT><DSPDRAMTA>-30389844.40</DSPDRAMTA></DSPDRAMT>"
                + "<DSPCRAMT><DSPCRAMTA>28019227.60</DSPCRAMTA></DSPCRAMT>"
                + "<DSPCLAMT><DSPCLAMTA>-2370616.80</DSPCLAMTA></DSPCLAMT></DSPACCINFO>"
                + "<DSPACCINFO><DSPDRAMT><DSPDRAMTA>-30249749.75</DSPDRAMTA></DSPDRAMT>"
                + "<DSPCRAMT><DSPCRAMTA>40111452.53</DSPCRAMTA></DSPCRAMT>"
                + "<DSPCLAMT><DSPCLAMTA>9861702.78</DSPCLAMTA></DSPCLAMT></DSPACCINFO>"
                + "</ENVELOPE>";
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        TallyLiveService.NativeCostCentreSummary result = new TallyLiveService().summarizeNativeCostCentre(document);

        assertEquals(60639594.15d, result.debitTransactions(), 0.001d);
        assertEquals(68130680.13d, result.creditTransactions(), 0.001d);
        assertEquals(7491085.98d, result.closingBalance(), 0.001d);
    }

    @Test
    void calculatesPnlFromNativeCostCentreBreakupForTheWholeSelectedPeriod() throws Exception {
        String xml = "<ENVELOPE>"
                + account("Sales Accounts", -1048.95, 67765413.93, 67764364.98)
                + account("Purchase Accounts", -55244905.99, 6156.00, -55238749.99)
                + account("Direct Expenses", -4608564.71, 132790.00, -4475774.71)
                + account("Indirect Expenses", -768074.50, 4500.00, -763574.50)
                + account("Current Assets", -17000.00, 0.00, -17000.00)
                + "</ENVELOPE>";
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        TallyLiveService.NativePnlSummary result = new TallyLiveService().summarizeNativeCostCentrePnl(document);

        assertEquals(5, result.lines().size());
        assertEquals("Sales Accounts", result.lines().get(0).particular());
        assertEquals(1048.95d, result.lines().get(0).debit(), 0.001d);
        assertEquals(67765413.93d, result.lines().get(0).credit(), 0.001d);
        assertEquals(67764364.98d, result.revenue(), 0.001d);
        assertEquals(60478099.20d, result.expense(), 0.001d);
    }

    @Test
    void readsHistoricalRowsFromNativeCostCentreVoucherReport() throws Exception {
        String xml = "<ENVELOPE><DSPVCHDATE>21-Aug-25</DSPVCHDATE>"
                + "<DSPVCHLEDACCOUNT>State Purchase @ 18%</DSPVCHLEDACCOUNT>"
                + "<DSPVCHTYPE>Purchase</DSPVCHTYPE>"
                + "<DSPVCHDRAMT>-224400.00</DSPVCHDRAMT><DSPVCHCRAMT></DSPVCHCRAMT></ENVELOPE>";
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        java.util.List<TallyLiveService.SiteAllocation> rows = new ArrayList<TallyLiveService.SiteAllocation>();

        new TallyLiveService().collectNativeCostCentreVoucherRows(document, "BGS-CCTV-BILLABLE",
                "BILLABLE", com.ncpl.sales.cashflow.util.Java8Collections.map("STATE PURCHASE @ 18%", TallyLiveService.FinancialNature.EXPENSE), rows);

        assertEquals(1, rows.size());
        assertEquals(LocalDate.of(2025, 8, 21), rows.get(0).voucherDate());
        assertEquals("State Purchase @ 18%", rows.get(0).ledgerName());
        assertEquals("BILLABLE", rows.get(0).classification());
        assertEquals(224400d, rows.get(0).amount(), 0.001d);
    }

    private String account(String name, double debit, double credit, double closing) {
        return "<DSPACCNAME><DSPDISPNAME>" + name + "</DSPDISPNAME></DSPACCNAME>"
                + "<DSPACCINFO><DSPDRAMT><DSPDRAMTA>" + java.math.BigDecimal.valueOf(debit).toPlainString() + "</DSPDRAMTA></DSPDRAMT>"
                + "<DSPCRAMT><DSPCRAMTA>" + java.math.BigDecimal.valueOf(credit).toPlainString() + "</DSPCRAMTA></DSPCRAMT>"
                + "<DSPCLAMT><DSPCLAMTA>" + java.math.BigDecimal.valueOf(closing).toPlainString() + "</DSPCLAMTA></DSPCLAMT></DSPACCINFO>";
    }

    private void setCompanyName(TallyLiveService service, String value) throws Exception {
        java.lang.reflect.Field field = TallyLiveService.class.getDeclaredField("companyName");
        field.setAccessible(true);
        field.set(service, value);
    }
}
