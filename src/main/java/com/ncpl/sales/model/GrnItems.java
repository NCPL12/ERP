package com.ncpl.sales.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "tbl_grn_items")
public class GrnItems extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int grnItemId;
	@Column(name="po_item_id")
	private String description;
	
	private float receivedQuantity;
	private float unitPrice;
	private float amount;
	
	@ManyToOne
	@JsonManagedReference
	@JoinColumn(name = "grn_id")
	private Grn grn;
	
	public Grn getGrn() {
		return grn;
	}
	public void setGrn(Grn grn) {
		this.grn = grn;
	}
	public int getGrnItemId() {
		return grnItemId;
	}
	public void setGrnItemId(int grnItemId) {
		this.grnItemId = grnItemId;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public float getReceivedQuantity() {
		return receivedQuantity;
	}
	public void setReceivedQuantity(float receivedQuantity) {
		this.receivedQuantity = receivedQuantity;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	/* for jackson json */
	@Transient
	private Map<String, Object> others = new HashMap<String, Object>();

	@JsonAnyGetter
	public Map<String, Object> get() {
		return others;
	}
	
	public void set(Map<String, Object> data){
		others.putAll(data);
	}

	@JsonAnySetter
	public void set(String property, Object value) {
		others.put(property, value);
	}

	public Object get(String key) {
		Map<String, Object> others = this.get();
		return others.get(key);
	}
	
}
