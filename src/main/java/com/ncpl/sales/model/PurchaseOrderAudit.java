package com.ncpl.sales.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;

// @D0017 Purchase Order audit log, mirrors SalesOrderAudit / tbl_sales_order_audit (see README.md)
@Entity
@Table(name = "tbl_purchase_order_audit")
public class PurchaseOrderAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "po_number", nullable = false)
	private String poNumber;

	@Column(name = "action", nullable = false)
	private String action;

	@Column(name = "performed_by", nullable = false)
	private String performedBy;

	@Column(name = "action_performed", nullable = false)
	private Timestamp actionPerformed;

	@Column(name = "old_values", columnDefinition = "TEXT")
	private String oldValues;

	@Column(name = "new_values", columnDefinition = "TEXT")
	private String newValues;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "session_id")
	private String sessionId;

	/** Set for line-level actions (CREATE/UPDATE/DELETE_PURCHASE_ITEM); null for header-only audit rows. */
	@Column(name = "purchase_item_id")
	private Integer purchaseItemId;

	public PurchaseOrderAudit() {
	}

	public PurchaseOrderAudit(String poNumber, String action, String performedBy, Timestamp actionPerformed,
			String oldValues, String newValues, String description) {
		this.poNumber = poNumber;
		this.action = action;
		this.performedBy = performedBy;
		this.actionPerformed = actionPerformed;
		this.oldValues = oldValues;
		this.newValues = newValues;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPoNumber() {
		return poNumber;
	}

	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getPerformedBy() {
		return performedBy;
	}

	public void setPerformedBy(String performedBy) {
		this.performedBy = performedBy;
	}

	public Timestamp getActionPerformed() {
		return actionPerformed;
	}

	public void setActionPerformed(Timestamp actionPerformed) {
		this.actionPerformed = actionPerformed;
	}

	public String getOldValues() {
		return oldValues;
	}

	public void setOldValues(String oldValues) {
		this.oldValues = oldValues;
	}

	public String getNewValues() {
		return newValues;
	}

	public void setNewValues(String newValues) {
		this.newValues = newValues;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getPurchaseItemId() {
		return purchaseItemId;
	}

	public void setPurchaseItemId(Integer purchaseItemId) {
		this.purchaseItemId = purchaseItemId;
	}

	@Override
	public String toString() {
		return "PurchaseOrderAudit{" +
				"id=" + id +
				", poNumber='" + poNumber + '\'' +
				", action='" + action + '\'' +
				", performedBy='" + performedBy + '\'' +
				", actionPerformed=" + actionPerformed +
				", description='" + description + '\'' +
				'}';
	}
}
