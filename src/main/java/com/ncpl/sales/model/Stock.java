package com.ncpl.sales.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ncpl.sales.generator.StockIdGenerator;

@Entity
@Table(name = "tbl_stock")
@Audited(targetAuditMode=RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
public class Stock extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_seq")
	@GenericGenerator(name = "stock_seq", strategy = "com.ncpl.sales.generator.StockIdGenerator", parameters = {
			@Parameter(name = StockIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d")})
	private String stockId;
	@Transient
	private String clientName;
	private Float quantity;
	private String storeName;
	private String locationInStore;
	private String reason;
	private String activity;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JsonManagedReference
	private Party party;
	
	@ManyToOne
	@JsonManagedReference
	private ItemMaster itemMaster;

	public ItemMaster getItemMaster() {
		return itemMaster;
	}
	public void setItemMaster(ItemMaster itemMaster) {
		this.itemMaster = itemMaster;
	}
	public Party getParty() {
		return party;
	}
	public void setParty(Party party) {
		this.party = party;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public String getLocationInStore() {
		return locationInStore;
	}
	public void setLocationInStore(String locationInStore) {
		this.locationInStore = locationInStore;
	}
	public String getStockId() {
		return stockId;
	}
	public void setStockId(String stockId) {
		this.stockId = stockId;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public float getQuantity() {
		return quantity;
	}
	public void setQuantity(float quantity) {
		this.quantity = quantity;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}

}
