package com.ncpl.sales.cashflow.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
@Configuration @Order(1)
public class CashflowSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/cashflow-analyzer/**")
            .authorizeRequests().anyRequest().hasAnyAuthority("ADMIN", "SUPER ADMIN")
            .and().csrf()
            .and().exceptionHandling().authenticationEntryPoint((request,response,error) ->
                response.sendRedirect(request.getContextPath() + "/login"));
    }
}
