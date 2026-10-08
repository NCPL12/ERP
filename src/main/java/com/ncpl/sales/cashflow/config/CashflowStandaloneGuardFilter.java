package com.ncpl.sales.cashflow.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Keeps the port-8081 test instance focused exclusively on Cashflow Analyzer routes. */
@Component
@Profile("cashflow-standalone")
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class CashflowStandaloneGuardFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        boolean allowed = path.startsWith("/cashflow-analyzer/")
                || "/cashflow-analyzer".equals(path)
                || "/login".equals(path)
                || "/logout".equals(path)
                || "/error".equals(path)
                || "/favicon.ico".equals(path);

        if (allowed || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        response.sendRedirect(contextPath + "/cashflow-analyzer/overview");
    }
}

