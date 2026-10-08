package com.ncpl.sales.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ncpl.sales.generator.SupplierIdGenerator;

@Entity
@Table(name = "tbl_supplier")
public class Supplier extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supplier_seq")
	@GenericGenerator(name = "supplier_seq", strategy = "com.ncpl.sales.generator.SupplierIdGenerator", parameters = {
			@Parameter(name = SupplierIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d")})
	private String supplierId;
	@Transient
	private String supplierName;
	private double costPrice;
	private String preferred;
	
	@ManyToOne
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
	
	
	public String getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}
	public String getSupplierName() {
		return supplierName;
	}
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	public double getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(double costPrice) {
		this.costPrice = costPrice;
	}
	public String getPreferred() {
		return preferred;
	}
	public void setPreferred(String preferred) {
		this.preferred = preferred;
	}
	public Party getParty() {
		return party;
	}
	public void setParty(Party party) {
		this.party = party;
	}

}
