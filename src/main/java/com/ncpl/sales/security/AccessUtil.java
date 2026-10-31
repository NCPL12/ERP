package com.ncpl.sales.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/** Small helpers to ask "what can the logged-in user do?" inside controllers. */
public final class AccessUtil {

	private AccessUtil() {
	}

	public static boolean isAdminRole(String role) {
		return "ADMIN".equalsIgnoreCase(role) || "SUPER ADMIN".equalsIgnoreCase(role);
	}

	/** True if the logged-in user holds this exact authority (a role name or a permission like INVOICE_EDIT). */
	public static boolean callerHas(String authority) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return false;
		}
		for (GrantedAuthority g : auth.getAuthorities()) {
			if (g.getAuthority().equals(authority)) {
				return true;
			}
		}
		return false;
	}

	public static boolean callerIsAdmin() {
		return callerHas("ADMIN") || callerHas("SUPER ADMIN");
	}
}