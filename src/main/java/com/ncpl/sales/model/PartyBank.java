package com.ncpl.sales.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "tbl_party_Bank")
public class PartyBank {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
    private Integer id;
    private String bank_name;
    private String branch_name;
    private String ifsc;
    private String account_no;

    @OneToOne
	@JsonManagedReference
    private Party party;
    
    public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
    }
    public String getBank_name() {
		return bank_name;
	}
	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
    }
    public String getBranch_name() {
		return branch_name;
	}
	public void setBranch_name(String branch_name) {
		this.branch_name = branch_name;
    }
    public String getIfsc() {
		return ifsc;
	}
	public void setIfsc(String ifsc) {
		this.ifsc = ifsc;
    }
    public String getAccount_no() {
		return account_no;
	}
	public void setAccount_no(String account_no) {
		this.account_no = account_no;
    }
    public Party getParty() {
		return party;
	}
	public void setParty(Party party) {
		this.party = party;
	
    }
}