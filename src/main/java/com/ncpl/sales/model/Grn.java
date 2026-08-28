package com.ncpl.sales.model;

import java.util.Date;
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
import com.ncpl.sales.generator.GrnIdGenerator;
@Entity
@Table(name = "tbl_grn")
public class Grn extends TimeStampEntity{
	/**@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int grnId;**/
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grn_seq")
	@GenericGenerator(name = "grn_seq", strategy = "com.ncpl.sales.generator.GrnIdGenerator", parameters = {
	@Parameter(name = GrnIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d")})
	//private String grnId;
	private String grnId;
	private String poNumber;
	private int quantityReceived;
	private Date invoiceDate;
	private String invoiceNo;
	private boolean archive;
	
	public Date getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Date invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "grn_id")
	@JsonBackReference
	private List<GrnItems> items;
	
	public List<GrnItems> getItems() {
		return items;
	}
	public void setItems(List<GrnItems> items) {
		this.items = items;
	}
	public String getGrnId() {
		return grnId;
	}
	public void setGrnId(String grnId) {
		this.grnId = grnId;
	}
	public String getPoNumber() {
		return poNumber;
	}
	public void setPoNumber(String poNumber) {
		this.poNumber = poNumber;
	}
	
	public int getQuantityReceived() {
		return quantityReceived;
	}
	public void setQuantityReceived(int quantityReceived) {
		this.quantityReceived = quantityReceived;
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
}
