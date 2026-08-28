package com.ncpl.sales.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ncpl.sales.model.TimeStampEntity;

@Entity
@Table(name = "tbl_tds_lots")
public class Lot extends TimeStampEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int lotId;

	private String lotNumber;

	private Float quantity;

	private String remarks;

	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "tds_item_id")
	private TdsItems tdsItems;

	public int getLotId() {
		return lotId;
	}

	public void setLotId(int lotId) {
		this.lotId = lotId;
	}

	public String getLotNumber() {
		return lotNumber;
	}

	public void setLotNumber(String lotNumber) {
		this.lotNumber = lotNumber;
	}

	public Float getQuantity() {
		return quantity;
	}

	public void setQuantity(Float quantity) {
		this.quantity = quantity;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public TdsItems getTdsItems() {
		return tdsItems;
	}

	public void setTdsItems(TdsItems tdsItems) {
		this.tdsItems = tdsItems;
	}
}
