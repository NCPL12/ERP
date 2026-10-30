package com.ncpl.sales.controller;

import static com.ncpl.sales.security.AccessUtil.callerHas;
import static com.ncpl.sales.security.AccessUtil.callerIsAdmin;
import static com.ncpl.sales.security.AccessUtil.isAdminRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ncpl.sales.model.DashboardTile;
import com.ncpl.sales.model.Module;
import com.ncpl.sales.model.ReportItem;
import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.model.RolePermissionDto;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserRepo;

@Controller
public class RoleController {
	private static final List<Module> MENU_MODULES = Module.menuModules();
	private static final Set<String> MENU_MODULE_NAMES = MENU_MODULES.stream()
			.map(Module::name).collect(java.util.stream.Collectors.toSet());

	@Autowired
	private RoleRepo roleRepo;
	@Autowired
	private RolePermissionRepo rolePermissionRepo;
	@Autowired
	private UserRepo userRepo;

	private static ResponseEntity<?> forbidden(String message) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
	}

	/**
	 * A user's role name is also one of their authorities (see UserService), so a role named like a
	 * permission ("PURCHASE_DELETE", "REPORT_...") would hand out that permission without the grid.
	 */
	private static boolean isPermissionLikeName(String name) {
		String n = name.trim().toUpperCase();
		for (Module m : Module.values()) {
			if (n.equals(m.name() + "_VIEW") || n.equals(m.name() + "_EDIT") || n.equals(m.name() + "_DELETE")) {
				return true;
			}
		}
		for (ReportItem r : ReportItem.values()) {
			if (n.equals("REPORT_" + r.name())) {
				return true;
			}
		}
		return false;
	}

	@GetMapping("/role-management")
	public String roleManagement(Model model) {
		return "roleManagement";
	}

	/** Roles for the left pane, with how many users hold each and how many modules they can view. */
	@GetMapping("/api/roles")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_VIEW')")
	public List<Map<String, Object>> listRoles() {
		Map<String, Long> userCountByRole = new HashMap<>();
		for (User u : userRepo.findAll()) {
			if (u.getRole() != null && u.isEnabled()) {
				userCountByRole.merge(u.getRole(), 1L, Long::sum);
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (Role role : roleRepo.findAll()) {
			long viewableModules = rolePermissionRepo.findByRole_RoleId(role.getRoleId()).stream()
					.filter(p -> MENU_MODULE_NAMES.contains(p.getModule()) && p.isCanView()).count();

			Map<String, Object> row = new HashMap<>();
			row.put("roleId", role.getRoleId());
			row.put("name", role.getName());
			row.put("userCount", userCountByRole.getOrDefault(role.getName(), 0L));
			row.put("moduleCount", viewableModules);
			row.put("totalModules", MENU_MODULES.size());
			rows.add(row);
		}
		return rows;
	}

	/** The permission grid for one role — one row per module, in Module enum order. */
	@GetMapping("/api/roles/{id}/permissions")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_VIEW')")
	public ResponseEntity<?> getPermissions(@PathVariable Long id) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}

		Map<String, RolePermission> byModule = new HashMap<>();
		for (RolePermission p : rolePermissionRepo.findByRole_RoleId(id)) {
			byModule.put(p.getModule(), p);
		}

		List<Map<String, Object>> grid = new ArrayList<>();
		for (Module module : MENU_MODULES) {
			RolePermission p = byModule.get(module.name());
			Map<String, Object> row = new HashMap<>();
			row.put("module", module.name());
			row.put("label", module.label());
			row.put("url", module.menuPath());
			row.put("editable", module.editable());
			row.put("deletable", module.deletable());
			row.put("canView", p != null && p.isCanView());
			row.put("canEdit", p != null && p.isCanEdit());
			row.put("canDelete", p != null && p.isCanDelete());
			grid.add(row);
		}
		return ResponseEntity.ok(grid);
	}

	@PostMapping("/api/roles")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_EDIT')")
	@Transactional
	public ResponseEntity<?> createRole(@RequestParam("name") String name) {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.isEmpty()) {
			return ResponseEntity.badRequest().body("Role name cannot be empty");
		}
		if (trimmed.length() > 50) {
			return ResponseEntity.badRequest().body("Role name cannot exceed 50 characters.");
		}
		if (isPermissionLikeName(trimmed)) {
			return ResponseEntity.badRequest().body("That role name is reserved. Please choose a different name.");
		}
		if (isAdminRole(trimmed) && !callerIsAdmin()) {
			return forbidden("Only an admin can create an admin role");
		}
		if (roleRepo.findByName(trimmed) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Role '" + trimmed + "' already exists.");
		}

		Role role = new Role();
		role.setName(trimmed);
		role = roleRepo.save(role);

		List<RolePermission> blankGrid = new ArrayList<>();
		for (Module module : MENU_MODULES) {
			RolePermission p = new RolePermission();
			p.setRole(role);
			p.setModule(module.name());
			blankGrid.add(p);
		}
		rolePermissionRepo.saveAll(blankGrid);

		return ResponseEntity.status(HttpStatus.CREATED).body(role);
	}

	/**
	 * Save the whole grid at once: delete-and-reinsert inside one transaction.
	 * A non-admin editor can only switch ON a permission they hold themselves (or one the role already had),
	 * and cannot touch an admin role. Otherwise they could give a role every permission and use it.
	 */
	@PutMapping("/api/roles/{id}/permissions")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_EDIT')")
	@Transactional
	public ResponseEntity<?> savePermissions(@PathVariable Long id, @RequestBody List<RolePermissionDto> rows) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		if (rows == null || rows.isEmpty()) {
			// Empty grid would wipe every permission — treat as bad request.
			return ResponseEntity.badRequest().body("Permission list cannot be empty.");
		}
		boolean admin = callerIsAdmin();
		if (!admin && isAdminRole(role.getName())) {
			return forbidden("Only an admin can change an admin role");
		}

		// Keep permissions for modules outside the current menu when editing a role.
		// They may be used by another workflow or restored to navigation later.
		List<RolePermission> retained = new ArrayList<>();
		for (RolePermission previous : rolePermissionRepo.findByRole_RoleId(id)) {
			if (!MENU_MODULE_NAMES.contains(previous.getModule())) {
				RolePermission copy = new RolePermission();
				copy.setRole(role);
				copy.setModule(previous.getModule());
				copy.setCanView(previous.isCanView());
				copy.setCanEdit(previous.isCanEdit());
				copy.setCanDelete(previous.isCanDelete());
				retained.add(copy);
			}
		}
		rolePermissionRepo.deleteByRole_RoleId(id);
		rolePermissionRepo.flush(); // IDENTITY inserts below run immediately — make sure the deletes land first

		java.util.Set<String> seenModules = new java.util.HashSet<>();
		List<RolePermission> entities = new ArrayList<>();
		List<String> denied = new ArrayList<>();
		for (RolePermissionDto row : rows) {
			if (row == null || row.getModule() == null) {
				continue;
			}
			String mod = row.getModule().trim();
			if (mod.isEmpty() || !MENU_MODULE_NAMES.contains(mod) || !seenModules.add(mod)) {
				continue;
			}
			boolean edit = row.isCanEdit();
			boolean del = row.isCanDelete();
			boolean view = row.isCanView() || edit || del;

			if (!admin) {
				RolePermission old = before.get(mod);
				if (view && !(callerHas(mod + "_VIEW") || (old != null && old.isCanView()))) {
					denied.add(mod + "_VIEW");
				}
				if (edit && !(callerHas(mod + "_EDIT") || (old != null && old.isCanEdit()))) {
					denied.add(mod + "_EDIT");
				}
				if (del && !(callerHas(mod + "_DELETE") || (old != null && old.isCanDelete()))) {
					denied.add(mod + "_DELETE");
				}
			}

			RolePermission p = new RolePermission();
			p.setRole(role);
			p.setModule(mod);
			p.setCanView(view);
			p.setCanEdit(edit);
			p.setCanDelete(del);
			entities.add(p);
		}
		entities.addAll(retained);
		rolePermissionRepo.saveAll(entities);

		return ResponseEntity.ok().build();
	}

	private boolean hasModuleView(Long roleId, String moduleName) {
		for (RolePermission p : rolePermissionRepo.findByRole_RoleId(roleId)) {
			if (moduleName.equals(p.getModule())) {
				return p.isCanView() || p.isCanEdit() || p.isCanDelete();
			}
		}
		return false;
	}

	/** Which of the 7 /dashboard tiles this role's users see. */
	@GetMapping("/api/roles/{id}/dashboard-tiles")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_VIEW')")
	public ResponseEntity<?> getDashboardTiles(@PathVariable Long id) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		Set<String> visible = DashboardTile.visible(role.getDashboardTiles());

		List<Map<String, Object>> tiles = new ArrayList<>();
		for (DashboardTile tile : DashboardTile.values()) {
			Map<String, Object> row = new HashMap<>();
			row.put("key", tile.name());
			row.put("label", tile.label());
			row.put("visible", visible.contains(tile.name()));
			tiles.add(row);
		}
		return ResponseEntity.ok(tiles);
	}

	@PutMapping("/api/roles/{id}/dashboard-tiles")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_EDIT')")
	public ResponseEntity<?> saveDashboardTiles(@PathVariable Long id, @RequestBody List<String> tileKeys) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		if (isAdminRole(role.getName()) && !callerIsAdmin()) {
			return forbidden("Only an admin can change an admin role");
		}
		java.util.Set<String> valid = new java.util.HashSet<>();
		for (com.ncpl.sales.model.DashboardTile tile : com.ncpl.sales.model.DashboardTile.values()) {
			valid.add(tile.name());
		}
		java.util.List<String> filtered = new java.util.ArrayList<>();
		if (tileKeys != null) {
			for (String k : tileKeys) {
				if (k != null) {
					String t = k.trim();
					if (!t.isEmpty() && valid.contains(t)) {
						filtered.add(t);
					}
				}
			}
		}
		// A role with Dashboard view on must keep at least one tile visible,
		// otherwise its /dashboard page renders completely empty.
		if (filtered.isEmpty() && hasModuleView(id, Module.DASHBOARD.name())) {
			return ResponseEntity.badRequest().body("At least one dashboard tile must be enabled.");
		}
		role.setDashboardTiles(String.join(",", filtered));
		roleRepo.save(role);
		return ResponseEntity.ok().build();
	}

	/** Which reports this role's users can open. */
	@GetMapping("/api/roles/{id}/report-items")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_VIEW')")
	public ResponseEntity<?> getReportItems(@PathVariable Long id) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		Set<String> visible = ReportItem.visible(role.getReportItems());

		List<Map<String, Object>> items = new ArrayList<>();
		for (ReportItem r : ReportItem.values()) {
			Map<String, Object> row = new HashMap<>();
			row.put("key", r.name());
			row.put("label", r.label());
			row.put("visible", visible.contains(r.name()));
			items.add(row);
		}
		return ResponseEntity.ok(items);
	}

	@PutMapping("/api/roles/{id}/report-items")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_EDIT')")
	public ResponseEntity<?> saveReportItems(@PathVariable Long id, @RequestBody List<String> keys) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		boolean admin = callerIsAdmin();
		if (!admin && isAdminRole(role.getName())) {
			return forbidden("Only an admin can change an admin role");
		}
		java.util.Set<String> valid = new java.util.HashSet<>();
		for (ReportItem r : ReportItem.values()) {
			valid.add(r.name());
		}
		Set<String> filtered = new java.util.LinkedHashSet<>();
		if (keys != null) {
			for (String k : keys) {
				if (k != null && valid.contains(k.trim())) {
					filtered.add(k.trim());
				}
			}
		}
		// Each report key is an authority (REPORT_<key>), so a non-admin editor can only switch on
		// reports they can open themselves, or ones the role already had.
		if (!admin) {
			Set<String> had = ReportItem.visible(role.getReportItems());
			for (String k : filtered) {
				if (!had.contains(k) && !callerHas("REPORT_" + k)) {
					return forbidden("You cannot grant a report you do not have access to: " + k);
				}
			}
		}
		// Same rule as dashboard tiles: Reports view on needs at least one report.
		if (filtered.isEmpty() && hasModuleView(id, Module.REPORTS.name())) {
			return ResponseEntity.badRequest().body("At least one report must be enabled.");
		}
		role.setReportItems(String.join(",", filtered));
		roleRepo.save(role);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/roles/{id}")
	@ResponseBody
	@PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_EDIT')")
	@Transactional
	public ResponseEntity<?> deleteRole(@PathVariable Long id) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		if (isAdminRole(role.getName()) && !callerIsAdmin()) {
			return forbidden("Only an admin can delete an admin role");
		}
		int assignedCount = 0;
		for (User u : userRepo.findAll()) {
			if (role.getName().equals(u.getRole()) && u.isEnabled()) {
				assignedCount++;
			}
		}
		if (assignedCount > 0) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Cannot delete role '" + role.getName() + "': still assigned to " + assignedCount + " active user" + (assignedCount == 1 ? "" : "s") + ".");
		}
		// Disabled users may still reference the role name — clear it so they don't dangle.
		for (User u : userRepo.findAll()) {
			if (role.getName().equals(u.getRole())) {
				u.setRole(null);
				userRepo.save(u);
			}
		}
		rolePermissionRepo.deleteByRole_RoleId(id);
		roleRepo.deleteById(id);
		return ResponseEntity.ok("Role deleted successfully");
	}
}