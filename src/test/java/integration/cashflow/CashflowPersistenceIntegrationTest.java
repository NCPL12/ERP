package integration.cashflow;
import com.ncpl.sales.cashflow.config.*;

import com.ncpl.sales.cashflow.controller.*;
import com.ncpl.sales.cashflow.service.*;
import com.ncpl.sales.cashflow.model.Invoice;
import com.ncpl.sales.cashflow.entity.InvoiceEntity;
import com.ncpl.sales.cashflow.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDate;
import java.util.Arrays;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("cashflow-module-test")
@SpringBootTest(classes=CashflowPersistenceIntegrationTest.Config.class, properties={
    "spring.datasource.url=jdbc:h2:mem:cashflowmodule;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "spring.jpa.hibernate.ddl-auto=create-drop",
    "cashflow.tally.sync.enabled=true", "spring.main.allow-bean-definition-overriding=false"})
class CashflowPersistenceIntegrationTest {
    @Configuration @EnableAutoConfiguration @EnableWebSecurity
    @EntityScan(basePackageClasses=InvoiceEntity.class)
    @EnableJpaRepositories(basePackageClasses=InvoiceRepository.class)
    @Import({CashflowStandaloneViewAdvice.class,CashflowViewConfig.class,CashflowSecurityConfig.class,DashboardController.class,CashFlowApiController.class,
        ExcelReaderService.class,CashFlowAnalysisService.class,AnalyticsService.class,AgingAnalysisService.class,
        CustomerVendorAnalysisService.class,SessionDataService.class,InsightsService.class,ExcelExportService.class,
        TallySyncService.class,RecurringTransactionManagementService.class,RecurringTransactionService.class})
    static class Config {
        @Bean com.fasterxml.jackson.databind.ObjectMapper objectMapper() { return new com.fasterxml.jackson.databind.ObjectMapper(); }
    }
    @Autowired WebApplicationContext context;
    @Autowired InvoiceRepository repository;
    @MockBean TallyLiveService tally;
    @MockBean InvoicePipelineService invoicePipelineService;
    @Test void syncPersistsBillsAndReturnsIsoDatesWithoutChangingTally() throws Exception {
        java.util.TimeZone previousZone = java.util.TimeZone.getDefault();
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("EST"));
        try {
        when(tally.fetchOutstandingBills()).thenReturn(Arrays.asList(
            new Invoice("Customer", "INV-TEST-1",LocalDate.of(2026,1,1),LocalDate.of(2026,2,1),1200,Invoice.InvoiceType.INFLOW),
            new Invoice("Vendor", "PUR-TEST-1",LocalDate.of(2026,1,2),LocalDate.of(2026,2,2),500,Invoice.InvoiceType.OUTFLOW)));
        MockMvc mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(post("/ncpl-sales/cashflow-analyzer/api/cashflow/tally/sync").contextPath("/ncpl-sales")
            .with(user("admin").authorities(() -> "ADMIN")).with(csrf().asHeader()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status.lastSyncStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.status.invoicesSynced").value(2));
        assertEquals(2,repository.findBySource("TALLY").size());
        mvc.perform(get("/cashflow-analyzer/api/cashflow/tally/outstanding?type=receivables")
            .with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1)).andExpect(jsonPath("$.total").value(1200))
            .andExpect(jsonPath("$.rows[0].invoiceDate").value("2026-01-01"));
        mvc.perform(get("/cashflow-analyzer/api/cashflow/tally/overview").with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.hasData").value(true));
        verify(tally).fetchOutstandingBills();
        mvc.perform(get("/cashflow-analyzer/api/cashflow/tally/status").with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.syncStatus.lastSyncEndTime").value(org.hamcrest.Matchers.startsWith(
                java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Kolkata")).toString().substring(0,13))));
        } finally { java.util.TimeZone.setDefault(previousZone); }
    }
}

