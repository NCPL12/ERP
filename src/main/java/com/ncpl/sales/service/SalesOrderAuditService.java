package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

	public void logAudit(String salesOrderId, String action, String performedBy, Object oldValues, Object newValues,
			String description, HttpServletRequest request) {
		try {
			SalesOrderAudit audit = new SalesOrderAudit();
			audit.setSalesOrderId(salesOrderId);
			audit.setAction(action);
			audit.setPerformedBy(performedBy);
			audit.setActionPerformed(new Timestamp(new Date().getTime()));
			audit.setDescription(description);

			if (oldValues != null) {
				audit.setOldValues(objectMapper.writeValueAsString(oldValues));
			}
			if (newValues != null) {
				audit.setNewValues(objectMapper.writeValueAsString(newValues));
			}

			if (request != null) {
				audit.setIpAddress(getClientIpAddress(request));
				audit.setSessionId(request.getSession().getId());
			}

			auditRepo.save(audit);
		} catch (Exception e) {
			// Log error but don't throw to avoid interrupting main operations
			System.err.println("Error logging audit: " + e.getMessage());
			e.printStackTrace();
		}
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
		
		// DEBUG: Track what's being passed to audit
		System.out.println("=== AUDIT SERVICE DEBUG ===");
		System.out.println("oldSalesOrder hash: " + System.identityHashCode(oldSalesOrder));
		System.out.println("newSalesOrder hash: " + System.identityHashCode(newSalesOrder));
		System.out.println("oldSalesOrder.shippingAddress: " + oldSalesOrder.getShippingAddress());
		System.out.println("newSalesOrder.shippingAddress: " + newSalesOrder.getShippingAddress());
		System.out.println("oldSalesOrder.billingAddress: " + oldSalesOrder.getBillingAddress());
		System.out.println("newSalesOrder.billingAddress: " + newSalesOrder.getBillingAddress());
		System.out.println("hasAddressChanges result: " + hasAddressChanges(oldSalesOrder, newSalesOrder));
		System.out.println("================================");
		
		// Create snapshots to ensure proper old/new value capture
		java.util.Map<String, Object> oldSnapshot = createSalesOrderSnapshot(oldSalesOrder);
		java.util.Map<String, Object> newSnapshot = createSalesOrderSnapshot(newSalesOrder);
		
		System.out.println("OLD SNAPSHOT: " + oldSnapshot);
		System.out.println("NEW SNAPSHOT: " + newSnapshot);
		
		// Check if this is specifically an address update
		if (hasAddressChanges(oldSalesOrder, newSalesOrder)) {
			System.out.println("DEBUG: Logging as ADDRESS_UPDATED");
			logAudit(newSalesOrder.getId(), ACTION_ADDRESS_UPDATED, performedBy, oldSnapshot, newSnapshot,
					"Address updated: " + newSalesOrder.getClientPoNumber(), request);
		} else {
			System.out.println("DEBUG: Logging as UPDATE");
			logAudit(newSalesOrder.getId(), ACTION_UPDATE, performedBy, oldSnapshot, newSnapshot,
					"Sales Order updated: " + newSalesOrder.getClientPoNumber(), request);
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
		logAudit(salesOrderId, ACTION_DELETE_ITEM, performedBy, salesItemId, null,
				"Sales Item deleted: " + salesItemId, request);
	}
	
	public void saveAuditLog(SalesOrderAudit audit) {
		auditRepo.save(audit);
	}
}
