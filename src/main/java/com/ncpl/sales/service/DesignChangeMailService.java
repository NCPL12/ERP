package com.ncpl.sales.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Role;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.repository.RoleRepo;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserRepo;
import com.ncpl.sales.security.UserService;

/** One mail to the purchase team per design change, listing what went from what to what. */
@Service
public class DesignChangeMailService {

	private static final Logger log = LoggerFactory.getLogger(DesignChangeMailService.class);

	@Autowired
	private EmailService emailService;
	@Autowired
	private SalesService salesService;
	@Autowired
	private ItemMasterService itemService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private RoleRepo roleRepo;

	// Blank = mail off (so dev machines never mail the real team); prod sets it.
	@Value("${design.change.mail.to:}")
	private String recipients;

	/** Email of the user the acting user's role names for design mail; null if none set. */
	private String roleRecipient() {
		User actor = userService.getCurrentUser();
		Role role = actor == null || actor.getRole() == null ? null : roleRepo.findByName(actor.getRole());
		String username = role == null ? null : role.getDesignMailUsername();
		if (username == null) {
			// The acting user's own role (e.g. the amendment recipient's) has none: use the one set on any role.
			// shortcut: first role found wins; if roles need different recipients, set it on each role.
			username = roleRepo.findAll().stream().map(Role::getDesignMailUsername)
					.filter(u -> u != null && !u.trim().isEmpty()).findFirst().orElse(null);
		}
		if (username == null) {
			return null;
		}
		User target = userRepo.findUserByUserName(username);
		String email = target == null || target.getEmailId() == null ? "" : target.getEmailId().trim();
		return email.isEmpty() ? null : email;
	}

	/** Model name for an item-master id; falls back to the id. */
	public String modelOf(String itemMasterId) {
		Optional<ItemMaster> item = itemService.getItemById(itemMasterId);
		return item.isPresent() ? item.get().getModel() : itemMasterId;
	}

	public static String qty(float q) {
		return q == (long) q ? String.valueOf((long) q) : String.valueOf(q);
	}

	/** changes: one line per item, e.g. "Model X: qty 5 -> 8". Sent in the background; never breaks the save. */
	public void notifyChange(String salesItemId, List<String> changes) {
		if (changes == null || changes.isEmpty()) {
			return;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String user = auth == null ? "unknown" : auth.getName();
		// Role setting (Roles & Permissions) wins; the property is the fallback. Resolved here, on the request thread.
		String fromRole = roleRecipient();
		final String to = fromRole != null ? fromRole : (recipients == null ? "" : recipients.trim());
		if (to.isEmpty()) {
			log.warn("Design change mail not sent for sales item {}: no recipient (set it on the role, or design.change.mail.to)", salesItemId);
			return;
		}
		CompletableFuture.runAsync(() -> {
			try {
				Optional<SalesItem> si = salesService.getSalesItemObjById(salesItemId);
				String soId = si.isPresent() && si.get().getSalesOrder() != null ? si.get().getSalesOrder().getId() : "?";
				String clientPo = si.isPresent() && si.get().getSalesOrder() != null
						? String.valueOf(si.get().getSalesOrder().getClientPoNumber()) : "?";
				StringBuilder body = new StringBuilder();
				body.append("The design was updated.\n\n");
				body.append("Sales Order: ").append(soId).append('\n');
				body.append("Client PO: ").append(clientPo).append('\n');
				body.append("Line item: ").append(si.isPresent() ? si.get().getDescription() : salesItemId).append('\n');
				body.append("Changed by: ").append(user).append("\n\nChanges (from -> to):\n");
				for (String c : changes) {
					body.append(" - ").append(c).append('\n');
				}
				emailService.sendErrorAlert(to,"[ERP] Design updated for " + soId, body.toString());
			} catch (Exception e) {
				log.error("Could not send design change mail for sales item " + salesItemId, e);
			}
		});
	}
}
