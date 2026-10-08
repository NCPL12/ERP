package com.ncpl.sales.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="category")
public class Category {
    
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;
	private String name;
	private String code;
	
	
	
	 public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	//  @OneToOne(fetch=FetchType.EAGER) 
	//  @JsonManagedReference
	//  private Party party;
	
	//  public Party getParty() { return party; }
	  
	//  public void setParty(Party party) { this.party = party; }
	 
	 
	
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

	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + ", code=" + code + "]";
	}

	/*
	 * @Override public String toString() { return "Category [id=" + id + ", name="
	 * + name + ", code=" + code + ", party=" + party + "]"; }
	 */

	
	
	
  
	
}
