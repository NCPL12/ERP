package com.ncpl.sales.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Kicks out a user whose account was disabled while they were logged in.
 */
@Component
public class UserStatusFilter extends OncePerRequestFilter {

    /** Static assets and the login/logout pages: no point hitting the DB for these. */
    private static final String[] SKIPPED = {
            "/css/", "/js/", "/images/", "/resources/", "/dist/", "/plugins/", "/webjars/",
            "/swagger-ui", "/v3/api-docs", "/swagger-resources",
            "/login", "/logout", "/access-denied"
    };

    @Autowired
    private UserService userService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String prefix : SKIPPED) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            // One lookup: it re-derives authorities (role edits apply immediately) and carries the enabled flag.
            UserDetails fresh = null;
            try {
                fresh = userService.loadUserByUsername(authentication.getName());
            } catch (UsernameNotFoundException deleted) {
                // treated like a disabled account
            }

            if (fresh == null || !fresh.isEnabled()) {
                SecurityContextHolder.clearContext();

                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                if (expectsJson(request)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Account disabled");
                } else {
                    response.sendRedirect(request.getContextPath() + "/login");
                }
                return;
            }

            Authentication refreshed = new UsernamePasswordAuthenticationToken(
                    fresh, authentication.getCredentials(), fresh.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(refreshed);
        }

        filterChain.doFilter(request, response);
    }

    /** AJAX and REST callers want a status code, not the login page's HTML. */
    private boolean expectsJson(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String accept = request.getHeader("Accept");
        return path.startsWith("/api/")
                || "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                || (accept != null && accept.contains("application/json"));
    }
}
