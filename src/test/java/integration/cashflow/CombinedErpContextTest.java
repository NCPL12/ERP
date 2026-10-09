package integration.cashflow;
import com.ncpl.sales.cashflow.config.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("h2")
@SpringBootTest(classes=com.ncpl.sales.SalesApp.class, properties={
    "spring.datasource.url=jdbc:h2:mem:combinederp;DB_CLOSE_DELAY=-1;MODE=MySQL",
    "spring.datasource.username=sa", "spring.datasource.password=", "cashflow.tally.sync.enabled=false",
    "tallyUrl=http://127.0.0.1:1", "spring.main.allow-bean-definition-overriding=false"})
@Import(CombinedErpContextTest.DisableScheduledJobs.class)
class CombinedErpContextTest {
    @TestConfiguration static class DisableScheduledJobs {
        @Bean static org.springframework.beans.factory.config.BeanFactoryPostProcessor removeSchedulers() {
            return factory -> {
                if (factory instanceof org.springframework.beans.factory.support.BeanDefinitionRegistry) {
                    org.springframework.beans.factory.support.BeanDefinitionRegistry registry=(org.springframework.beans.factory.support.BeanDefinitionRegistry)factory;
                    String scheduler="org.springframework.context.annotation.internalScheduledAnnotationProcessor";
                    if(registry.containsBeanDefinition(scheduler)) registry.removeBeanDefinition(scheduler);
                }
            };
        }
    }
    @Autowired WebApplicationContext context;
    @Autowired com.ncpl.sales.security.UserRepo erpUsers;
    @org.junit.jupiter.api.BeforeEach void createEnabledErpAdmin() {
        if (erpUsers.findUserByUserName("admin") == null) {
            com.ncpl.sales.security.User admin = new com.ncpl.sales.security.User();
            admin.setUsername("admin"); admin.setPassword("test-only-not-for-login");
            admin.setRole("ADMIN"); admin.setEnabled(true); admin.setName("Test Administrator");
            erpUsers.save(admin);
        }
    }
    @Test void erpAndCashflowShareSecurityWithoutViewConflicts() throws Exception {
        org.springframework.test.web.servlet.MockMvc mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(get("/ncpl-sales/cashflow-analyzer/overview").contextPath("/ncpl-sales").with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk()).andExpect(view().name("cashflow/overview"));
        mvc.perform(get("/ncpl-sales/cashflow-analyzer/overview").contextPath("/ncpl-sales"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/ncpl-sales/login"));
        mvc.perform(get("/ncpl-sales/dashboard").contextPath("/ncpl-sales").with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk()).andExpect(view().name("dashboard"));
    }

    @org.springframework.boot.test.mock.mockito.MockBean
    com.ncpl.sales.cashflow.service.TallyLiveService tally;

    @Test void siteReportAcceptsHtmlDateFiltersAndExportsExcel() throws Exception {
        java.time.LocalDate from=java.time.LocalDate.of(2026,4,1), to=java.time.LocalDate.of(2026,9,18);
        com.ncpl.sales.cashflow.service.TallyLiveService.SiteProfitability report =
            new com.ncpl.sales.cashflow.service.TallyLiveService.SiteProfitability(
                "Test Site",from,to,1,100,150,50,150,100,50,33.33,
                java.util.Collections.emptyList(),java.util.Collections.emptyList());
        org.mockito.Mockito.when(tally.fetchSiteProfitability("Test Site",from,to)).thenReturn(report);
        org.springframework.test.web.servlet.MockMvc mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(get("/ncpl-sales/cashflow-analyzer/api/cashflow/tally/cost-centres/site-profitability")
            .contextPath("/ncpl-sales").param("site","Test Site").param("from","2026-04-01").param("to","2026-09-18")
            .with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.siteName").value("Test Site"))
            .andExpect(jsonPath("$.fromDate").value("2026-04-01")).andExpect(jsonPath("$.profit").value(50));
        mvc.perform(get("/ncpl-sales/cashflow-analyzer/api/cashflow/tally/cost-centres/site-report/export")
            .contextPath("/ncpl-sales").param("site","Test Site").param("from","2026-04-01").param("to","2026-09-18")
            .with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk()).andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }


    @Test
    @org.springframework.transaction.annotation.Transactional
    void financeCashPlanSummarizesTallyAndManualCashByMonth() throws Exception {
        com.ncpl.sales.cashflow.repository.InvoiceRepository invoices = context.getBean(com.ncpl.sales.cashflow.repository.InvoiceRepository.class);
        com.ncpl.sales.cashflow.repository.FinanceCashPlanEntryRepository entries = context.getBean(com.ncpl.sales.cashflow.repository.FinanceCashPlanEntryRepository.class);
        com.ncpl.sales.cashflow.repository.FinanceCashPlanSettingsRepository settings = context.getBean(com.ncpl.sales.cashflow.repository.FinanceCashPlanSettingsRepository.class);

        com.ncpl.sales.cashflow.entity.FinanceCashPlanSettingsEntity planSettings = new com.ncpl.sales.cashflow.entity.FinanceCashPlanSettingsEntity();
        planSettings.setId(1L);
        planSettings.setOpeningBalance(new java.math.BigDecimal("100"));
        planSettings.setMinimumCashReserve(new java.math.BigDecimal("50"));
        settings.save(planSettings);

        invoices.save(new com.ncpl.sales.cashflow.entity.InvoiceEntity("Customer A", "R-1", java.time.LocalDate.of(2026,1,1),
            java.time.LocalDate.of(2026,1,10), new java.math.BigDecimal("500"), com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.INFLOW, "TALLY"));
        invoices.save(new com.ncpl.sales.cashflow.entity.InvoiceEntity("Vendor A", "P-1", java.time.LocalDate.of(2026,2,1),
            java.time.LocalDate.of(2026,2,10), new java.math.BigDecimal("200"), com.ncpl.sales.cashflow.entity.InvoiceEntity.InvoiceType.OUTFLOW, "TALLY"));

        com.ncpl.sales.cashflow.entity.FinanceCashPlanEntryEntity sales = new com.ncpl.sales.cashflow.entity.FinanceCashPlanEntryEntity();
        sales.setEntryType("MANUAL_CASH_IN"); sales.setPartyName("Projected Sales"); sales.setPlannedDate(java.time.LocalDate.of(2026,1,15));
        sales.setAmount(new java.math.BigDecimal("300")); sales.setConfidencePercent(100); sales.setActive(true); entries.save(sales);
        com.ncpl.sales.cashflow.entity.FinanceCashPlanEntryEntity expense = new com.ncpl.sales.cashflow.entity.FinanceCashPlanEntryEntity();
        expense.setEntryType("MANUAL_CASH_OUT"); expense.setPartyName("Planned Purchase"); expense.setPlannedDate(java.time.LocalDate.of(2026,2,5));
        expense.setAmount(new java.math.BigDecimal("50")); expense.setConfidencePercent(100); expense.setActive(true); entries.save(expense);

        org.springframework.test.web.servlet.MockMvc mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(get("/ncpl-sales/cashflow-analyzer/finance-cash-plan").contextPath("/ncpl-sales")
            .with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk()).andExpect(view().name("cashflow/finance-cash-plan"));
        mvc.perform(get("/ncpl-sales/cashflow-analyzer/api/cashflow/finance-plan/summary").contextPath("/ncpl-sales")
            .param("startMonth", "2026-01").param("months", "2").with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk())
            
            .andExpect(jsonPath("$.periods[0].totalCashIn").value(800))
            .andExpect(jsonPath("$.periods[0].netFlow").value(800))
            
            .andExpect(jsonPath("$.periods[1].totalCashOut").value(250))
            .andExpect(jsonPath("$.periods[1].netFlow").value(-250));
    }
}

