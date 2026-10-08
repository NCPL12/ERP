package com.ncpl.sales.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ncpl.sales.generator.WorkOrderIdGenerator;

@Entity
@Table(name = "tbl_work_order")
public class WorkOrder extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "work_order_seq")
	@GenericGenerator(name = "work_order_seq", strategy = "com.ncpl.sales.generator.WorkOrderIdGenerator", parameters = {
			@Parameter(name = WorkOrderIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d") })
	private String id;
	
	@ManyToOne
	@JsonManagedReference
	private Party party;
	
	@ManyToOne
	@JsonManagedReference
	private SalesOrder salesOrder;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "wo_id")
	@JsonBackReference
	private List<WorkOrderItems> items;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	public SalesOrder getSalesOrder() {
		return salesOrder;
	}

	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}

	public List<WorkOrderItems> getItems() {
		return items;
	}

	public void setItems(List<WorkOrderItems> items) {
		this.items = items;
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
