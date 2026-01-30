package com.ncpl.sales.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler{
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		@SuppressWarnings("unused")
		String s = authentication.getName();
		if(authentication.getName().equalsIgnoreCase("ItemMaster") || authentication.getName().equalsIgnoreCase("store")) {
			response.sendRedirect(request.getContextPath()+"/itemMaster");
		}else {
			response.sendRedirect(request.getContextPath()+"/welcome");
		}
	}
	
}
