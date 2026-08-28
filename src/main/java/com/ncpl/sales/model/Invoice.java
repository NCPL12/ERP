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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ncpl.sales.generator.InvoiceIdGenerator;


@Entity
@Table(name="tbl_invoice")
public class Invoice extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_seq")
	@GenericGenerator(name = "invoice_seq", strategy = "com.ncpl.sales.generator.InvoiceIdGenerator", parameters = {
			@Parameter(name = InvoiceIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d")})
	private String invoiceId;
	
	private String soNumber;
	private String dcNumber;
	private String type;
	private double total;
	private float gst;
	
	private double grandTotal;
	private float gstRate;
	private String paymentStatus;
	private String paymentMode;
	private String paymentRemarks;
	private String transactionNumber;
	
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	public String getPaymentRemarks() {
		return paymentRemarks;
	}
	public void setPaymentRemarks(String paymentRemarks) {
		this.paymentRemarks = paymentRemarks;
	}
	public String getTransactionNumber() {
		return transactionNumber;
	}
	public void setTransactionNumber(String transactionNumber) {
		this.transactionNumber = transactionNumber;
	}
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "inoiceId")
	@JsonBackReference
	private List<InvoiceItem> items;
	
	
	public String getSoNumber() {
		return soNumber;
	}
	public void setSoNumber(String soNumber) {
		this.soNumber = soNumber;
	}
	public String getDcNumber() {
		return dcNumber;
	}
	public void setDcNumber(String dcNumber) {
		this.dcNumber = dcNumber;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal(double grandTotal) {
		this.grandTotal = grandTotal;
	}
	public List<InvoiceItem> getItems() {
		return items;
	}
	public void setItems(List<InvoiceItem> items) {
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
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public float getGst() {
		return gst;
	}
	public void setGst(float gst) {
		this.gst = gst;
	}
	
	public float getGstRate() {
		return gstRate;
	}
	public void setGstRate(float gstRate) {
		this.gstRate = gstRate;
	}
}
