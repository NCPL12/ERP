package com.ncpl.sales.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_purchase_copy")
public class PurchaseCopy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;
	
	private String poNumber;
	private String deliveryAddress;
	private String billingAddress;
	private String contactPerson;
	private String contactNumber;
	private String email;
	private double sgst;
	private double cgst;
	private double igst;
	private double total;
	private double grandTotal;
	private String deliveyTerm;
	private String warranty;
	private String modeOfPayment;
	private String taxesTerm;
	private String restrictions;
	private String taxType;
	private String created;
	private String updated;
	private String vendorAddress;
	private double gst;
	private String amountinwords;
	
	
	
	private String quoteRefNo;
	private String quoteDate;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPoNumber() {
		return poNumber;
	}
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
	public String getBillingAddress() {
		return billingAddress;
	}
	public void setBillingAddress(String billingAddress) {
		this.billingAddress = billingAddress;
	}
	public String getContactPerson() {
		return contactPerson;
	}
	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}
	public String getContactNumber() {
		return contactNumber;
	}
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public double getSgst() {
		return sgst;
	}
	public void setSgst(double sgst) {
		this.sgst = sgst;
	}
	public double getCgst() {
		return cgst;
	}
	public void setCgst(double cgst) {
		this.cgst = cgst;
	}
	public double getIgst() {
		return igst;
	}
	public void setIgst(double igst) {
		this.igst = igst;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double totalAmount) {
		this.total = totalAmount;
	}
	public double getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal(double grandTotal) {
		this.grandTotal = grandTotal;
	}
	public String getDeliveyTerm() {
		return deliveyTerm;
	}
	public void setDeliveyTerm(String deliveyTerm) {
		this.deliveyTerm = deliveyTerm;
	}
	public String getWarranty() {
		return warranty;
	}
	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}
	public String getModeOfPayment() {
		return modeOfPayment;
	}
	public void setModeOfPayment(String modeOfPayment) {
		this.modeOfPayment = modeOfPayment;
	}
	public String getTaxesTerm() {
		return taxesTerm;
	}
	public void setTaxesTerm(String taxesTerm) {
		this.taxesTerm = taxesTerm;
	}
	public String getRestrictions() {
		return restrictions;
	}
	public void setRestrictions(String restrictions) {
		this.restrictions = restrictions;
	}
	public String getTaxType() {
		return taxType;
	}
	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String poDate) {
		this.created = poDate;
	}
	public String getUpdated() {
		return updated;
	}
	public void setUpdated(String updated) {
		this.updated = updated;
	}
	public String getVendorAddress() {
		return vendorAddress;
	}
	public void setVendorAddress(String vendorAddress) {
		this.vendorAddress = vendorAddress;
	}
	public double getGst() {
		return gst;
	}
	public void setGst(double gst) {
		this.gst = gst;
	}
	public String getAmountinwords() {
		return amountinwords;
	}
	public void setAmountinwords(String amountinwords) {
		this.amountinwords = amountinwords;
	}
	
	public String getQuoteRefNo() {
		return quoteRefNo;
	}
	public void setQuoteRefNo(String quoteRefNo) {
		this.quoteRefNo = quoteRefNo;
	}
	public String getQuoteDate() {
		return quoteDate;
	}
	public void setQuoteDate(String quoteDate) {
		this.quoteDate = quoteDate;
	}
	
	
	
}
