package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderAudit;
import com.ncpl.sales.repository.SalesOrderAuditRepo;

@Service
public class SalesOrderAuditService {

	@Autowired
	private SalesOrderAuditRepo auditRepo;

	@Autowired
	private ObjectMapper objectMapper;

	public static final String ACTION_CREATE = "CREATE";
	public static final String ACTION_UPDATE = "UPDATE";
	public static final String ACTION_ARCHIVE = "ARCHIVE";
	public static final String ACTION_UNARCHIVE = "UNARCHIVE";
	public static final String ACTION_DELETE_ITEM = "DELETE_ITEM";
	public static final String ACTION_ADDRESS_UPDATED = "ADDRESS_UPDATED";

	/** Per–sales-line audit (tbl_sales_order_audit) */
	public static final String ACTION_CREATE_SALES_ITEM = "CREATE_SALES_ITEM";
	public static final String ACTION_UPDATE_SALES_ITEM = "UPDATE_SALES_ITEM";
	public static final String ACTION_DELETE_SALES_ITEM = "DELETE_SALES_ITEM";

	/**
	 * Excluded from line-item diff equality: JPA refreshes these on every cascade save even when the user did
	 * not change the line, which caused spurious UPDATE_SALES_ITEM rows for all lines.
	 */
	private static final Set<String> SALES_ITEM_DIFF_IGNORE_KEYS;
	static {
		Set<String> s = new HashSet<>();
		s.add("created");
		s.add("updated");
		s.add("createdBy");
		s.add("lastModifiedBy");
		SALES_ITEM_DIFF_IGNORE_KEYS = Collections.unmodifiableSet(s);
	}

	/** Map copy for comparison only; logged audit rows still use full {@link #toAuditMap(SalesItem)}. */
	private static Map<String, Object> salesItemMapForDiffCompare(Map<String, Object> full) {
		if (full == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> m = new LinkedHashMap<>();
		for (Map.Entry<String, Object> e : full.entrySet()) {
			if (!SALES_ITEM_DIFF_IGNORE_KEYS.contains(e.getKey())) {
				m.put(e.getKey(), e.getValue());
			}
		}
		return m;
	}

	public void logAudit(String salesOrderId, String action, String performedBy, Object oldValues, Object newValues,
			String description, HttpServletRequest request) {
		logAudit(salesOrderId, action, performedBy, oldValues, newValues, description, request, null);
	}

	public void logAudit(String salesOrderId, String action, String performedBy, Object oldValues, Object newValues,
			String description, HttpServletRequest request, String salesItemId) {
		try {
			HttpServletRequest req = request != null ? request : resolveCurrentRequest();

			SalesOrderAudit audit = new SalesOrderAudit();
			audit.setSalesOrderId(salesOrderId);
			audit.setAction(action);
			audit.setPerformedBy(resolvePerformedBy(performedBy));
			audit.setActionPerformed(new Timestamp(System.currentTimeMillis()));
			audit.setDescription(description);
			if (salesItemId != null && !salesItemId.trim().isEmpty()) {
				audit.setSalesItemId(salesItemId.trim());
			}

			if (oldValues != null) {
				audit.setOldValues(objectMapper.writeValueAsString(oldValues));
			}
			if (newValues != null) {
				audit.setNewValues(objectMapper.writeValueAsString(newValues));
			}

			if (req != null) {
				audit.setIpAddress(getClientIpAddress(req));
				audit.setSessionId(req.getSession().getId());
			}

			auditRepo.save(audit);
		} catch (Exception e) {
			// Log error but don't throw to avoid interrupting main operations
			System.err.println("Error logging audit: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Current HTTP request when code runs in a web request thread (even if callers pass null).
	 */
	private HttpServletRequest resolveCurrentRequest() {
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			return attrs != null ? attrs.getRequest() : null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Prefer explicit username; otherwise Spring Security principal; otherwise "system".
	 */
	private String resolvePerformedBy(String performedBy) {
		if (performedBy != null) {
			String t = performedBy.trim();
			if (!t.isEmpty()) {
				return t;
			}
		}
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.isAuthenticated() && auth.getName() != null && !auth.getName().isEmpty()) {
				return auth.getName();
			}
		} catch (Exception ignored) {
		}
		return "system";
	}

	public void logAudit(String salesOrderId, String action, String performedBy, Object oldValues, Object newValues,
			String description) {
		logAudit(salesOrderId, action, performedBy, oldValues, newValues, description, null);
	}

	public List<SalesOrderAudit> getAuditBySalesOrderId(String salesOrderId) {
		return auditRepo.findBySalesOrderId(salesOrderId);
	}

	public List<SalesOrderAudit> getAuditByPerformedBy(String performedBy) {
		return auditRepo.findByPerformedBy(performedBy);
	}

	public List<SalesOrderAudit> getAuditByAction(String action) {
		return auditRepo.findByAction(action);
	}

	public List<SalesOrderAudit> getAuditByDateRange(Timestamp startDate, Timestamp endDate) {
		return auditRepo.findByDateRange(startDate, endDate);
	}

	public List<SalesOrderAudit> getAuditByMultipleCriteria(String salesOrderId, String performedBy, String action,
			Timestamp startDate, Timestamp endDate) {
		return auditRepo.findByMultipleCriteria(salesOrderId, performedBy, action, startDate, endDate);
	}

	public List<SalesOrderAudit> getAllAuditLogs() {
		return auditRepo.findAll();
	}

	private String getClientIpAddress(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
			return xForwardedFor.split(",")[0];
		}
		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
			return xRealIp;
		}
		return request.getRemoteAddr();
	}

	public void logSalesOrderCreation(SalesOrder salesOrder, String performedBy, HttpServletRequest request) {
		logAudit(salesOrder.getId(), ACTION_CREATE, performedBy, null, salesOrder,
				"Sales Order created: " + salesOrder.getClientPoNumber(), request);
	}

	public void logSalesOrderUpdate(SalesOrder oldSalesOrder, SalesOrder newSalesOrder, String performedBy,
			HttpServletRequest request) {

		java.util.Map<String, Object> oldSnapshot = createSalesOrderSnapshot(oldSalesOrder);
		java.util.Map<String, Object> newSnapshot = createSalesOrderSnapshot(newSalesOrder);

		if (hasAddressChanges(oldSalesOrder, newSalesOrder)) {
			logAudit(newSalesOrder.getId(), ACTION_ADDRESS_UPDATED, performedBy, oldSnapshot, newSnapshot,
					"Address updated: " + newSalesOrder.getClientPoNumber(), request);
		} else {
			logAudit(newSalesOrder.getId(), ACTION_UPDATE, performedBy, oldSnapshot, newSnapshot,
					"Sales Order updated: " + newSalesOrder.getClientPoNumber(), request);
		}
	}

	/**
	 * JSON-safe snapshot of a sales line (no lazy graphs beyond item_units).
	 */
	public Map<String, Object> toAuditMap(SalesItem item) {
		Map<String, Object> m = new LinkedHashMap<>();
		if (item == null) {
			return m;
		}
		m.put("id", item.getId());
		m.put("salesItemId", item.getId());
		m.put("description", item.getDescription());
		m.put("hsnCode", item.getHsnCode());
		m.put("servicehsnCode", item.getServicehsnCode());
		m.put("slNo", item.getSlNo());
		m.put("modelNo", item.getModelNo());
		m.put("quantity", item.getQuantity());
		m.put("unitPrice", item.getUnitPrice());
		m.put("amount", item.getAmount());
		m.put("servicePrice", item.getServicePrice());
		m.put("status", item.getStatus());
		m.put("archive", item.isArchive());
		m.put("amendedQuantity", item.getAmendedQuantity());
		if (item.getItem_units() != null) {
			m.put("unitsId", item.getItem_units().getId());
			m.put("unitsName", item.getItem_units().getName());
		} else {
			m.put("unitsId", null);
			m.put("unitsName", null);
		}
		m.put("salesOrderId", item.getSalesOrder() != null ? item.getSalesOrder().getId() : null);
		m.put("created", item.getCreated());
		m.put("updated", item.getUpdated());
		m.put("createdBy", item.getCreatedBy());
		m.put("lastModifiedBy", item.getLastModifiedBy());
		return m;
	}

	public Map<String, Map<String, Object>> buildSalesItemSnapshotMap(List<SalesItem> items) {
		Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
		if (items == null) {
			return byId;
		}
		for (SalesItem it : items) {
			if (it != null && it.getId() != null) {
				byId.put(it.getId(), toAuditMap(it));
			}
		}
		return byId;
	}

	public void logSalesItemChange(String salesOrderId, String salesItemId, String action, String performedBy,
			Object oldPayload, Object newPayload, String description, HttpServletRequest request) {
		logAudit(salesOrderId, action, performedBy, oldPayload, newPayload, description, request, salesItemId);
	}

	/**
	 * Compares snapshots built with the same rules (non-archived lines only in both lists when loaded from
	 * {@link com.ncpl.sales.model.SalesOrder#getItems()}).
	 */
	public void diffAndLogSalesItemChanges(String salesOrderId, Map<String, Map<String, Object>> oldById,
			Map<String, Map<String, Object>> newById, String performedBy, HttpServletRequest request) {
		Map<String, Map<String, Object>> oldM = oldById != null ? oldById : Collections.emptyMap();
		Map<String, Map<String, Object>> newM = newById != null ? newById : Collections.emptyMap();

		for (String id : newM.keySet()) {
			if (!oldM.containsKey(id)) {
				logSalesItemChange(salesOrderId, id, ACTION_CREATE_SALES_ITEM, performedBy, null, newM.get(id),
						"Sales line created: " + id, request);
			}
		}
		Map<String, Object> removedMarker = new HashMap<>();
		removedMarker.put("removed", true);
		for (String id : oldM.keySet()) {
			if (!newM.containsKey(id)) {
				logSalesItemChange(salesOrderId, id, ACTION_DELETE_SALES_ITEM, performedBy, oldM.get(id), removedMarker,
						"Sales line removed: " + id, request);
			}
		}
		for (String id : oldM.keySet()) {
			if (!newM.containsKey(id)) {
				continue;
			}
			Map<String, Object> o = oldM.get(id);
			Map<String, Object> n = newM.get(id);
			if (!Objects.equals(salesItemMapForDiffCompare(o), salesItemMapForDiffCompare(n))) {
				logSalesItemChange(salesOrderId, id, ACTION_UPDATE_SALES_ITEM, performedBy, o, n,
						"Sales line updated: " + id, request);
			}
		}
	}

	public void logCreatedSalesItemsAfterSave(SalesOrder savedOrder, String performedBy, HttpServletRequest request) {
		if (savedOrder == null || savedOrder.getItems() == null) {
			return;
		}
		for (SalesItem it : savedOrder.getItems()) {
			if (it == null || it.getId() == null) {
				continue;
			}
			logSalesItemChange(savedOrder.getId(), it.getId(), ACTION_CREATE_SALES_ITEM, performedBy, null,
					toAuditMap(it), "Sales line created: " + it.getId(), request);
		}
	}
	
	/**
	 * Check if shipping or billing addresses have changed between old and new sales orders
	 */
	private boolean hasAddressChanges(SalesOrder oldOrder, SalesOrder newOrder) {
		if (oldOrder == null || newOrder == null) {
			return false;
		}
		
		// Check shipping address changes
		String oldShipping = oldOrder.getShippingAddress();
		String newShipping = newOrder.getShippingAddress();
		
		// Handle null values and trim whitespace to avoid false positives
		String oldShippingClean = (oldShipping != null) ? oldShipping.trim() : "";
		String newShippingClean = (newShipping != null) ? newShipping.trim() : "";
		boolean shippingChanged = !java.util.Objects.equals(oldShippingClean, newShippingClean);
		
		// Check billing address changes
		String oldBilling = oldOrder.getBillingAddress();
		String newBilling = newOrder.getBillingAddress();
		
		// Handle null values and trim whitespace to avoid false positives
		String oldBillingClean = (oldBilling != null) ? oldBilling.trim() : "";
		String newBillingClean = (newBilling != null) ? newBilling.trim() : "";
		boolean billingChanged = !java.util.Objects.equals(oldBillingClean, newBillingClean);
		
		return shippingChanged || billingChanged;
	}
	
	/**
	 * Create a snapshot map of sales order fields for audit purposes
	 */
	private java.util.Map<String, Object> createSalesOrderSnapshot(SalesOrder salesOrder) {
		java.util.Map<String, Object> snapshot = new java.util.HashMap<>();
		
		if (salesOrder != null) {
			snapshot.put("id", salesOrder.getId());
			snapshot.put("city", salesOrder.getCity());
			snapshot.put("total", salesOrder.getTotal());
			snapshot.put("totalItems", salesOrder.getTotalItems());
			snapshot.put("gst", salesOrder.getGst());
			snapshot.put("grandTotal", salesOrder.getGrandTotal());
			snapshot.put("shippingAddress", salesOrder.getShippingAddress());
			snapshot.put("billingAddress", salesOrder.getBillingAddress());
			snapshot.put("clientPoNumber", salesOrder.getClientPoNumber());
			snapshot.put("clientPoDate", salesOrder.getClientPoDate());
			snapshot.put("projectClosureDate", salesOrder.getProjectClosureDate());
			snapshot.put("otherTermsAndConditions", salesOrder.getOtherTermsAndConditions());
			snapshot.put("modeOfPayment", salesOrder.getModeOfPayment());
			snapshot.put("jurisdiction", salesOrder.getJurisdiction());
			snapshot.put("freight", salesOrder.getFreight());
			snapshot.put("delivery", salesOrder.getDelivery());
			snapshot.put("created", salesOrder.getCreated());
			snapshot.put("updated", salesOrder.getUpdated());
			snapshot.put("createdBy", salesOrder.getCreatedBy());
			snapshot.put("lastModifiedBy", salesOrder.getLastModifiedBy());
		}
		
		return snapshot;
	}

	public void logSalesOrderArchive(String salesOrderId, String performedBy, HttpServletRequest request) {
		logAudit(salesOrderId, ACTION_ARCHIVE, performedBy, null, null,
				"Sales Order archived: " + salesOrderId, request);
	}

	public void logSalesOrderUnarchive(String salesOrderId, String performedBy, HttpServletRequest request) {
		logAudit(salesOrderId, ACTION_UNARCHIVE, performedBy, null, null,
				"Sales Order unarchived: " + salesOrderId, request);
	}

	public void logSalesItemDeletion(String salesItemId, String salesOrderId, String performedBy,
			HttpServletRequest request) {
		Map<String, Object> removed = new HashMap<>();
		removed.put("removed", true);
		logAudit(salesOrderId, ACTION_DELETE_SALES_ITEM, performedBy,
				Collections.singletonMap("salesItemId", salesItemId), removed,
				"Sales Item deleted: " + salesItemId, request, salesItemId);
	}
	
	public void saveAuditLog(SalesOrderAudit audit) {
		if (audit == null) {
			return;
		}
		if (audit.getActionPerformed() == null) {
			audit.setActionPerformed(new Timestamp(System.currentTimeMillis()));
		}
		audit.setPerformedBy(resolvePerformedBy(audit.getPerformedBy()));
		HttpServletRequest req = resolveCurrentRequest();
		if (req != null) {
			if (audit.getIpAddress() == null || audit.getIpAddress().isEmpty()) {
				audit.setIpAddress(getClientIpAddress(req));
			}
			if (audit.getSessionId() == null || audit.getSessionId().isEmpty()) {
				audit.setSessionId(req.getSession().getId());
			}
		}
		auditRepo.save(audit);
	}
}
