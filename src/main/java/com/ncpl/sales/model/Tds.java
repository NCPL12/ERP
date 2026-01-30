package com.ncpl.sales.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "tbl_tds")
public class Tds extends TimeStampEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int tdsId;
	private String soNumber;
	
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "tds_id")
	@JsonBackReference
	private List<TdsItems> items;
	
	public String getSoNumber() {
		return soNumber;
	}

	public void setSoNumber(String soNumber) {
		this.soNumber = soNumber;
	}

	public int getTdsId() {
		return tdsId;
	}

	public void setTdsId(int tdsId) {
		this.tdsId = tdsId;
	}

	public List<TdsItems> getItems() {
		return items;
	}

	public void setItems(List<TdsItems> items) {
		this.items = items;
	}
	
	
	
}
