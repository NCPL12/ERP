package com.ncpl.sales.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ncpl.sales.generator.SalesIdGenerator;



@Entity
@Table(name = "tbl_sales_item")
public class SalesItem extends TimeStampEntity implements Comparable<SalesItem>{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_seq")
	@GenericGenerator(name = "sales_seq", strategy = "com.ncpl.sales.generator.SalesItemIdGenerator", parameters = {
			@Parameter(name = SalesIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d") })
	private String id;

	@NotNull(message = "{error.desc.not.empty}")
	@Column(name = "description")
	private String description;

	/*@NotNull
	@Size(min = 1, max = 150, message = "{error.hsn.not.empty}")*/
	@Column(name = "hsnCode")
	private String hsnCode;

	@Column(name = "servicehsnCode")
	private String servicehsnCode;
	
	@Column(name = "slNo")
	private String slNo;
	
	@OneToOne(fetch= FetchType.EAGER)
	@JoinColumn(name = "units_id")
	private Units item_units;
	
	private String status;
	
	private boolean archive;
	
	private float amendedQuantity;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSlNo() {
		return slNo;
	}

	public void setSlNo(String slNo) {
		this.slNo = slNo;
	}

	public String getServicehsnCode() {
		return servicehsnCode;
	}

	public void setServicehsnCode(String servicehsnCode) {
		this.servicehsnCode = servicehsnCode;
	}

	/*@NotNull*/
	@Column(name = "modelNo")
	private String modelNo;
	
	@Transient
	private String unit;

	@NotNull
	@Column(name = "servicePrice")
	private float servicePrice;
	
	public float getServicePrice() {
		return servicePrice;
	}

	public void setServicePrice(float servicePrice) {
		this.servicePrice = servicePrice;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@NotNull
	@Column(name = "quantity")
	private float quantity;

	@NotNull
	@Column(name = "unitPrice")
	private float unitPrice;

	@NotNull
	@Column(name = "amount")
	private float amount;

	@ManyToOne
	@JsonManagedReference
	private SalesOrder salesOrder;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHsnCode() {
		return hsnCode;
	}

	public void setHsnCode(String hsnCode) {
		this.hsnCode = hsnCode;
	}

	public String getModelNo() {
		return modelNo;
	}

	public void setModelNo(String modelNo) {
		this.modelNo = modelNo;
	}

	public float getQuantity() {
		return quantity;
	}

	public void setQuantity(float quantity) {
		this.quantity = quantity;
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

	public SalesOrder getSalesOrder() {
		return salesOrder;
	}

	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}

	@Override
	public int compareTo(SalesItem salesItem) {
		
		return Integer.valueOf(this.id.split("-")[2]).compareTo(Integer.valueOf(salesItem.id.split("-")[2]));
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

	public Units getItem_units() {
		return item_units;
	}

	public void setItem_units(Units item_units) {
		this.item_units = item_units;
	}

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public float getAmendedQuantity() {
		return amendedQuantity;
	}

	public void setAmendedQuantity(float amendedQuantity) {
		this.amendedQuantity = amendedQuantity;
	}

}
