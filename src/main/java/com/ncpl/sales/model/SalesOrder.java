package com.ncpl.sales.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
import org.hibernate.annotations.Where;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ncpl.sales.generator.SalesIdGenerator;

@Entity
@Table(name = "tbl_sales_order")
public class SalesOrder extends TimeStampEntity implements Comparable<SalesOrder>{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_seq")
	@GenericGenerator(name = "sales_seq", strategy = "com.ncpl.sales.generator.SalesIdGenerator", parameters = {
			@Parameter(name = SalesIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%01d") })
	private String id;

	private String city;
	private double total;
	private int totalItems;
	private float gst;
	private double grandTotal;
	private String shippingAddress;
	private String billingAddress;
	private String otherTermsAndConditions;
	private String modeOfPayment;
	private String jurisdiction;
	private String freight;
	private String delivery;
	private String warranty ;
	private String clientPoNumber;
	private Date clientPoDate;
	private Date projectClosureDate;
	private String region;
	private String status;
	private Date actualClosureDate;
	private Date dlp;
	private MultipartFile[] certificate;
	private int gstRate;
	private boolean archive;
	private String responsiblePerson;
	public int getGstRate() {
		return gstRate;
	}


	public void setGstRate(int gstRate) {
		this.gstRate = gstRate;
	}


	public Date getProjectClosureDate() {
		return projectClosureDate;
	}


	public void setProjectClosureDate(Date projectClosureDate) {
		this.projectClosureDate = projectClosureDate;
	}


	public String getClientPoNumber() {
		return clientPoNumber;
	}


	public void setClientPoNumber(String clientPoNumber) {
		this.clientPoNumber = clientPoNumber;
	}


	public Date getClientPoDate() {
		return clientPoDate;
	}


	public void setClientPoDate(Date clientPoDate) {
		this.clientPoDate = clientPoDate;
	}


	public String getModeOfPayment() {
		return modeOfPayment;
	}


	public void setModeOfPayment(String modeOfPayment) {
		this.modeOfPayment = modeOfPayment;
	}


	public String getJurisdiction() {
		return jurisdiction;
	}


	public void setJurisdiction(String jurisdiction) {
		this.jurisdiction = jurisdiction;
	}


	public String getFreight() {
		return freight;
	}


	public void setFreight(String freight) {
		this.freight = freight;
	}


	public String getDelivery() {
		return delivery;
	}


	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}


	public String getWarranty() {
		return warranty;
	}


	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}

	

	public String getShippingAddress() {
		return shippingAddress;
	}


	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}


	public String getBillingAddress() {
		return billingAddress;
	}


	public void setBillingAddress(String billingAddress) {
		this.billingAddress = billingAddress;
	}


	public String getOtherTermsAndConditions() {
		return otherTermsAndConditions;
	}


	public void setOtherTermsAndConditions(String otherTermsAndConditions) {
		this.otherTermsAndConditions = otherTermsAndConditions;
	}


	public SalesOrder(String id, String city, double total, double grandTotal, List<SalesItem> items, int totalItems,float gst) {
		super();
		this.id = id;
		this.city = city;
		this.total = total;
		this.grandTotal = grandTotal;
		this.items = items;
		this.totalItems = totalItems;
		this.gst = gst;
	}

	
	public SalesOrder() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * One to many association with contacts entity
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "sales_order_id")
	@JsonBackReference
	@Where(clause = "archive = 0")
	private List<SalesItem> items;
	
	@JsonManagedReference
	@ManyToOne
	private Party party;

	public Party getParty() {
		return party;
	}


	public void setParty(Party party) {
		this.party = party;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public float getGst() {
		return gst;
	}

	public void setGst(float gst) {
		this.gst = gst;
	}

	public double getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(double grandTotal) {
		this.grandTotal = grandTotal;
	}

	public List<SalesItem> getItems() {
		return items;
	}

	public void setItems(List<SalesItem> items) {
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


	public String getRegion() {
		return region;
	}


	public void setRegion(String region) {
		this.region = region;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public Date getActualClosureDate() {
		return actualClosureDate;
	}


	public void setActualClosureDate(Date actualClosureDate) {
		this.actualClosureDate = actualClosureDate;
	}


	public Date getDlp() {
		return dlp;
	}


	public void setDlp(Date dlp) {
		this.dlp = dlp;
	}


	public MultipartFile[] getCertificate() {
		return certificate;
	}


	public void setCertificate(MultipartFile[] certificate) {
		this.certificate = certificate;
	}


	public boolean isArchive() {
		return archive;
	}


	public void setArchive(boolean archive) {
		this.archive = archive;
	}


	@Override
	public int compareTo(SalesOrder so) {
		if (this.getCreated() == null || so.getCreated() == null)
		      return 0;
		return this.getCreated().compareTo(so.getCreated());
	}


	public String getResponsiblePerson() {
		return responsiblePerson;
	}


	public void setResponsiblePerson(String responsiblePerson) {
		this.responsiblePerson = responsiblePerson;
	}
	
}