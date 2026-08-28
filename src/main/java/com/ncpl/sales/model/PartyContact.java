/*
 * @Author Anurag Dhiman
 * @Copyright Teknika Infotech Pvt. Ltd.
 * @Date 23-12-2019
 * 
 * 
 * 
 * This program is written to create a party contacts. A party can have multiple contacts like sales manager etc.. 
 */
//The model package of sales module
package com.ncpl.sales.model;

import javax.persistence.Column;
/**
 * JPA dependencies imported
 */
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "tbl_party_contacts")
public class PartyContact {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private int id;
	private String name;
	
	private String designation;
	private String mobile_no;
	private String whats_app;
	private String phone_no;
	private String email_id;
	private String skype_id;
	
	
	//Association with party entity
	@ManyToOne
	@JsonManagedReference
	private Party party;

	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name ="designation_id")
	private Designation party_contact_designation;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public String getMobile_no() {
		return mobile_no;
	}
	public void setMobile_no(String mobile_no) {
		this.mobile_no = mobile_no;
	}
	public String getwhats_app() {
		return whats_app;
	}
	public void setwhats_app(String whats_app) {
		this.whats_app = whats_app;
	}
	public String getPhone_no() {
		return phone_no;
	}
	public void setPhone_no(String phone_no) {
		this.phone_no = phone_no;
	}
	public String getEmail_id() {
		return email_id;
	}
	public void setEmail_id(String email_id) {
		this.email_id = email_id;
	}
	public String getSkype_id() {
		return skype_id;
	}
	public void setSkype_id(String skype_id) {
		this.skype_id = skype_id;
	}
	public Party getParty() {
		return party;
	}
	public void setParty(Party party) {
		this.party = party;
	}
	public Designation getParty_contact_designation() {
		return party_contact_designation;
	}
	public void setParty_contact_designation(Designation party_contact_designation) {
		this.party_contact_designation = party_contact_designation;
	}
	/*
	 * @Override public String toString() { return "PartyContact [id=" + id +
	 * ", name=" + name + ", designation=" + designation + ", mobile_no=" +
	 * mobile_no + ", phone_no=" + phone_no + ", email_id=" + email_id +
	 * ", skype_id=" + skype_id + ", party=" + party + "]"; }
	 */
	
}
