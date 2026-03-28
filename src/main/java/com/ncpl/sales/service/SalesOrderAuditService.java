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
		logAudit(newSalesOrder.getId(), ACTION_UPDATE, performedBy, oldSalesOrder, newSalesOrder,
				"Sales Order updated: " + newSalesOrder.getClientPoNumber(), request);
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
