/*
 * @Author Anurag Dhiman
 * @Copyright Teknika Infotech Pvt. Ltd.
 * @Date 23-12-2019
 * 
 * 
 * 
 * This program is written to create a party. A party can be Supplier, Client etc. 
 */

//The model package of sales module
package com.ncpl.sales.model;

import java.util.List;

/**
 * JPA dependencies imported
 */
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * The imports are used to generate the custom id, that will as primary key in table
 * A separate program is written to generate custom primary key
 */
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonBackReference;
/**
 * This package contains the program to generate the custom primary keys for an entity class
 */
import com.ncpl.sales.generator.PartyIdGenerator;
@Entity
@Table(name = "tbl_party")
@Audited(targetAuditMode=RelationTargetAuditMode.NOT_AUDITED)
public class Party implements Comparable<Party>{
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "party_seq")
	@GenericGenerator(name = "party_seq" , strategy = "com.ncpl.sales.generator.PartyIdGenerator", 
	parameters = {
		   @Parameter(name = PartyIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d")
			}) 
	private String id;
	
	private String party_intial;
	private String addr1;
	private String addr2;
	private String website;
	
	private String phone1;
	private String phone2;
	private String fax;
	private String email1;
	private String email2;
	private String abbrivation ;
	private String pan;
	private String cin;
	private String gst;
	private String pin;

	private String remarks;
	private String interState;
	public String getInterState() {
		return interState;
	}
	public void setInterState(String interState) {
		this.interState = interState;
	}

	

	/*
	 * @Transient private String city;
	 */
	@Transient
	private  int city;
	private String partyName;
	private String type;
	@Transient
	private int category;
	
	
	
	
	/**
	 * One to many association with contacts entity
	 */
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "party_id")
	@JsonBackReference
	private List<PartyContact> contacts;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "party_id")
	@JsonBackReference
	private List<SalesOrder> salesOrder;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "party_id")
	@JsonBackReference
	private List<PurchaseOrder> purchaseOrder;
	
	@OneToOne(fetch= FetchType.EAGER)
	@JoinColumn(name = "city_id")
	 private City party_city;
	 
	@OneToOne(fetch= FetchType.EAGER)
	@JoinColumn(name = "type_id")
	 private Type party_type;

	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name ="category_id")
	//@JsonBackReference 
	private Category party_category;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "party_id")
	@JsonBackReference
	private List<PartyCategory> categories;
	
	public  List<PartyCategory>  getcategories() {
		return categories;
	}
	public void setgetcategories(List<PartyCategory> categories) {
		this.categories = categories;
	}

	public Category getParty_category() {
		return party_category;
	}
	public void setParty_category(Category party_category) {
		this.party_category = party_category;
	}
	public int getCity() {
		return city;
	}
	public void setCity(int city) {
		this.city = city;
	}
	public City getParty_city() {
		return party_city;
	}
	public void setParty_city(City party_city) {
		this.party_city = party_city;
	}
	public Type getParty_type() {
		return party_type;
	}
	public void setParty_type(Type party_type) {
		this.party_type = party_type;
	}
	
	
	/*
	 * @Override public String toString() { return "Party [id=" + id +
	 * ", party_intial=" + party_intial + ", addr1=" + addr1 + ", addr2=" + addr2 +
	 * ", website=" + website + ", phone1=" + phone1 + ", phone2=" + phone2 +
	 * ", email1=" + email1 + ", email2=" + email2 + ", type=" + type +
	 * ", abbrivation=" + abbrivation + ", pan=" + pan + ", cin=" + cin + ",city="+
	 * city + ", gst=" + gst + "]";
	 */
	
	@Override
	public String toString() {
		return "Party [id=" + id + ", party_intial=" + party_intial + ", addr1=" + addr1 + ", addr2=" + addr2
				+ ", website=" + website + ", phone1=" + phone1 + ", phone2=" + phone2 + ", email1=" + email1
				+ ", email2=" + email2 + ", type=" + type + ", abbrivation=" + abbrivation + ", pan=" + pan + ", cin="
				+ cin + ", gst=" + gst + ", city=" + city + ", partyName=" + partyName + ", category=" + category
				+ ", contacts=" + contacts + ", salesOrder=" + salesOrder + ", party_city=" + party_city
				+ ", party_category=" + party_category + "]";
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getParty_intial() {
		return party_intial;
	}
	public void setParty_intial(String party_intial) {
		this.party_intial = party_intial;
	}
	public String getAddr1() {
		return addr1;
	}
	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}
	public String getAddr2() {
		return addr2;
	}
	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getPhone1() {
		return phone1;
	}
	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}
	public String getPhone2() {
		return phone2;
	}
	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getEmail1() {
		return email1;
	}
	public void setEmail1(String email1) {
		this.email1 = email1;
	}
	public String getEmail2() {
		return email2;
	}
	public void setEmail2(String email2) {
		this.email2 = email2;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getAbbrivation() {
		return abbrivation;
	}
	public void setAbbrivation(String abbrivation) {
		this.abbrivation = abbrivation;
	}
	public String getPan() {
		return pan;
	}
	public void setPan(String pan) {
		this.pan = pan;
	}
	public String getCin() {
		return cin;
	}
	public void setCin(String cin) {
		this.cin = cin;
	}
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public List<PartyContact> getContacts() {
		return contacts;
	}
	public List<SalesOrder> getSalesOrder() {
		return salesOrder;
	}
	public void setSalesOrder(List<SalesOrder> salesOrder) {
		this.salesOrder = salesOrder;
	}
	
	public List<PurchaseOrder> getPurchaseOrder() {
		return purchaseOrder;
	}
	public void setPurchaseOrder(List<PurchaseOrder> purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}
	public void setContacts(List<PartyContact> contacts) {
		this.contacts = contacts;
	}

	public String getPartyName() {
		return partyName;
	}
	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}

	@Override
	public int compareTo(Party party) {
		return this.partyName.compareToIgnoreCase(party.partyName);
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}

	/* Satish Patil changes adding party contacts attributes */
	
/*	private String contactName;
	private String contactDesignation;
	private String contactMobile;
	private String contactPhone;
	private String contactEmail;
	private String contactSkype;



	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactDesignation() {
		return contactDesignation;
	}
	public void setContactDesignation(String contactDesignation) {
		this.contactDesignation = contactDesignation;
	}
	public String getContactMobile() {
		return contactMobile;
	}
	public void setContactMobile(String contactMobile) {
		this.contactMobile = contactMobile;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getContactEmail() {
		return contactEmail;
	}
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	public String getContactSkype() {
		return contactSkype;
	}
	public void setContactSkype(String contactSkype) {
		this.contactSkype = contactSkype;
	}
	
	*/
	
	
	
	
	
}
