package com.ncpl.sales.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "sales_order_design")
public class SalesOrderDesign extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "design_id", unique = true, nullable = false)
	private long id;
	
	private String salesItemId;
	
	@OneToMany(mappedBy="salesOrderDesign", cascade = CascadeType.ALL)
	private List<DesignItems> items = new ArrayList<DesignItems>();
	
	public List<DesignItems> getItems() {
		return items;
	}
	public void setItems(List<DesignItems> items) {
		this.items = items;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSalesItemId() {
		return salesItemId;
	}
	public void setSalesItemId(String salesItemId) {
		this.salesItemId = salesItemId;
	}
	
}

