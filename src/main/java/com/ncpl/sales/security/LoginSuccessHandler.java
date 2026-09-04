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

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler{

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
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
