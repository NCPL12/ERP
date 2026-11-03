package com.ncpl.sales.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;

public class ReportPermissionTest {
    @Test
    public void customRoleCanSeeEachGrantedReportWithoutALegacyRoleName() throws Exception {
        String jsp = new String(Files.readAllBytes(Paths.get(
                "src/main/webapp/WEB-INF/views/salesReport.jsp")), StandardCharsets.UTF_8);
        Matcher tags = Pattern.compile("<security:authorize access=\"([^\"]+)\"").matcher(jsp);
        Set<String> covered = new java.util.HashSet<>();
        while (tags.find()) {
            String expression = tags.group(1);
            Matcher permission = Pattern.compile("REPORT_[A-Z_]+").matcher(expression);
            if (!permission.find()) continue;
            String authority = permission.group();
            covered.add(authority);
            assertTrue(authority + " is hidden for a custom role", reportVisible(expression, authority));
            assertFalse(authority + " is visible without its permission", reportVisible(expression, "REPORTS_VIEW"));
        }
        for (com.ncpl.sales.model.ReportItem item : com.ncpl.sales.model.ReportItem.values()) {
            assertTrue("Missing report check: " + item, covered.contains("REPORT_" + item.name()));
        }
    }

    private boolean reportVisible(String expression, String authority) {
        SecurityExpressionRoot root = new SecurityExpressionRoot(new UsernamePasswordAuthenticationToken(
                "hari", "unused", Arrays.asList(new SimpleGrantedAuthority("Accounts Admin"),
                        new SimpleGrantedAuthority(authority)))) { };
        return Boolean.TRUE.equals(new SpelExpressionParser().parseExpression(expression)
                .getValue(new StandardEvaluationContext(root), Boolean.class));
    }

    @Test
    public void reportAuthoritiesRequireReportsViewAndTheSelectedItem() {
        User user = new User();
        user.setUsername("report-user");
        user.setPassword("password");
        user.setEnabled(true);
        user.setRole("REPORT USER");

        UserRepo users = mock(UserRepo.class);
        RoleRepo roles = mock(RoleRepo.class);
        RolePermissionRepo permissions = mock(RolePermissionRepo.class);
        Role role = new Role();
        role.setReportItems("PENDING_PO_REPORT");
        RolePermission reports = new RolePermission();
        reports.setModule("REPORTS");
        when(users.findUserByUserName("report-user")).thenReturn(user);
        when(roles.findByName("REPORT USER")).thenReturn(role);
        when(permissions.findByRoleName("REPORT USER")).thenReturn(Collections.singletonList(reports));

        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepo", users);
        ReflectionTestUtils.setField(service, "roleRepo", roles);
        ReflectionTestUtils.setField(service, "rolePermissionRepo", permissions);

        reports.setCanView(false);
        assertFalse(authorities(service).contains("REPORT_PENDING_PO_REPORT"));

        reports.setCanView(true);
        Set<String> granted = authorities(service);
        assertTrue(granted.contains("REPORTS_VIEW"));
        assertTrue(granted.contains("REPORT_PENDING_PO_REPORT"));
        assertFalse(granted.contains("REPORT_DAILY_REPORT"));
    }

    private Set<String> authorities(UserService service) {
        return service.loadUserByUsername("report-user").getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }
}
