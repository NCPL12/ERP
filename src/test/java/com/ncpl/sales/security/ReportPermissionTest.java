package com.ncpl.sales.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;

public class ReportPermissionTest {
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
