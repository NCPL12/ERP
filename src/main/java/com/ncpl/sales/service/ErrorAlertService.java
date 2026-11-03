package com.ncpl.sales.service;

import java.time.Instant;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ErrorAlertService {
    private static final Logger log = LoggerFactory.getLogger(ErrorAlertService.class);

    private final EmailService emailService;
    private final String recipients;

    public ErrorAlertService(EmailService emailService, @Value("${error.alert.to:}") String recipients) {
        this.emailService = emailService;
        this.recipients = recipients == null ? "" : recipients.trim();
        if (this.recipients.isEmpty()) {
            log.warn("Error alert email is disabled; set ERROR_ALERT_TO to the recipient address(es)");
        }
    }

    public boolean isEnabled() {
        return !recipients.isEmpty();
    }

    @Async("errorAlertExecutor")
    public void sendAlert(int status, String method, String path, String route, Throwable failure,
            String username, String authorities, String clientAddress, String browser, long durationMillis) {
        String subject = "[ERP error] HTTP " + status + " " + limit(oneLine(route), 120);
        StringBuilder body = new StringBuilder();
        body.append("Time (UTC): ").append(Instant.now()).append('\n');
        body.append("User: ").append(limit(oneLine(username), 200)).append('\n');
        body.append("Roles / permissions: ").append(limit(oneLine(authorities), 2000)).append('\n');
        body.append("Client address: ").append(limit(oneLine(clientAddress), 100)).append('\n');
        body.append("Browser: ").append(limit(oneLine(browser), 500)).append('\n');
        body.append("Request duration: ").append(durationMillis).append(" ms\n");
        body.append("Status: ").append(status).append('\n');
        body.append("Request: ").append(method).append(' ').append(path).append('\n');
        body.append("Route: ").append(route).append('\n');
        if (failure != null) {
            body.append("Exception: ").append(failure.getClass().getName()).append('\n');
            body.append("Message: ").append(limit(oneLine(failure.getMessage()), 500)).append('\n');
            Throwable root = failure;
            java.util.Set<Throwable> visited = java.util.Collections.newSetFromMap(
                    new java.util.IdentityHashMap<Throwable, Boolean>());
            visited.add(root);
            while (root.getCause() != null && visited.add(root.getCause())) root = root.getCause();
            body.append("Root cause: ").append(root.getClass().getName()).append(": ")
                    .append(limit(oneLine(root.getMessage()), 500)).append('\n');
            body.append("Root cause stack (up to 10 frames):\n");
            StackTraceElement[] frames = root.getStackTrace();
            for (int i = 0; i < Math.min(frames.length, 10); i++) {
                body.append("  at ").append(limit(oneLine(frames[i].toString()), 500)).append('\n');
            }
        } else {
            body.append("Exception: None captured; the endpoint returned HTTP 5xx.\n");
        }
        body.append("Inspect the application log at this time for the stack trace.\n");

        try {
            emailService.sendErrorAlert(recipients, subject, body.toString());
        } catch (MessagingException | RuntimeException mailError) {
            log.error("Could not send error alert for " + method + " " + route + " (HTTP " + status + ")", mailError);
        }
    }

    private String oneLine(String value) {
        return value == null ? "" : value.replace('\r', ' ').replace('\n', ' ');
    }

    private String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
