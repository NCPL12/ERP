package com.ncpl.sales.model;

import java.util.HashMap;
import java.util.Map;

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
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "sales_order_design_items")
public class DesignItems {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String ItemId;
	private float quantity;
	private float deliveredQty;
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "design_id")
	private SalesOrderDesign salesOrderDesign;
	
	public String getItemId() {
		return ItemId;
	}
	public void setItemId(String itemId) {
		ItemId = itemId;
	}
	public float getQuantity() {
		return quantity;
	}
	public void setQuantity(float quantity) {
		this.quantity = quantity;
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
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public SalesOrderDesign getSalesOrderDesign() {
		return salesOrderDesign;
	}
	public void setSalesOrderDesign(SalesOrderDesign salesOrderDesign) {
		this.salesOrderDesign = salesOrderDesign;
	}
	public Map<String, Object> getOthers() {
		return others;
	}
	public void setOthers(Map<String, Object> others) {
		this.others = others;
	}
	public float getDeliveredQty() {
		return deliveredQty;
	}
	public void setDeliveredQty(float deliveredQty) {
		this.deliveredQty = deliveredQty;
	}
	
	
	
}
