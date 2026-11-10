package com.ncpl.sales.config;

import java.util.EnumSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ncpl.sales.model.ArchiveItem;
import com.ncpl.sales.model.Module;
import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.RolePermission;
import com.ncpl.sales.repository.RolePermissionRepo;
import com.ncpl.sales.repository.RoleRepo;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserRepo;

/**
 * First-run seed for the Role & Permission module: one `role` row per distinct
 * users.role value already in use, and a `role_permission` grid that mirrors
 * exactly which modules each role can reach today (read off leftMenuBar.jsp's
 * existing hasAnyAuthority(...) gates). Existing roles receive missing permission upgrades.
 */
@Component
public class RolePermissionSeeder implements CommandLineRunner {
	private static final String ALL_ARCHIVE_ITEMS = Arrays.stream(ArchiveItem.values())
			.map(ArchiveItem::name).collect(Collectors.joining(","));

	@Autowired
	private UserRepo userRepo;
	@Autowired
	private RoleRepo roleRepo;
	@Autowired
	private RolePermissionRepo rolePermissionRepo;

	// Module access per role, exactly as leftMenuBar.jsp's hasAnyAuthority(...) gates read today.
	private static final Map<String, Set<Module>> TODAYS_ACCESS = new HashMap<>();
	static {
		TODAYS_ACCESS.put("SUPER ADMIN", EnumSet.allOf(Module.class));
		TODAYS_ACCESS.put("ADMIN", EnumSet.allOf(Module.class));
		TODAYS_ACCESS.put("NORMAL USER", EnumSet.of(
				Module.DASHBOARD, Module.PARTY, Module.ITEM_MASTER, Module.COMPANY_ASSETS,
				Module.SALES_ORDER, Module.DELIVERY_CHALLAN, Module.RETURNABLE, Module.INVOICE,
				Module.WORK_ORDER, Module.PURCHASE, Module.GRN, Module.NON_BILLABLE,
				Module.REPORTS, Module.ARCHIVES));
		TODAYS_ACCESS.put("PURCHASE", EnumSet.of(
				Module.DASHBOARD, Module.PARTY, Module.ITEM_MASTER, Module.COMPANY_ASSETS,
				Module.SALES_ORDER, Module.DELIVERY_CHALLAN, Module.RETURNABLE, Module.INVOICE,
				Module.WORK_ORDER, Module.PURCHASE, Module.GRN, Module.NON_BILLABLE,
				Module.REPORTS, Module.ARCHIVES));
		TODAYS_ACCESS.put("STORE", EnumSet.of(
				Module.ITEM_MASTER, Module.COMPANY_ASSETS, Module.SALES_ORDER, Module.DELIVERY_CHALLAN,
				Module.PURCHASE, Module.GRN, Module.NON_BILLABLE, Module.REPORTS, Module.ARCHIVES));
		TODAYS_ACCESS.put("STORE USER", EnumSet.of(
				Module.ITEM_MASTER, Module.COMPANY_ASSETS, Module.DELIVERY_CHALLAN, Module.PURCHASE, Module.GRN));
		TODAYS_ACCESS.put("ITEMMASTER", EnumSet.of(Module.ITEM_MASTER, Module.COMPANY_ASSETS));
		TODAYS_ACCESS.put("PURCHASE STORE", EnumSet.of(
				Module.PARTY, Module.ITEM_MASTER, Module.COMPANY_ASSETS, Module.SALES_ORDER,
				Module.DELIVERY_CHALLAN, Module.RETURNABLE, Module.PURCHASE, Module.GRN,
				Module.REPORTS, Module.ARCHIVES));
		TODAYS_ACCESS.put("SALES", EnumSet.of(
				Module.SALES_ORDER, Module.DELIVERY_CHALLAN, Module.RETURNABLE, Module.REPORTS));
	}

	// dashboard.jsp used to hardcode !hasAuthority('PURCHASE') around 4 of the 7 tiles;
	// this is that same cutdown, expressed as data instead of a role name in the JSP.
	private static final Map<String, String> TODAYS_DASHBOARD_TILES = new HashMap<>();
	static {
		TODAYS_DASHBOARD_TILES.put("PURCHASE", "PURCHASE_ORDER,SO_WITH_DESIGN");
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (roleRepo.count() > 0) {
			backfillArchiveAndChartPermissions();
			return;
		}

		java.util.Set<String> roleNames = new java.util.LinkedHashSet<>();
		for (User u : userRepo.findAll()) {
			if (u.getRole() != null && !u.getRole().isEmpty()) {
				roleNames.add(u.getRole());
			}
		}
		// Make sure every role named in the access map above also gets a row,
		// even if no user currently holds it.
		roleNames.addAll(TODAYS_ACCESS.keySet());

		for (String roleName : roleNames) {
			Set<Module> accessible = TODAYS_ACCESS.getOrDefault(roleName, EnumSet.noneOf(Module.class));
			Role role = new Role();
			role.setName(roleName);
			role.setDashboardTiles(TODAYS_DASHBOARD_TILES.get(roleName));
			role.setArchiveItems(accessible.contains(Module.ARCHIVES) ? ALL_ARCHIVE_ITEMS : "");
			role = roleRepo.save(role);

			for (Module module : Module.menuModules()) {
				RolePermission p = new RolePermission();
				p.setRole(role);
				p.setModule(module.name());
				boolean view = accessible.contains(module)
						|| (module == Module.CHARTS && accessible.contains(Module.SALES_ORDER));
				p.setCanView(view);
				p.setCanEdit(view && module.editable());
				p.setCanDelete(view && module.deletable());
				rolePermissionRepo.save(p);
			}
		}
	}

	private void backfillArchiveAndChartPermissions() {
		for (Role role : roleRepo.findAll()) {
			boolean archivesView = false;
			boolean salesView = false;
			boolean chartsConfigured = false;
			for (RolePermission permission : rolePermissionRepo.findByRole_RoleId(role.getRoleId())) {
				if (Module.ARCHIVES.name().equals(permission.getModule())) archivesView = permission.isCanView();
				if (Module.SALES_ORDER.name().equals(permission.getModule())) salesView = permission.isCanView();
				if (Module.CHARTS.name().equals(permission.getModule())) chartsConfigured = true;
			}
			// NULL means pre-upgrade; an empty string is an explicit choice to allow none.
			if (role.getArchiveItems() == null) {
				role.setArchiveItems(archivesView ? ALL_ARCHIVE_ITEMS : "");
				roleRepo.save(role);
			}
			if (!chartsConfigured) {
				RolePermission charts = new RolePermission();
				charts.setRole(role);
				charts.setModule(Module.CHARTS.name());
				charts.setCanView(salesView);
				rolePermissionRepo.save(charts);
			}
		}
	}
}
