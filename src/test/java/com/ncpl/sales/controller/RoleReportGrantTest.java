package com.ncpl.sales.controller;

import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.model.RolePermissionDto;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RoleReportGrantTest {
    private RoleController controller;
    private RolePermissionRepo permissions;
    private Role role;
    private List<RolePermission> grid;

    @Before public void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "limited-editor", "unused", Arrays.asList(new SimpleGrantedAuthority("USER_MANAGEMENT_EDIT"),
                        new SimpleGrantedAuthority("REPORTS_VIEW"), new SimpleGrantedAuthority("REPORT_DAILY_REPORT"))));
        controller = new RoleController();
        RoleRepo roles = mock(RoleRepo.class);
        permissions = mock(RolePermissionRepo.class);
        grid = new ArrayList<>();
        ReflectionTestUtils.setField(controller, "roleRepo", roles);
        ReflectionTestUtils.setField(controller, "rolePermissionRepo", permissions);
        when(roles.save(any(Role.class))).thenAnswer(call -> {
            Role saved = call.getArgument(0); saved.setRoleId(7L); return saved;
        });
        when(permissions.saveAll(anyList())).thenAnswer(call -> {
            grid.clear(); grid.addAll(call.getArgument(0)); return grid;
        });
        role = (Role) controller.createRole("NEW ROLE").getBody();
        when(roles.findById(7L)).thenReturn(Optional.of(role));
        when(permissions.findByRole_RoleId(7L)).thenAnswer(call -> new ArrayList<>(grid));
        clearInvocations(permissions);
    }

    @After public void clearSecurity() { SecurityContextHolder.clearContext(); }

    private int enableReports() {
        RolePermissionDto reports = new RolePermissionDto();
        reports.setModule("REPORTS"); reports.setCanView(true);
        return controller.savePermissions(7L, Collections.singletonList(reports)).getStatusCodeValue();
    }

    @Test public void newRoleCannotReceiveAReportTheEditorDoesNotHave() {
        assertEquals("", role.getReportItems());
        assertEquals(200, enableReports());
        assertEquals(403, controller.saveReportItems(7L,
                Collections.singletonList("MONTHLY_REPORT")).getStatusCodeValue());
        assertEquals("", role.getReportItems());
        assertEquals(200, controller.saveReportItems(7L,
                Collections.singletonList("DAILY_REPORT")).getStatusCodeValue());
        assertEquals("DAILY_REPORT", role.getReportItems());
    }

    @Test public void enablingReportsCannotActivateImplicitOrDisabledUnheldGrants() {
        for (String selection : new String[]{null, "MONTHLY_REPORT"}) {
            role.setReportItems(selection);
            assertEquals(403, enableReports());
        }
        verify(permissions, never()).deleteByRole_RoleId(7L);
        verify(permissions, never()).saveAll(anyList());
    }

    @Test public void existingReportGrantsCanBeRetainedByALimitedEditor() {
        role.setReportItems("MONTHLY_REPORT");
        grid.stream().filter(p -> p.getModule().equals("REPORTS")).forEach(p -> p.setCanView(true));
        assertEquals(200, enableReports());
        assertEquals(200, controller.saveReportItems(7L,
                Collections.singletonList("MONTHLY_REPORT")).getStatusCodeValue());
    }

    @Test public void administratorsCanEnableLegacyReportSelections() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "unused", Collections.singletonList(new SimpleGrantedAuthority("ADMIN"))));
        role.setReportItems(null);
        assertEquals(200, enableReports());
        assertEquals(200, controller.saveReportItems(7L,
                Collections.singletonList("MONTHLY_REPORT")).getStatusCodeValue());
    }
}
