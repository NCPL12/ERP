package com.ncpl.sales.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import com.ncpl.sales.service.ErrorAlertService;

public class ErrorAlertFilterTest {
    @Test
    public void alertsOnServerFailureButNotClientFailure() throws Exception {
        RecordingAlertService alerts = new RecordingAlertService();
        ErrorAlertFilter filter = new ErrorAlertFilter(alerts, 300000);
        MockHttpServletRequest badRequest = request();
        filter.doFilter(badRequest, new MockHttpServletResponse(), status(400));
        assertEquals(0, alerts.sent);

        filter.doFilter(request(), new MockHttpServletResponse(), status(503));
        assertEquals(1, alerts.sent);
        assertEquals(503, alerts.lastStatus);
        assertEquals("/api/orders", alerts.lastRoute);

        filter.doFilter(request(), new MockHttpServletResponse(), status(503));
        assertEquals(1, alerts.sent);
    }

    @Test
    public void alertsOnUnhandledExceptionWithoutChangingTheResponseFlow() throws Exception {
        RecordingAlertService alerts = new RecordingAlertService();
        ErrorAlertFilter filter = new ErrorAlertFilter(alerts, 0);
        IllegalStateException failure = new IllegalStateException("unexpected");
        try {
            filter.doFilter(request(), new MockHttpServletResponse(), (req, res) -> {
                throw failure;
            });
            fail("Expected the original exception");
        } catch (IllegalStateException actual) {
            assertSame(failure, actual);
        }
        assertEquals(1, alerts.sent);
        assertEquals(500, alerts.lastStatus);
        assertSame(failure, alerts.lastFailure);
    }

    @Test
    public void reportsHandledExceptionOnlyOnceAcrossDispatches() throws Exception {
        RecordingAlertService alerts = new RecordingAlertService();
        ErrorAlertFilter filter = new ErrorAlertFilter(alerts, 0);
        MockHttpServletRequest request = request();
        IllegalStateException failure = new IllegalStateException("handled");
        request.setAttribute(ErrorAlertFilter.FAILURE_ATTRIBUTE, failure);

        filter.doFilter(request, new MockHttpServletResponse(), status(500));
        filter.doFilter(request, new MockHttpServletResponse(), status(500));

        assertEquals(1, alerts.sent);
        assertSame(failure, alerts.lastFailure);
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ncpl-sales/api/orders");
        request.setContextPath("/ncpl-sales");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/orders");
        return request;
    }

    private FilterChain status(int code) {
        return (request, response) -> ((HttpServletResponse) response).setStatus(code);
    }

    private static class RecordingAlertService extends ErrorAlertService {
        private int sent;
        private int lastStatus;
        private String lastRoute;
        private Throwable lastFailure;

        private RecordingAlertService() {
            super(null, "team@example.com");
        }

        @Override
        public void sendAlert(int status, String method, String path, String route, Throwable failure) {
            sent++;
            lastStatus = status;
            lastRoute = route;
            lastFailure = failure;
        }
    }
}
