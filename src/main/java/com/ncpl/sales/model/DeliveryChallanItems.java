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
@Table(name = "tbl_dc_items")
public class DeliveryChallanItems extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int dcItemId;
	
	private String description;
	private String soModelNo;
	private String slNo;
    
    private float totalQuantity;
    private float deliveredQuantity;
    private float todaysQty;
	
	@ManyToOne
	@JsonManagedReference
	@JoinColumn(name="dc_id")
	private DeliveryChallan deliveryChallan;
	
	public String getSoModelNo() {
		return soModelNo;
	}
	public void setSoModelNo(String soModelNo) {
		this.soModelNo = soModelNo;
	}
	
	public int getDcItemId() {
		return dcItemId;
	}
	public void setDcItemId(int dcItemId) {
		this.dcItemId = dcItemId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public float getTotalQuantity() {
		return totalQuantity;
	}
	public void setTotalQuantity(float totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	public float getDeliveredQuantity() {
		return deliveredQuantity;
	}
	public void setDeliveredQuantity(float deliveredQuantity) {
		this.deliveredQuantity = deliveredQuantity;
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
	public float getTodaysQty() {
		return todaysQty;
	}
	public void setTodaysQty(float todaysQty) {
		this.todaysQty = todaysQty;
	}

	public String getSlNo() {
		return slNo;
	}
	public void setSlNo(String slNo) {
		this.slNo = slNo;
	}
	public DeliveryChallan getDeliveryChallan() {
		return deliveryChallan;
	}
	public void setDeliveryChallan(DeliveryChallan deliveryChallan) {
		this.deliveryChallan = deliveryChallan;
	}
}
