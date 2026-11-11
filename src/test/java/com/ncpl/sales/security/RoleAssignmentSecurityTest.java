package com.ncpl.sales.security;

import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RoleAssignmentSecurityTest {
    private final RoleRepo roles = mock(RoleRepo.class);
    private final RolePermissionRepo permissions = mock(RolePermissionRepo.class);
    private final UserRepo users = mock(UserRepo.class);
    private final UserService service = new UserService();
    private final UserController controller = new UserController();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "roleRepo", roles);
        ReflectionTestUtils.setField(service, "rolePermissionRepo", permissions);
        ReflectionTestUtils.setField(service, "userRepo", users);
        EncryptedPasswordUtils encoder = mock(EncryptedPasswordUtils.class);
        when(encoder.encrytePassword(anyString())).thenReturn("encoded");
        ReflectionTestUtils.setField(service, "encryptPasswd", encoder);
        ReflectionTestUtils.setField(controller, "userService", service);
        when(users.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        login("USER_MANAGEMENT_EDIT");
        role("Limited", "", Collections.emptyList());
    }

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    private void login(String... grants) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "limited-editor", "", Arrays.stream(grants).map(SimpleGrantedAuthority::new).collect(Collectors.toList())));
    }

    private void role(String name, String reports, List<RolePermission> grid) {
        Role role = new Role(); role.setName(name); role.setReportItems(reports);
        when(roles.findByName(name)).thenReturn(role);
        when(permissions.findByRoleName(name)).thenReturn(grid);
    }

    private RolePermission permission(String module, boolean delete) {
        RolePermission permission = new RolePermission();
        permission.setModule(module); permission.setCanView(true); permission.setCanDelete(delete);
        return permission;
    }

    private ResponseEntity<?> create(String role) {
        return controller.createUser("new-account", "password", role, true, "1234567890", "new@example.com", "New user");
    }

    private User target(String role) {
        User target = new User(); target.setId(2L); target.setUsername("target");
        target.setPassword("encoded"); target.setRole(role); target.setEnabled(true);
        when(users.findById(2L)).thenReturn(Optional.of(target));
        return target;
    }

    @Test void rejectsUnknownAndPermissionNamedRolesEvenForAdmin() {
        for (String caller : new String[] {"USER_MANAGEMENT_EDIT", "ADMIN"}) {
            login(caller);
            assertThat(create("Unknown").getStatusCodeValue()).isEqualTo(403);
            role("PURCHASE_DELETE", "", Collections.emptyList());
            assertThat(create("PURCHASE_DELETE").getStatusCodeValue()).isEqualTo(403);
        }
        verify(users, never()).save(any());
    }

    @Test void blocksCreatingStrongerCustomRoleAndAllowsHeldPermissions() {
        role("Finance Manager", "", Collections.singletonList(permission("PURCHASE", true)));
        assertThat(create("Finance Manager").getStatusCodeValue()).isEqualTo(403);
        verify(users, never()).save(any());
        login("USER_MANAGEMENT_EDIT", "PURCHASE_VIEW", "PURCHASE_DELETE");
        assertThat(create("Finance Manager").getStatusCodeValue()).isEqualTo(201);
    }

    @Test void legacyPermissionTextIsNotGrantedAsARoleAuthority() {
        User target = target("PURCHASE_DELETE");
        assertThat(service.toUserDetails(target).getAuthorities()).extracting("authority")
                .doesNotContain("PURCHASE_DELETE");
    }

    @Test void checksReportChoicesAndLegacyAllReportDefaults() {
        role("Daily", "DAILY_REPORT", Collections.singletonList(permission("REPORTS", false)));
        role("All reports", null, Collections.singletonList(permission("REPORTS", false)));
        login("USER_MANAGEMENT_EDIT", "REPORTS_VIEW", "REPORT_DAILY_REPORT");
        assertThat(create("Daily").getStatusCodeValue()).isEqualTo(201);
        assertThat(create("All reports").getStatusCodeValue()).isEqualTo(403);
    }

    @Test void adminCanAssignExistingStrongerRole() {
        role("Finance Manager", "", Collections.singletonList(permission("PURCHASE", true)));
        login("ADMIN");
        assertThat(create("Finance Manager").getStatusCodeValue()).isEqualTo(201);
    }

    @Test void updatesCannotInjectPermissionsOrPromoteAnotherAccount() {
        User target = target("Limited");
        role("Finance Manager", "", Collections.singletonList(permission("PURCHASE", true)));
        for (String role : new String[] {"PURCHASE_DELETE", "Finance Manager"}) {
            assertThat(controller.updateUser(2L, "target", role, "123", "target@example.com", true, "Target",
                    new MockHttpServletRequest(), new MockHttpServletResponse()).getStatusCodeValue()).isEqualTo(403);
            assertThat(target.getRole()).isEqualTo("Limited");
        }
        verify(users, never()).save(any());
    }

    @Test void cannotTakeOverStrongerAccountByPasswordOrProfileChanges() {
        role("Finance Manager", "", Collections.singletonList(permission("PURCHASE", true)));
        User target = target("Finance Manager");
        assertThat(controller.changePassword(2L, "chosen-password").getStatusCodeValue()).isEqualTo(403);
        assertThat(controller.updateUser(2L, "target", "Limited", "123", "changed@example.com", true, "Target",
                new MockHttpServletRequest(), new MockHttpServletResponse()).getStatusCodeValue()).isEqualTo(403);
        assertThat(controller.deleteUser(2L).getStatusCodeValue()).isEqualTo(403);
        assertThat(target.getRole()).isEqualTo("Finance Manager");
        verify(users, never()).updatePassword(anyLong(), anyString());
        verify(users, never()).save(any());
        verify(users, never()).deleteById(anyLong());
    }

    @Test void ordinaryProfileUpdatesAndPasswordResetsStillWork() {
        target("Limited");
        when(users.updatePassword(2L, "encoded")).thenReturn(1);
        assertThat(controller.updateUser(2L, "target", "Limited", "123", "target@example.com", true, "Target",
                new MockHttpServletRequest(), new MockHttpServletResponse()).getStatusCodeValue()).isEqualTo(200);
        assertThat(controller.changePassword(2L, "password").getStatusCodeValue()).isEqualTo(200);
    }

    @Test void rejectsDatabaseMatchesWithDifferentCaseOrPadding() {
        Role role = new Role(); role.setName("ADMIN");
        when(roles.findByName("ADMIN ")).thenReturn(role);
        assertThat(create("ADMIN ").getStatusCodeValue()).isEqualTo(403);
    }
}
