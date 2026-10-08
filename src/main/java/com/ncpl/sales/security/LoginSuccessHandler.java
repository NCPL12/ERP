package com.ncpl.sales.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler{

	@Value("${cashflow.standalone:false}")
	private boolean cashflowStandalone;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		if (cashflowStandalone) {
			response.sendRedirect(request.getContextPath()+"/cashflow-analyzer/overview");
			return;
		}
		boolean isItemMasterOrStore = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(role -> role.equalsIgnoreCase("ITEMMASTER") || role.equalsIgnoreCase("STORE"));
		if (isItemMasterOrStore) {
			response.sendRedirect(request.getContextPath()+"/itemMaster");
		}else {
			response.sendRedirect(request.getContextPath()+"/dashboard");
		}
	}
	
}
