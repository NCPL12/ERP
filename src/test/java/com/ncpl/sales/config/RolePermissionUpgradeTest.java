package com.ncpl.sales.config;

import com.ncpl.sales.model.ArchiveItem;
import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import com.ncpl.sales.security.UserRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RolePermissionUpgradeTest {
    private RolePermissionSeeder seeder;
    private RoleRepo roles;
    private RolePermissionRepo permissions;
    private Role role;
    private List<RolePermission> grid;

    @Before public void setup() {
        seeder = new RolePermissionSeeder();
        roles = mock(RoleRepo.class);
        permissions = mock(RolePermissionRepo.class);
        ReflectionTestUtils.setField(seeder, "roleRepo", roles);
        ReflectionTestUtils.setField(seeder, "rolePermissionRepo", permissions);
        ReflectionTestUtils.setField(seeder, "userRepo", mock(UserRepo.class));
        role = new Role(); role.setRoleId(7L); role.setName("Custom Role");
        grid = new ArrayList<>();
        when(roles.count()).thenReturn(1L);
        when(roles.findAll()).thenReturn(Collections.singletonList(role));
        when(permissions.findByRole_RoleId(7L)).thenReturn(grid);
        when(permissions.save(any(RolePermission.class))).thenAnswer(call -> {
            RolePermission permission = call.getArgument(0);
            grid.add(permission);
            return permission;
        });
    }

    private RolePermission permission(String module, boolean view) {
        RolePermission permission = new RolePermission();
        permission.setRole(role); permission.setModule(module); permission.setCanView(view);
        grid.add(permission);
        return permission;
    }

    @Test public void upgradePreservesLegacyAccessAndDoesNotRepeat() {
        permission("ARCHIVES", true);
        permission("SALES_ORDER", true);
        seeder.run();
        assertEquals(Arrays.stream(ArchiveItem.values()).map(Enum::name).collect(Collectors.toSet()),
                ArchiveItem.visible(role.getArchiveItems()));
        RolePermission charts = grid.stream().filter(p -> p.getModule().equals("CHARTS")).findFirst().get();
        assertTrue(charts.isCanView());
        assertFalse(charts.isCanEdit());
        assertFalse(charts.isCanDelete());
        seeder.run();
        verify(roles, times(1)).save(role);
        verify(permissions, times(1)).save(any(RolePermission.class));
    }

    @Test public void upgradeDoesNotInventAccessForPreviouslyDeniedRoles() {
        permission("ARCHIVES", false);
        permission("SALES_ORDER", false);
        seeder.run();
        assertEquals("", role.getArchiveItems());
        assertFalse(grid.stream().filter(p -> p.getModule().equals("CHARTS")).findFirst().get().isCanView());
    }

    @Test public void upgradeKeepsExplicitSelectionsAndRevocations() {
        permission("ARCHIVES", true);
        permission("SALES_ORDER", true);
        permission("CHARTS", false);
        for (String selected : new String[]{"", "PURCHASE_ORDER"}) {
            role.setArchiveItems(selected);
            seeder.run();
            assertEquals(selected, role.getArchiveItems());
        }
        verify(roles, never()).save(any(Role.class));
        verify(permissions, never()).save(any(RolePermission.class));
    }

    @Test public void freshInstallSeedsChartAndArchiveAccessConsistently() {
        when(roles.count()).thenReturn(0L);
        List<Role> created = new ArrayList<>();
        when(roles.save(any(Role.class))).thenAnswer(call -> {
            Role saved = call.getArgument(0); created.add(saved); return saved;
        });
        seeder.run();
        for (Role seeded : created) {
            boolean archiveView = grid.stream().anyMatch(p -> p.getRole() == seeded
                    && p.getModule().equals("ARCHIVES") && p.isCanView());
            assertEquals(archiveView ? ArchiveItem.values().length : 0,
                    ArchiveItem.visible(seeded.getArchiveItems()).size());
            boolean salesView = grid.stream().anyMatch(p -> p.getRole() == seeded
                    && p.getModule().equals("SALES_ORDER") && p.isCanView());
            boolean chartsView = grid.stream().anyMatch(p -> p.getRole() == seeded
                    && p.getModule().equals("CHARTS") && p.isCanView());
            assertEquals(seeded.getName(), salesView, chartsView);
        }
        assertFalse(created.isEmpty());
    }
}
