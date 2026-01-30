package com.ncpl.sales.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ncpl.sales.generator.PartyIdGenerator;

@Entity
@Table(name = "tbl_party_Address")
public class PartyAddress extends TimeStampEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "party_addr_seq")
	@GenericGenerator(name = "party_addr_seq" , strategy = "com.ncpl.sales.generator.PartyAddressIdGenerator", 
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
	private String email1;
	private String email2;
	private String contact;
	private String faxnumber;
	private String gst;
	private String pin;
	
	/*
	 * @Transient private String city;
	 */
	@Transient
	private  int city;
	private String partyName;

	@OneToOne(fetch= FetchType.EAGER)
	@JoinColumn(name = "city_id")
	 private City partyaddr_city;
	
	
	@OneToOne(fetch = FetchType.EAGER)
	@JsonManagedReference
	private Party party;
	
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
	
	
	public int getCity() {
		return city;
	}
	public void setCity(int city) {
		this.city = city;
	}
	public String getPartyName() {
		return partyName;
	}
	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}

	public Party getParty() {
		return party;
	}
	public void setParty(Party party) {
		this.party = party;
	
}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	public City getPartyaddr_city() {
		return partyaddr_city;
	}
	public void setPartyaddr_city(City partyaddr_city) {
		this.partyaddr_city = partyaddr_city;
	}
	public String getFaxnumber() {
		return faxnumber;
	}
	public void setFaxnumber(String faxnumber) {
		this.faxnumber = faxnumber;
	}
	
	public String getGst() {
		return gst;
	}
	public void setGst(String gst) {
		this.gst = gst;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
}