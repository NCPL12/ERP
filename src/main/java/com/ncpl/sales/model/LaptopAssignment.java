package com.ncpl.sales.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "tbl_laptop_assignment")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED, withModifiedFlag = true)
public class LaptopAssignment extends TimeStampEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(unique = true, nullable = false)
	private int id;

	@ManyToOne
	@JoinColumn(name = "company_asset_id")
	@JsonBackReference
	private CompanyAssets companyAsset;

	@ManyToOne
	@JoinColumn(name = "employee_id")
	private EmployeeMaster employee;

	/** Form-bound employee id (assignments[N].employeeId) — resolved to {@link #employee} in the service before save. */
	@Transient
	private String employeeId;

	private String location;
	private String issuedBy;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dateIssued;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dateReturned;

	private String returnedTo;

	private String remarks;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public CompanyAssets getCompanyAsset() {
		return companyAsset;
	}
	public void setCompanyAsset(CompanyAssets companyAsset) {
		this.companyAsset = companyAsset;
	}
	public EmployeeMaster getEmployee() {
		return employee;
	}
	public void setEmployee(EmployeeMaster employee) {
		this.employee = employee;
	}
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getIssuedBy() {
		return issuedBy;
	}
	public void setIssuedBy(String issuedBy) {
		this.issuedBy = issuedBy;
	}
	public Date getDateIssued() {
		return dateIssued;
	}
	public void setDateIssued(Date dateIssued) {
		this.dateIssued = dateIssued;
	}
	public Date getDateReturned() {
		return dateReturned;
	}
	public void setDateReturned(Date dateReturned) {
		this.dateReturned = dateReturned;
	}
	public String getReturnedTo() {
		return returnedTo;
	}
	public void setReturnedTo(String returnedTo) {
		this.returnedTo = returnedTo;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
