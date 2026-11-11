package com.ncpl.sales.controller;

import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.model.RolePermissionDto;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RoleReportGrantTest {
    private final RoleRepo roles = mock(RoleRepo.class);
    private final RolePermissionRepo permissions = mock(RolePermissionRepo.class);
    private final RoleController controller = new RoleController();
    private final Role role = new Role();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(controller, "roleRepo", roles);
        ReflectionTestUtils.setField(controller, "rolePermissionRepo", permissions);
        role.setRoleId(7L); role.setName("New role");
        when(roles.findById(7L)).thenReturn(Optional.of(role));
        when(roles.save(any(Role.class))).thenAnswer(call -> call.getArgument(0));
        login("USER_MANAGEMENT_EDIT", "REPORTS_VIEW", "REPORT_DAILY_REPORT");
    }

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    private void login(String... grants) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "editor", "", Arrays.stream(grants).map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
    }

    private int enableReports() {
        RolePermissionDto reports = new RolePermissionDto(); reports.setModule("REPORTS"); reports.setCanView(true);
        return controller.savePermissions(7L, Collections.singletonList(reports)).getStatusCodeValue();
    }

    @Test void newlyCreatedRolesStartWithoutReportGrants() {
        Role created = (Role) controller.createRole("Fresh").getBody();
        assertThat(created.getReportItems()).isEmpty();
    }

    @Test void enablingLegacyNullReportsCannotGrantUnheldReports() {
        assertThat(enableReports()).isEqualTo(403);
        verify(permissions, never()).deleteByRole_RoleId(anyLong());
        verify(permissions, never()).saveAll(any());
    }

    @Test void cannotReactivateUnheldStoredReportSelection() {
        role.setReportItems("MONTHLY_REPORT");
        assertThat(enableReports()).isEqualTo(403);
        verify(permissions, never()).deleteByRole_RoleId(anyLong());
    }

    @Test void heldReportSelectionCanBeEnabled() {
        role.setReportItems("DAILY_REPORT");
        assertThat(enableReports()).isEqualTo(200);
        verify(permissions).saveAll(any());
    }

    @Test void alreadyActiveReportsArePreservedWhenSavingOtherPermissions() {
        RolePermission current = new RolePermission(); current.setModule("REPORTS"); current.setCanView(true);
        when(permissions.findByRole_RoleId(7L)).thenReturn(Collections.singletonList(current));
        assertThat(enableReports()).isEqualTo(200);
        assertThat(role.getReportItems()).isNull();
    }

    @Test void adminCanEnableLegacyReports() {
        login("ADMIN");
        assertThat(enableReports()).isEqualTo(200);
    }
}
