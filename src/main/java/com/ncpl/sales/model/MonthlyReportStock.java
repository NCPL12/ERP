package com.ncpl.sales.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_monthly_report_stock")
public class MonthlyReportStock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "item_master_id")
	private String itemMasterId;

	@Column(name = "report_date")
	private LocalDate reportDate;

	@Column(name = "outstanding_value")
	private BigDecimal outstandingValue;

	@Column(name = "outstanding_qty")
	private BigDecimal outstandingQty;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getItemMasterId() {
		return itemMasterId;
	}

	public void setItemMasterId(String itemMasterId) {
		this.itemMasterId = itemMasterId;
	}

	public LocalDate getReportDate() {
		return reportDate;
	}

	public void setReportDate(LocalDate reportDate) {
		this.reportDate = reportDate;
	}

	public BigDecimal getOutstandingValue() {
		return outstandingValue;
	}

	public void setOutstandingValue(BigDecimal outstandingValue) {
		this.outstandingValue = outstandingValue;
	}

	public BigDecimal getOutstandingQty() {
		return outstandingQty;
	}

	public void setOutstandingQty(BigDecimal outstandingQty) {
		this.outstandingQty = outstandingQty;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
