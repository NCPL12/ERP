package com.ncpl.sales.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ncpl.sales.model.Module;
import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.model.RolePermissionDto;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import com.ncpl.sales.security.UserRepo;

public class RoleMenuModulesTest {

	/**
	 * savePermissions() consults the Spring SecurityContext (callerIsAdmin / callerHas).
	 * In a plain unit test there is none, so the caller looked like a non-admin without
	 * PARTY_VIEW and the controller returned 403 before ever calling saveAll().
	 * Run every test as an admin caller.
	 */
	@Before
	public void asAdmin() {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("admin", "x", Arrays.asList(
						new SimpleGrantedAuthority("ADMIN"),
						new SimpleGrantedAuthority("SUPER ADMIN"))));
	}

	@After
	public void clearSecurity() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void everyManagedModuleHasASidebarLink() throws Exception {
		String sidebar = new String(Files.readAllBytes(Paths.get(
				"src/main/webapp/WEB-INF/tiles/leftMenuBar.jsp")), StandardCharsets.UTF_8);
		for (Module module : Module.menuModules()) {
			assertTrue(module.name() + " has no sidebar link",
					sidebar.contains("request.contextPath}" + module.menuPath()));
		}
	}

	@Test
	public void roleSummaryCountsOnlySidebarModules() throws Exception {
		RoleController controller = new RoleController();
		RoleRepo roles = mock(RoleRepo.class);
		RolePermissionRepo permissions = mock(RolePermissionRepo.class);
		UserRepo users = mock(UserRepo.class);
		set(controller, "roleRepo", roles);
		set(controller, "rolePermissionRepo", permissions);
		set(controller, "userRepo", users);
		Role role = new Role();
		role.setRoleId(7L);
		role.setName("TEST");
		when(roles.findAll()).thenReturn(Arrays.asList(role));
		when(users.findAll()).thenReturn(Collections.emptyList());
		when(permissions.findByRole_RoleId(7L)).thenReturn(Arrays.asList(
				permission("PARTY", true), permission("COMPANY_ASSETS", true)));

		Map<String, Object> summary = controller.listRoles().get(0);
		assertEquals(1L, summary.get("moduleCount"));
		assertEquals(12, summary.get("totalModules"));
	}

	@Test
	public void gridOmitsModulesNotShownInTheSidebar() throws Exception {
		RoleController controller = new RoleController();
		RoleRepo roles = mock(RoleRepo.class);
		RolePermissionRepo permissions = mock(RolePermissionRepo.class);
		set(controller, "roleRepo", roles);
		set(controller, "rolePermissionRepo", permissions);
		when(roles.findById(7L)).thenReturn(Optional.of(new Role()));
		when(permissions.findByRole_RoleId(7L)).thenReturn(Arrays.asList(permission("COMPANY_ASSETS", true)));

		ResponseEntity<?> response = controller.getPermissions(7L);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> grid = (List<Map<String, Object>>) response.getBody();
		List<String> names = grid.stream().map(row -> (String) row.get("module")).collect(Collectors.toList());
		assertEquals(12, names.size());
		assertTrue(names.contains("PARTY"));
		assertFalse(names.contains("COMPANY_ASSETS"));
		assertFalse(names.contains("WORK_ORDER"));
		assertFalse(names.contains("NON_BILLABLE"));
	}

	@Test
	public void savingVisibleGridRetainsExistingHiddenPermissions() throws Exception {
		RoleController controller = new RoleController();
		RoleRepo roles = mock(RoleRepo.class);
		RolePermissionRepo permissions = mock(RolePermissionRepo.class);
		set(controller, "roleRepo", roles);
		set(controller, "rolePermissionRepo", permissions);

		Role role = new Role();
		role.setRoleId(7L);
		role.setName("TEST");
		when(roles.findById(7L)).thenReturn(Optional.of(role));
		when(permissions.findByRole_RoleId(7L)).thenReturn(Arrays.asList(permission("COMPANY_ASSETS", true)));

		RolePermissionDto party = new RolePermissionDto();
		party.setModule("PARTY");
		party.setCanView(true);
		// Not a sidebar module: the incoming value is ignored and the stored one is kept.
		RolePermissionDto hidden = new RolePermissionDto();
		hidden.setModule("COMPANY_ASSETS");
		hidden.setCanView(false);

		ResponseEntity<?> response = controller.savePermissions(7L, Arrays.asList(party, hidden));
		assertEquals(HttpStatus.OK, response.getStatusCode());

		verify(permissions).deleteByRole_RoleId(7L);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Iterable<RolePermission>> saved = ArgumentCaptor.forClass(Iterable.class);
		verify(permissions).saveAll(saved.capture());
		int count = 0;
		for (RolePermission row : saved.getValue()) {
			count++;
			assertTrue(row.isCanView());
			assertTrue(row.getModule().equals("PARTY") || row.getModule().equals("COMPANY_ASSETS"));
		}
		assertEquals(2, count);
	}

	private static RolePermission permission(String module, boolean canView) {
		RolePermission row = new RolePermission();
		row.setModule(module);
		row.setCanView(canView);
		return row;
	}

	private static void set(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
