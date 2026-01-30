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
@Table(name = "tbl_tds_items")
public class TdsItems extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int tdsItemId;
	
	private String description;
	private boolean tdsApproved;
	private float siteQuantity;
	private String modelNumber;
	private float designQty;
	
	@ManyToOne
	@JsonManagedReference
	@JoinColumn(name="tds_id")
	private Tds tds;

	public int getTdsItemId() {
		return tdsItemId;
	}

	public void setTdsItemId(int tdsItemId) {
		this.tdsItemId = tdsItemId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isTdsApproved() {
		return tdsApproved;
	}

	public void setTdsApproved(boolean tdsApproved) {
		this.tdsApproved = tdsApproved;
	}

	public float getSiteQuantity() {
		return siteQuantity;
	}

	public void setSiteQuantity(float siteQuantity) {
		this.siteQuantity = siteQuantity;
	}

	public String getModelNumber() {
		return modelNumber;
	}

	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}

	public float getDesignQty() {
		return designQty;
	}

	public void setDesignQty(float designQty) {
		this.designQty = designQty;
	}

	public Tds getTds() {
		return tds;
	}

	public void setTds(Tds tds) {
		this.tds = tds;
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

	public Map<String, Object> getOthers() {
		return others;
	}
	public void setOthers(Map<String, Object> others) {
		this.others = others;
	}
}
