package com.ncpl.sales.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "tbl_dc")
public class DeliveryChallan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int dcId;
	private String soNumber;
	private boolean archive;
	
	@Column(name = "dc_comment")
	private String dcComment;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "dc_id")
	@JsonBackReference
	private List<DeliveryChallanItems> items;
	
	public List<DeliveryChallanItems> getItems() {
		return items;
	}
	public void setItems(List<DeliveryChallanItems> items) {
		this.items = items;
	}
	public int getDcId() {
		return dcId;
	}
	public void setDcId(int dcId) {
		this.dcId = dcId;
	}
	public String getSoNumber() {
		return soNumber;
	}
	public void setSoNumber(String soNumber) {
		this.soNumber = soNumber;
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
	public boolean isArchive() {
		return archive;
	}
	public void setArchive(boolean archive) {
		this.archive = archive;
	}
	
	public String getDcComment() {
		return dcComment;
	}
	
	public void setDcComment(String dcComment) {
		this.dcComment = dcComment;
	}
	
}
