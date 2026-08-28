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
@Table(name = "tbl_returnable_items")
public class ReturnableItems extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;
	private int dcItemId;
	
	@ManyToOne
	@JsonManagedReference
	@JoinColumn(name="returnable_id")
	private Returnable returnable;
	
	public Returnable getReturnable() {
		return returnable;
	}

	public void setReturnable(Returnable returnable) {
		this.returnable = returnable;
	}

	public int getDcItemId() {
		return dcItemId;
	}

	public void setDcItemId(int dcItemId) {
		this.dcItemId = dcItemId;
	}

	private float returnedQty;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getReturnedQty() {
		return returnedQty;
	}

	public void setReturnedQty(float returnedQty) {
		this.returnedQty = returnedQty;
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
