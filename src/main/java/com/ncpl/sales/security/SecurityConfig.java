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

		// An exact-match rule like "/party" does not match "/party/", but Spring MVC
		// can still route "/party/" to the same controller, so it would skip the rule.
		// This adds the trailing-slash form of each exact path.
		private static String[] withSlash(String... paths) {
			String[] out = new String[paths.length * 2];
			for (int i = 0; i < paths.length; i++) {
				out[2 * i] = paths[i];
				out[2 * i + 1] = paths[i] + "/";
			}
			return out;
		}
		
		//URL security. The first matching rule wins, so keep specific rules above general ones.
		@Override
		 protected void configure(HttpSecurity http) throws Exception {
		 http.addFilterAfter(userStatusFilter, SecurityContextPersistenceFilter.class);
		 // TODO: CSRF is still off. Turn on later (delete this line) after adding the
		 // token to every POST form and AJAX call. Test on a copy first.
		 http.csrf().disable();
		 http
		 .authorizeRequests()
		 .antMatchers("/css/**", "/js/**", "/images/**","/resources/**", "/login/**").permitAll()
		 .antMatchers("/webjars/**").permitAll()
		 // API docs show every endpoint, so only admins can open them.
		 .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**")
		         .hasAnyAuthority("ADMIN","SUPER ADMIN")
		 // Audit logs were open to anyone without login. Admin only now.
		 // If other roles need them, change this line.
		 .antMatchers("/audit/**", "/audit-test.html", "/api/audit/**").hasAnyAuthority("ADMIN","SUPER ADMIN")
		 .antMatchers(HttpMethod.POST, withSlash("/po_list/tally", "/po_list/tally/import")).hasAuthority("PURCHASE_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/save/purchaseOrder", "/api/purchase/upload",
		         "/api/update_po_archive", "/api/update_po_unarchive")).hasAuthority("PURCHASE_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/api/purchase/delete")).hasAuthority("PURCHASE_DELETE")
		 .antMatchers(HttpMethod.GET, withSlash("/purchase/view")).hasAuthority("PURCHASE_EDIT")
		 .antMatchers(HttpMethod.GET, withSlash("/po_list/tally/options", "/po_list/tally/progress")).hasAuthority("PURCHASE_VIEW")
		 // A report card being hidden in the JSP is not an access check for its URL.
		 .antMatchers(HttpMethod.GET, withSlash("/stock_history/Download")).hasAuthority("REPORT_STOCK_HISTORY_BY_DATE")
		 .antMatchers(HttpMethod.GET, "/stock/summary_details", "/stock/summary_details/**")
		         .hasAuthority("REPORT_MONTHLY_REPORT")
		 .antMatchers(HttpMethod.GET, withSlash("/monthly_stock_report/download")).hasAuthority("REPORT_MONTHLY_REPORT")
		 .antMatchers(HttpMethod.GET, withSlash("/stock_report_by_region/Download")).hasAuthority("REPORT_STOCK_REPORT_BY_REGION")
		 .antMatchers(HttpMethod.GET, withSlash("/stock_report_by_date/Download")).hasAuthority("REPORT_DAILY_REPORT")
		 .antMatchers(HttpMethod.GET, "/stock/outstandingReport", "/stock/outstandingReport/**").hasAuthority("REPORT_OUTSTANDING_STOCK_REPORT")
		 .antMatchers(HttpMethod.GET, "/stock/pendingporeport", "/stock/pendingporeport/").hasAuthority("REPORT_PENDING_PO_REPORT")
		 .antMatchers(HttpMethod.GET, "/stock/dcReport", "/stock/dcReport/**").hasAuthority("REPORT_PENDING_DC_REPORT")
		 .antMatchers(HttpMethod.GET, "/poItem/History", "/poItem/History/**").hasAuthority("REPORT_PURCHASE_ITEM_HISTORY")
		 .antMatchers(HttpMethod.GET, withSlash("/sales_list/by_item_id")).hasAuthority("REPORT_SALES_LIST_BY_ITEM")
		 .antMatchers(HttpMethod.GET, withSlash("/stock/pendingporeport/byPoNumber")).hasAuthority("REPORT_PENDING_PO_REPORT_BY_PONUMBER")
		 .antMatchers(HttpMethod.GET, withSlash("/dc_list/by_item")).hasAuthority("REPORT_DC_LIST_BY_ITEM")
		 .antMatchers(HttpMethod.GET, withSlash("/po_list/by_date")).hasAuthority("REPORT_PO_LIST_BY_DATE")
		 .antMatchers(HttpMethod.GET, withSlash("/active_sales/by_customer")).hasAuthority("REPORT_ACTIVE_SALES_ORDER_BY_CUSTOMER")
		 .antMatchers(HttpMethod.GET, withSlash("/grn_itemwise/by_date")).hasAuthority("REPORT_GRN_REPORT_BY_DATE")
		 .antMatchers(HttpMethod.GET, withSlash("/dc_list/by_date")).hasAuthority("REPORT_DC_REPORT_BY_DATE")
		 .antMatchers(HttpMethod.GET, withSlash("/api/grn_po_by_model")).hasAuthority("REPORT_GRN_AND_PONUMBER_BY_MODELS")
		 .antMatchers(HttpMethod.GET, withSlash("/monthly_report/by_date")).hasAuthority("REPORT_MONTHLY_STOCK_MOVEMENT_REPORT")
		 .antMatchers("/sales_report/**").hasAuthority("REPORTS_VIEW")

		 // ---- Save, delete and upload URLs: each needs the EDIT or DELETE permission of its module ----
		 // Sales order
		 .antMatchers(HttpMethod.POST, withSlash("/add/salesOrder", "/api/add/design", "/api/update_so_archive",
		         "/api/update_so_unarchive", "/api/sales/upload", "/api/design/upload")).hasAuthority("SALES_ORDER_EDIT")
		 .antMatchers(HttpMethod.POST, "/api/clientPo/upload/**").hasAuthority("SALES_ORDER_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/api/design/delete", "/api/salesItem/delete")).hasAuthority("SALES_ORDER_DELETE")
		 .antMatchers(HttpMethod.GET, "/sales/details/**", "/sales/downlaod/**").hasAuthority("SALES_ORDER_VIEW")
		 .antMatchers(HttpMethod.GET, "/material/tracker/**").hasAnyAuthority("SALES_ORDER_VIEW","REPORTS_VIEW")
		 // TDS has no module of its own, so it follows sales order or purchase
		 .antMatchers(HttpMethod.POST, withSlash("/add/tds")).hasAnyAuthority("SALES_ORDER_EDIT","PURCHASE_EDIT")
		 .antMatchers(HttpMethod.GET, withSlash("/tds", "/api/salesOrder_tds/view")).hasAnyAuthority("SALES_ORDER_VIEW","PURCHASE_VIEW")
		 // Party and its masters (category, type, country, state, city)
		 .antMatchers(withSlash("/api/save/Party_category")).hasAuthority("PARTY_EDIT") // a GET that saves data
		 .antMatchers(HttpMethod.POST, withSlash("/add/partyAddress", "/add/partyBank", "/api/add/category", "/api/type/add",
		         "/api/country/add", "/api/state/add", "/api/city/add")).hasAuthority("PARTY_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/api/partyAltAddress/delete", "/api/partyBank/delete", "/api/category/delete",
		         "/api/type/delete", "/api/city/delete")).hasAuthority("PARTY_DELETE")
		 .antMatchers(HttpMethod.GET, withSlash("/partyAltAddress", "/partyBank")).hasAuthority("PARTY_VIEW")
		 .antMatchers(HttpMethod.POST, withSlash("/api/add/designation", "/api/designation/delete"))
		         .hasAnyAuthority("PARTY_EDIT","COMPANY_ASSETS_EDIT","USER_MANAGEMENT_EDIT","ADMIN","SUPER ADMIN")
		 // Employees and asset types (company assets screen)
		 .antMatchers(HttpMethod.POST, "/api/employees/**")
		         .hasAnyAuthority("COMPANY_ASSETS_EDIT","USER_MANAGEMENT_EDIT","ADMIN","SUPER ADMIN")
		 .antMatchers(HttpMethod.PUT, "/api/employees/**")
		         .hasAnyAuthority("COMPANY_ASSETS_EDIT","USER_MANAGEMENT_EDIT","ADMIN","SUPER ADMIN")
		 .antMatchers(HttpMethod.DELETE, "/api/employees/**")
		         .hasAnyAuthority("COMPANY_ASSETS_EDIT","USER_MANAGEMENT_EDIT","ADMIN","SUPER ADMIN")
		 .antMatchers(HttpMethod.POST, withSlash("/api/asset_type/add")).hasAuthority("COMPANY_ASSETS_EDIT")
		 // Item master
		 .antMatchers(HttpMethod.POST, withSlash("/api/add/stock", "/api/add/supplier", "/api/make/add",
		         "/api/items/upload", "/add/itemMaster")).hasAuthority("ITEM_MASTER_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/api/item/delete", "/api/make/delete")).hasAuthority("ITEM_MASTER_DELETE")
		 .antMatchers(HttpMethod.GET, withSlash("/itemist/Download", "/stockModified/Download", "/stockList/Download"))
		         .hasAnyAuthority("ITEM_MASTER_VIEW","REPORTS_VIEW")
		 // GRN, delivery challan, invoice, work order, returnable
		 .antMatchers(HttpMethod.POST, withSlash("/add/grn", "/api/update_grn_archive", "/api/update_grn_unarchive"))
		         .hasAuthority("GRN_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/add/delivery_challan", "/api/update_dc_archive", "/api/update_dc_unarchive"))
		         .hasAuthority("DELIVERY_CHALLAN_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/add/invoice", "/api/payment_status")).hasAuthority("INVOICE_EDIT")
		 .antMatchers(HttpMethod.POST, withSlash("/add/work_order")).hasAuthority("WORK_ORDER_EDIT")
		 .antMatchers(HttpMethod.GET, withSlash("/returnable/create")).hasAuthority("RETURNABLE_EDIT")

		 // ---- Page access by module (VIEW) ----
		 .antMatchers("/itemMaster/**").hasAuthority("ITEM_MASTER_VIEW")
		 // View-only users may open the pages (GET). Anything that changes data needs EDIT or admin.
		 .antMatchers(HttpMethod.GET, "/user-management/**", "/role-management/**")
		         .hasAnyAuthority("ADMIN","SUPER ADMIN","USER_MANAGEMENT_VIEW","USER_MANAGEMENT_EDIT")
		 .antMatchers("/user-management/**", "/role-management/**")
		         .hasAnyAuthority("ADMIN","SUPER ADMIN","USER_MANAGEMENT_EDIT")
		 .antMatchers("/nonBillableList/**").hasAuthority("NON_BILLABLE_VIEW")
		 .antMatchers("/dashboard/**").hasAuthority("DASHBOARD_VIEW")
		 .antMatchers("/returnableList/**", "/returnable/**", "/returnableDCList", "/returnableDCList/",
		         "/returnableDcList", "/returnableDcList/").hasAuthority("RETURNABLE_VIEW")
		 .antMatchers(withSlash("/add/returned_items")).hasAuthority("RETURNABLE_EDIT")
		 .antMatchers("/partyList/**").hasAuthority("PARTY_VIEW")
		 .antMatchers(withSlash("/party", "/add/party")).hasAuthority("PARTY_EDIT")
		 .antMatchers("/new_salesOrder", "/new_salesOrder/**", "/edit_salesOrder/**").hasAuthority("SALES_ORDER_EDIT")
		 .antMatchers("/salesList", "/salesList/**", "/soChart", "/soChart/**").hasAuthority("SALES_ORDER_VIEW")
		 .antMatchers("/deliveryChallan", "/deliveryChallan/**").hasAuthority("DELIVERY_CHALLAN_EDIT")
		 .antMatchers("/dcList", "/dcList/**", "/dc/**").hasAuthority("DELIVERY_CHALLAN_VIEW")
		 .antMatchers("/invoice", "/invoice/**", "/invoiceList", "/invoiceList/**").hasAuthority("INVOICE_VIEW")
		 .antMatchers("/work_order", "/work_order/**", "/workOrderList", "/workOrderList/**").hasAuthority("WORK_ORDER_VIEW")
		 .antMatchers("/purchaseOrder", "/purchaseOrder/").hasAuthority("PURCHASE_EDIT")
		 .antMatchers("/purchaseOrder/**", "/purchaseOrder_pdf/**", "/prevpurchaseOrder/**").hasAuthority("PURCHASE_VIEW")
		 .antMatchers("/purchase", "/purchase/**", "/purchase_list/**", "/poItem/History/**").hasAuthority("PURCHASE_VIEW")
		 .antMatchers("/new_grn", "/new_grn/**").hasAuthority("GRN_EDIT")
		 .antMatchers("/grn/download/**").hasAuthority("GRN_VIEW")
		 .antMatchers("/grn/*").hasAuthority("GRN_EDIT")
		 .antMatchers("/grnLists", "/grnLists/**", "/grnList", "/grnList/**").hasAuthority("GRN_VIEW")
		 .antMatchers(withSlash("/add/companyAssets")).hasAuthority("COMPANY_ASSETS_EDIT")
		 .antMatchers(HttpMethod.DELETE, "/companyAssets/**").hasAuthority("COMPANY_ASSETS_DELETE")
		 .antMatchers("/companyAssets", "/companyAssets/**").hasAuthority("COMPANY_ASSETS_VIEW")
		 .antMatchers(withSlash("/purchase_archived", "/dc_archived", "/grn_archived", "/salesList_archived")).hasAuthority("ARCHIVES_VIEW")
		 // Anything not listed above is open to EVERY logged-in user. That is mainly the small
		 // GET helper endpoints under /api (dropdown lists and lookups shared by several pages).
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