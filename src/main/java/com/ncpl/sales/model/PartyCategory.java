package com.ncpl.sales.model;
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
import com.fasterxml.jackson.annotation.JsonManagedReference;
@Entity
@Table(name = "tbl_party_category")
public class PartyCategory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;
	
	@ManyToOne
	@JsonManagedReference
	private Party party;
	
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name ="category_id")
	private Category category_id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Party getParty() {
		return party;
	}
	public void setParty(Party party) {
		this.party = party;
	}
	public Category getCategory_id() {
		return category_id;
	}
	public void setCategory_id(Category category_id) {
		this.category_id = category_id;
	}
	
	
}