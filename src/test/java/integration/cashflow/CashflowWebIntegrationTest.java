package integration.cashflow;
import com.ncpl.sales.cashflow.config.*;

import com.ncpl.sales.cashflow.controller.DashboardController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes=CashflowWebIntegrationTest.Config.class)
class CashflowWebIntegrationTest {
    @org.springframework.boot.test.context.TestConfiguration @EnableWebMvc @EnableWebSecurity
    @Import({CashflowStandaloneViewAdvice.class,CashflowViewConfig.class, CashflowSecurityConfig.class, DashboardController.class, Probe.class})
    static class Config {}
    @RestController static class Probe {
        @PostMapping("/cashflow-analyzer/api/probe") public String post() { return "accepted"; }
    }
    @Autowired WebApplicationContext context;
    MockMvc mvc;
    @BeforeEach void setup() { mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }
    @Test void anonymousRequiresErpLogin() throws Exception {
        mvc.perform(get("/ncpl-sales/cashflow-analyzer/overview").contextPath("/ncpl-sales"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/ncpl-sales/login"));
    }
    @Test void purchaseUserCannotAccessFinance() throws Exception {
        mvc.perform(get("/cashflow-analyzer/overview").with(user("buyer").authorities(() -> "PURCHASE")))
            .andExpect(status().isForbidden());
    }
    @Test void erpAdminCanRenderAllFinancePages() throws Exception {
        for(String route:new String[]{"overview","receivables","payables","advances","cost-centres","aging","reconciliation",
                "analytics","cashflow-forecast","due-date-tracker","customer-vendor","top-performers","debtors-creditors","recurring-transactions"}) {
            mvc.perform(get("/ncpl-sales/cashflow-analyzer/"+route).contextPath("/ncpl-sales")
                .with(user("admin").authorities(() -> "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/ncpl-sales/cashflow-analyzer/")));
        }
        mvc.perform(get("/cashflow-analyzer/aging-analysis").with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cashflow-analyzer/aging"));
    }
    @Test void mutationsRequireCsrfWithErpAdminSession() throws Exception {
        mvc.perform(post("/cashflow-analyzer/api/probe").with(user("admin").authorities(() -> "ADMIN")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/cashflow-analyzer/api/probe").with(user("admin").authorities(() -> "ADMIN")).with(csrf().asHeader()))
            .andExpect(status().isOk());
    }
}

