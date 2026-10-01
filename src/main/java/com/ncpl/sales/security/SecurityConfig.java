package com.ncpl.sales.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{

		//User details
		@Autowired
		UserService userDetailsService;
		//Custom login handler
		@Autowired
		LoginSuccessHandler customLoginSuccessHandler;

		@Autowired
		UserStatusFilter userStatusFilter;

		//Encrypt password
		@Bean
	    public BCryptPasswordEncoder passwordEncoder() {
	        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
	        return bCryptPasswordEncoder;
	    }
		
		//Check user in DB
		@Autowired
		protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {	
			auth.userDetailsService(userDetailsService);
		}
		
		//URL security
		@Override
		 protected void configure(HttpSecurity http) throws Exception {
		 http.addFilterAfter(userStatusFilter, SecurityContextPersistenceFilter.class);
		 http.csrf().disable();
		 http
		 .authorizeRequests()
		 .antMatchers("/css/**", "/js/**", "/images/**","/resources/**", "/api/**", "/login/**", "/audit/**", "/audit-test.html").permitAll()
		 .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
		 .antMatchers("/sales_report/**").hasAuthority("REPORTS_VIEW")
		 .antMatchers("/itemMaster/**").hasAuthority("ITEM_MASTER_VIEW")
		 .antMatchers("/user-management/**", "/role-management/**").hasAnyAuthority("ADMIN","SUPER ADMIN")
		 .antMatchers("/nonBillableList/**").hasAuthority("NON_BILLABLE_VIEW")
		 .antMatchers("/dashboard/**").hasAuthority("DASHBOARD_VIEW")
		 .antMatchers("/returnableList/**").hasAuthority("RETURNABLE_VIEW")
		 .antMatchers("/partyList/**").hasAuthority("PARTY_VIEW")
		 .antMatchers("/party", "/add/party", "/add/party#").hasAuthority("PARTY_EDIT")
		 .antMatchers("/new_salesOrder", "/new_salesOrder/**", "/edit_salesOrder/**").hasAuthority("SALES_ORDER_EDIT")
		 .antMatchers("/salesList", "/salesList/**", "/soChart", "/soChart/**").hasAuthority("SALES_ORDER_VIEW")
		 .antMatchers("/deliveryChallan", "/deliveryChallan/**").hasAuthority("DELIVERY_CHALLAN_EDIT")
		 .antMatchers("/dcList", "/dcList/**", "/dc/details/**").hasAuthority("DELIVERY_CHALLAN_VIEW")
		 .antMatchers("/invoice", "/invoice/**", "/invoiceList", "/invoiceList/**").hasAuthority("INVOICE_VIEW")
		 .antMatchers("/work_order", "/work_order/**", "/workOrderList", "/workOrderList/**").hasAuthority("WORK_ORDER_VIEW")
		 .antMatchers("/purchaseOrder").hasAuthority("PURCHASE_EDIT")
		 .antMatchers("/purchaseOrder/**").hasAuthority("PURCHASE_VIEW")
		 .antMatchers("/purchase", "/purchase/**", "/purchase_list/**", "/poItem/History/**").hasAuthority("PURCHASE_VIEW")
		 .antMatchers("/new_grn", "/new_grn/**").hasAuthority("GRN_EDIT")
		 .antMatchers("/grn/download/**").hasAuthority("GRN_VIEW")
		 .antMatchers("/grn/*").hasAuthority("GRN_EDIT")
		 .antMatchers("/grnLists", "/grnLists/**", "/grnList", "/grnList/**").hasAuthority("GRN_VIEW")
		 .antMatchers("/add/companyAssets").hasAuthority("COMPANY_ASSETS_EDIT")
		 .antMatchers(HttpMethod.DELETE, "/companyAssets/**").hasAuthority("COMPANY_ASSETS_DELETE")
		 .antMatchers("/companyAssets", "/companyAssets/**").hasAuthority("COMPANY_ASSETS_VIEW")
		 .antMatchers("/purchase_archived", "/dc_archived", "/grn_archived", "/salesList_archived").hasAuthority("ARCHIVES_VIEW")
		 .antMatchers("/**").hasAnyAuthority("ADMIN","NORMAL USER","ITEMMASTER","PURCHASE","STORE","SUPER ADMIN","PURCHASE STORE","STORE USER","SALES")
		 .anyRequest().authenticated()
		 .and()
		 .formLogin().successHandler(customLoginSuccessHandler)
		 .loginPage("/login")
		 .permitAll()
		 .and()
		 .logout()
		 .permitAll()
		 .and()
		 .exceptionHandling().accessDeniedPage("/access-denied");
		 }
		
		
}
