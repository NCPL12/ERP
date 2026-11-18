package com.ncpl.sales.cashflow.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
@Configuration @Order(1)
public class CashflowSecurityConfig extends WebSecurityConfigurerAdapter {
    @org.springframework.beans.factory.annotation.Autowired(required = false) // absent only in trimmed test contexts
    private com.ncpl.sales.security.UserStatusFilter userStatusFilter;

    @Override protected void configure(HttpSecurity http) throws Exception {
        // Same as the main chain: kicks out disabled users and refreshes role permissions per request.
        if (userStatusFilter != null) http.addFilterAfter(userStatusFilter, org.springframework.security.web.context.SecurityContextPersistenceFilter.class);
        http.antMatcher("/cashflow-analyzer/**")
            .authorizeRequests().anyRequest().hasAnyAuthority("ADMIN", "SUPER ADMIN", "CASHFLOW_ANALYZER_VIEW")
            .and().headers().frameOptions().sameOrigin()
            .and().csrf()
            .and().exceptionHandling().authenticationEntryPoint((request,response,error) ->
                response.sendRedirect(request.getContextPath() + "/login"));
    }
}
