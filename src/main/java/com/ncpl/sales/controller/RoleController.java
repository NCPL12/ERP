package com.ncpl.sales.controller;

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
import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.model.RolePermissionDto;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserRepo;

@Controller
public class RoleController {

	@Autowired
	private RoleRepo roleRepo;
	@Autowired
	private RolePermissionRepo rolePermissionRepo;
	@Autowired
	private UserRepo userRepo;

	@GetMapping("/role-management")
	public String roleManagement(Model model) {
		return "roleManagement";
	}

	/** Roles for the left pane, with how many users hold each and how many modules they can view. */
	@GetMapping("/api/roles")
	@ResponseBody
	@PreAuthorize("hasAuthority('USER_MANAGEMENT_VIEW')")
	public List<Map<String, Object>> listRoles() {
		Map<String, Long> userCountByRole = new HashMap<>();
		for (User u : userRepo.findAll()) {
			if (u.getRole() != null) {
				userCountByRole.merge(u.getRole(), 1L, Long::sum);
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (Role role : roleRepo.findAll()) {
			long viewableModules = rolePermissionRepo.findByRole_RoleId(role.getRoleId()).stream()
					.filter(RolePermission::isCanView).count();

			Map<String, Object> row = new HashMap<>();
			row.put("roleId", role.getRoleId());
			row.put("name", role.getName());
			row.put("userCount", userCountByRole.getOrDefault(role.getName(), 0L));
			row.put("moduleCount", viewableModules);
			row.put("totalModules", Module.values().length);
			rows.add(row);
		}
		return rows;
	}

	/** The permission grid for one role — one row per module, in Module enum order. */
	@GetMapping("/api/roles/{id}/permissions")
	@ResponseBody
	@PreAuthorize("hasAuthority('USER_MANAGEMENT_VIEW')")
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
		for (Module module : Module.values()) {
			RolePermission p = byModule.get(module.name());
			Map<String, Object> row = new HashMap<>();
			row.put("module", module.name());
			row.put("label", module.label());
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
	@PreAuthorize("hasAuthority('USER_MANAGEMENT_EDIT')")
	@Transactional
	public ResponseEntity<?> createRole(@RequestParam("name") String name) {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.isEmpty()) {
			return ResponseEntity.badRequest().body("Role name cannot be empty");
		}
		if (roleRepo.findByName(trimmed) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Role '" + trimmed + "' already exists.");
		}

		Role role = new Role();
		role.setName(trimmed);
		role = roleRepo.save(role);

		List<RolePermission> blankGrid = new ArrayList<>();
		for (Module module : Module.values()) {
			RolePermission p = new RolePermission();
			p.setRole(role);
			p.setModule(module.name());
			blankGrid.add(p);
		}
		rolePermissionRepo.saveAll(blankGrid);

		return ResponseEntity.status(HttpStatus.CREATED).body(role);
	}

	/** Save the whole grid at once: delete-and-reinsert inside one transaction. */
	@PutMapping("/api/roles/{id}/permissions")
	@ResponseBody
	@PreAuthorize("hasAuthority('USER_MANAGEMENT_EDIT')")
	@Transactional
	public ResponseEntity<?> savePermissions(@PathVariable Long id, @RequestBody List<RolePermissionDto> rows) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}

		rolePermissionRepo.deleteByRole_RoleId(id);
		rolePermissionRepo.flush(); // IDENTITY inserts below run immediately — make sure the deletes land first

		List<RolePermission> entities = new ArrayList<>();
		for (RolePermissionDto row : rows) {
			RolePermission p = new RolePermission();
			p.setRole(role);
			p.setModule(row.getModule());
			// Ticking Edit or Delete implies View — enforced here too, not just in the checkbox handler.
			boolean view = row.isCanView() || row.isCanEdit() || row.isCanDelete();
			p.setCanView(view);
			p.setCanEdit(row.isCanEdit());
			p.setCanDelete(row.isCanDelete());
			entities.add(p);
		}
		rolePermissionRepo.saveAll(entities);

		return ResponseEntity.ok().build();
	}

	/** Which of the 7 /dashboard tiles this role's users see. */
	@GetMapping("/api/roles/{id}/dashboard-tiles")
	@ResponseBody
	@PreAuthorize("hasAuthority('USER_MANAGEMENT_VIEW')")
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
	@PreAuthorize("hasAuthority('USER_MANAGEMENT_EDIT')")
	public ResponseEntity<?> saveDashboardTiles(@PathVariable Long id, @RequestBody List<String> tileKeys) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		role.setDashboardTiles(String.join(",", tileKeys));
		roleRepo.save(role);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/api/roles/{id}")
	@ResponseBody
	@PreAuthorize("hasAuthority('USER_MANAGEMENT_EDIT')")
	@Transactional
	public ResponseEntity<?> deleteRole(@PathVariable Long id) {
		Role role = roleRepo.findById(id).orElse(null);
		if (role == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found with ID: " + id);
		}
		for (User u : userRepo.findAll()) {
			if (role.getName().equals(u.getRole())) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body("Cannot delete role '" + role.getName() + "': still assigned to " + "existing users.");
			}
		}
		rolePermissionRepo.deleteByRole_RoleId(id);
		roleRepo.deleteById(id);
		return ResponseEntity.ok("Role deleted successfully");
	}
}
