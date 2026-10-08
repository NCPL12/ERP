package com.ncpl.sales.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler{

	// Landing page candidates, checked in order — first one the user actually
	// has View on wins. Dashboard is the usual first page; Sales Order is the
	// fallback when a role has had Dashboard access removed.
	private static final Map<String, String> LANDING_PAGES = new LinkedHashMap<>();
	static {
		LANDING_PAGES.put("DASHBOARD_VIEW", "/dashboard");
		LANDING_PAGES.put("SALES_ORDER_VIEW", "/salesList");
		LANDING_PAGES.put("PARTY_VIEW", "/partyList");
		LANDING_PAGES.put("ITEM_MASTER_VIEW", "/itemMaster");
		LANDING_PAGES.put("COMPANY_ASSETS_VIEW", "/companyAssets");
		LANDING_PAGES.put("DELIVERY_CHALLAN_VIEW", "/dcList");
		LANDING_PAGES.put("RETURNABLE_VIEW", "/returnableList");
		LANDING_PAGES.put("INVOICE_VIEW", "/invoiceList");
		LANDING_PAGES.put("WORK_ORDER_VIEW", "/workOrderList");
		LANDING_PAGES.put("PURCHASE_VIEW", "/purchase");
		LANDING_PAGES.put("GRN_VIEW", "/grnLists");
		LANDING_PAGES.put("NON_BILLABLE_VIEW", "/nonBillableList");
		LANDING_PAGES.put("REPORTS_VIEW", "/sales_report");
		LANDING_PAGES.put("ARCHIVES_VIEW", "/salesList_archived");
		LANDING_PAGES.put("USER_MANAGEMENT_VIEW", "/user-management");
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		Set<String> authorities = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());

		for (Map.Entry<String, String> landingPage : LANDING_PAGES.entrySet()) {
			if (authorities.contains(landingPage.getKey())) {
				response.sendRedirect(request.getContextPath() + landingPage.getValue());
				return;
			}
		}

		// None of the known landing pages are open to this role — don't dump
		// them straight onto an access-denied page right after they log in.
		response.sendRedirect(request.getContextPath()+"/access-denied");
	}

}
