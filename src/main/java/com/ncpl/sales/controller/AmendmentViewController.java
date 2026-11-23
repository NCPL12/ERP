package com.ncpl.sales.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.repository.RoleRepo;
import com.ncpl.sales.security.User;
import com.ncpl.sales.service.SalesService;
import com.ncpl.sales.security.UserService;

/**
 * Amendment view for the user chosen as "Send amendment mail to" on Roles & Access.
 * Shows the sales order with the amended items highlighted.
 */
@Controller
public class AmendmentViewController {

	@Autowired
	private SalesService salesService;
	@Autowired
	private RoleRepo roleRepo;
	@Autowired
	private UserService userService;

	@GetMapping("/amendment/{soId}")
	public String amendmentView(@PathVariable("soId") String soId, Model model, HttpServletRequest request) {
		User me = userService.getCurrentUser();
		boolean isRecipient = me != null && roleRepo.findAll().stream()
				.anyMatch(r -> me.getUsername().equals(r.getAmendmentMailUsername()));
		if (!isRecipient) {
			return "access-denied";
		}

		Optional<SalesOrder> soOpt = salesService.getSalesOrderById(soId);
		if (!soOpt.isPresent()) {
			model.addAttribute("error", "Sales order not found: " + soId);
			return "amendmentView";
		}

		// Show the normal SO edit page, switched to amendment mode (read-only, design on amended items).
		request.setAttribute("amendmentMode", Boolean.TRUE);
		return "forward:/edit_salesOrder/" + soId;
	}
}
