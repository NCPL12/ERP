package com.ncpl.sales.config;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerMapping;

import com.ncpl.sales.service.ErrorAlertService;

public class ErrorAlertFilter implements Filter {
    public static final String FAILURE_ATTRIBUTE = ErrorAlertFilter.class.getName() + ".failure";
    private static final String ALERT_SENT_ATTRIBUTE = ErrorAlertFilter.class.getName() + ".sent";
    private static final Logger log = LoggerFactory.getLogger(ErrorAlertFilter.class);

    private final ErrorAlertService alertService;
    private final long cooldownMillis;
    private final ConcurrentMap<String, Long> lastAlertAt = new ConcurrentHashMap<>();

    public ErrorAlertFilter(ErrorAlertService alertService, long cooldownMillis) {
        this.alertService = alertService;
        this.cooldownMillis = Math.max(0, cooldownMillis);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        Throwable failure = null;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException | Error error) {
            failure = error;
            throw error;
        } finally {
            try {
                reportFailure(httpRequest, httpResponse, failure);
            } catch (RuntimeException alertError) {
                log.error("Error alert processing failed", alertError);
            }
        }
    }

    private void reportFailure(HttpServletRequest request, HttpServletResponse response, Throwable thrown) {
        if (!alertService.isEnabled() || Boolean.TRUE.equals(request.getAttribute(ALERT_SENT_ATTRIBUTE))) {
            return;
        }
        Throwable failure = thrown;
        if (failure == null) {
            Object handled = request.getAttribute(FAILURE_ATTRIBUTE);
            failure = handled instanceof Throwable ? (Throwable) handled : null;
        }
        if (failure == null) {
            Object dispatched = request.getAttribute("javax.servlet.error.exception");
            failure = dispatched instanceof Throwable ? (Throwable) dispatched : null;
        }
        if (isClientAbort(failure)) {
            return;
        }

        int status = response.getStatus();
        if (status < 500 && failure == null) {
            return;
        }
        status = Math.max(status, 500);
        String path = request.getRequestURI().substring(request.getContextPath().length());
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String route = pattern instanceof String ? (String) pattern : "(unmapped request)";
        String failureType = failure == null ? "" : failure.getClass().getName();
        String key = request.getMethod() + " " + route + " " + status + " " + failureType;
        long now = System.currentTimeMillis();
        AtomicBoolean shouldSend = new AtomicBoolean();
        lastAlertAt.compute(key, (ignored, previous) -> {
            if (previous == null || now - previous >= cooldownMillis) {
                shouldSend.set(true);
                return now;
            }
            return previous;
        });
        request.setAttribute(ALERT_SENT_ATTRIBUTE, Boolean.TRUE);
        if (shouldSend.get()) {
            alertService.sendAlert(status, request.getMethod(), path, route, failure);
        }
    }

    private boolean isClientAbort(Throwable failure) {
        while (failure != null) {
            if (failure instanceof ClientAbortException) {
                return true;
            }
            failure = failure.getCause();
        }
        return false;
    }
}
