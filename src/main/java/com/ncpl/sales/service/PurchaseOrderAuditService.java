package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.PurchaseOrderAudit;
import com.ncpl.sales.repository.PurchaseOrderAuditRepo;

// @D0017 Purchase Order audit log, mirrors SalesOrderAuditService (see README.md)
@Service
public class PurchaseOrderAuditService {

	@Autowired
	private PurchaseOrderAuditRepo auditRepo;

	@Autowired
	private ObjectMapper objectMapper;

	public static final String ACTION_CREATE = "CREATE";
	public static final String ACTION_UPDATE = "UPDATE";
	public static final String ACTION_ARCHIVE = "ARCHIVE";
	public static final String ACTION_UNARCHIVE = "UNARCHIVE";

	/** Per-purchase-line audit (tbl_purchase_order_audit) */
	public static final String ACTION_CREATE_PURCHASE_ITEM = "CREATE_PURCHASE_ITEM";
	public static final String ACTION_UPDATE_PURCHASE_ITEM = "UPDATE_PURCHASE_ITEM";
	public static final String ACTION_DELETE_PURCHASE_ITEM = "DELETE_PURCHASE_ITEM";

	public void logAudit(String poNumber, String action, String performedBy, Object oldValues, Object newValues,
			String description, HttpServletRequest request) {
		logAudit(poNumber, action, performedBy, oldValues, newValues, description, request, null);
	}

	public void logAudit(String poNumber, String action, String performedBy, Object oldValues, Object newValues,
			String description, HttpServletRequest request, Integer purchaseItemId) {
		try {
			HttpServletRequest req = request != null ? request : resolveCurrentRequest();

			PurchaseOrderAudit audit = new PurchaseOrderAudit();
			audit.setPoNumber(poNumber);
			audit.setAction(action);
			audit.setPerformedBy(resolvePerformedBy(performedBy));
			audit.setActionPerformed(new Timestamp(System.currentTimeMillis()));
			audit.setDescription(description);
			if (purchaseItemId != null) {
				audit.setPurchaseItemId(purchaseItemId);
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
			System.err.println("Error logging PO audit: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private HttpServletRequest resolveCurrentRequest() {
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			return attrs != null ? attrs.getRequest() : null;
		} catch (Exception e) {
			return null;
		}
	}

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

	public List<PurchaseOrderAudit> getAuditByPoNumber(String poNumber) {
		return auditRepo.findByPoNumber(poNumber);
	}

	public List<PurchaseOrderAudit> getAuditByPerformedBy(String performedBy) {
		return auditRepo.findByPerformedBy(performedBy);
	}

	public List<PurchaseOrderAudit> getAuditByAction(String action) {
		return auditRepo.findByAction(action);
	}

	public List<PurchaseOrderAudit> getAuditByDateRange(Timestamp startDate, Timestamp endDate) {
		return auditRepo.findByDateRange(startDate, endDate);
	}

	public List<PurchaseOrderAudit> getAuditByMultipleCriteria(String poNumber, String performedBy, String action,
			Timestamp startDate, Timestamp endDate) {
		return auditRepo.findByMultipleCriteria(poNumber, performedBy, action, startDate, endDate);
	}

	public List<PurchaseOrderAudit> getAllAuditLogs() {
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
		String ip = request.getRemoteAddr();
		return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
	}

	public void logPurchaseOrderCreation(PurchaseOrder purchaseOrder, String performedBy, HttpServletRequest request) {
		logAudit(purchaseOrder.getPoNumber(), ACTION_CREATE, performedBy, null,
				createPurchaseOrderSnapshot(purchaseOrder),
				"Purchase Order created: " + purchaseOrder.getPoNumber(), request);
	}

	public void logPurchaseOrderUpdate(PurchaseOrder oldPo, PurchaseOrder newPo, String performedBy,
			HttpServletRequest request) {
		Map<String, Object> oldSnapshot = createPurchaseOrderSnapshot(oldPo);
		Map<String, Object> newSnapshot = createPurchaseOrderSnapshot(newPo);
		logAudit(newPo.getPoNumber(), ACTION_UPDATE, performedBy, oldSnapshot, newSnapshot,
				"Purchase Order updated: " + newPo.getPoNumber(), request);
	}

	public void logPurchaseOrderArchive(String poNumber, String performedBy, HttpServletRequest request) {
		logAudit(poNumber, ACTION_ARCHIVE, performedBy, null, null,
				"Purchase Order archived: " + poNumber, request);
	}

	public void logPurchaseOrderUnarchive(String poNumber, String performedBy, HttpServletRequest request) {
		logAudit(poNumber, ACTION_UNARCHIVE, performedBy, null, null,
				"Purchase Order unarchived: " + poNumber, request);
	}

	/** JSON-safe snapshot of a purchase order header (no lazy item graph). */
	private Map<String, Object> createPurchaseOrderSnapshot(PurchaseOrder po) {
		Map<String, Object> snapshot = new HashMap<>();
		if (po != null) {
			snapshot.put("poNumber", po.getPoNumber());
			snapshot.put("archive", po.isArchive());
			snapshot.put("version", po.getVersion());
			snapshot.put("partyId", po.getParty() != null ? po.getParty().getId() : null);
			snapshot.put("partyName", po.getParty() != null ? po.getParty().getPartyName() : null);
			snapshot.put("itemCount", po.getItems() != null ? po.getItems().size() : 0);
		}
		return snapshot;
	}

	/** JSON-safe snapshot of a purchase line. */
	public Map<String, Object> toAuditMap(PurchaseItem item) {
		Map<String, Object> m = new LinkedHashMap<>();
		if (item == null) {
			return m;
		}
		m.put("purchaseItemId", item.getPurchase_item_id());
		m.put("description", item.getDescription());
		m.put("poDescription", item.getPoDescription());
		m.put("hsnCode", item.getHsnCode());
		m.put("modelNo", item.getModelNo());
		m.put("quantity", item.getQuantity());
		m.put("unitPrice", item.getUnitPrice());
		m.put("amount", item.getAmount());
		m.put("delivaryDate", item.getDelivaryDate());
		m.put("lrNum", item.getLrNum());
		return m;
	}

	public Map<Integer, Map<String, Object>> buildPurchaseItemSnapshotMap(List<PurchaseItem> items) {
		Map<Integer, Map<String, Object>> byId = new LinkedHashMap<>();
		if (items == null) {
			return byId;
		}
		for (PurchaseItem it : items) {
			if (it != null) {
				byId.put(it.getPurchase_item_id(), toAuditMap(it));
			}
		}
		return byId;
	}

	public void logPurchaseItemChange(String poNumber, Integer purchaseItemId, String action, String performedBy,
			Object oldPayload, Object newPayload, String description, HttpServletRequest request) {
		logAudit(poNumber, action, performedBy, oldPayload, newPayload, description, request, purchaseItemId);
	}

	/** Compares snapshots built by {@link #buildPurchaseItemSnapshotMap(List)} for the same PO. */
	public void diffAndLogPurchaseItemChanges(String poNumber, Map<Integer, Map<String, Object>> oldById,
			Map<Integer, Map<String, Object>> newById, String performedBy, HttpServletRequest request) {
		Map<Integer, Map<String, Object>> oldM = oldById != null ? oldById : Collections.emptyMap();
		Map<Integer, Map<String, Object>> newM = newById != null ? newById : Collections.emptyMap();

		for (Integer id : newM.keySet()) {
			// purchase_item_id is 0 for not-yet-persisted new lines
			if (id == 0 || !oldM.containsKey(id)) {
				logPurchaseItemChange(poNumber, id == 0 ? null : id, ACTION_CREATE_PURCHASE_ITEM, performedBy, null,
						newM.get(id), "Purchase line created", request);
			}
		}
		Map<String, Object> removedMarker = new HashMap<>();
		removedMarker.put("removed", true);
		for (Integer id : oldM.keySet()) {
			if (!newM.containsKey(id)) {
				logPurchaseItemChange(poNumber, id, ACTION_DELETE_PURCHASE_ITEM, performedBy, oldM.get(id),
						removedMarker, "Purchase line removed: " + id, request);
			}
		}
		for (Integer id : oldM.keySet()) {
			if (!newM.containsKey(id)) {
				continue;
			}
			Map<String, Object> o = oldM.get(id);
			Map<String, Object> n = newM.get(id);
			if (!Objects.equals(o, n)) {
				logPurchaseItemChange(poNumber, id, ACTION_UPDATE_PURCHASE_ITEM, performedBy, o, n,
						"Purchase line updated: " + id, request);
			}
		}
	}
}
