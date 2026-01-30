package com.ncpl.sales.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;

@MappedSuperclass
@Audited(targetAuditMode=RelationTargetAuditMode.NOT_AUDITED)
public abstract class TimeStampEntity {
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "dd-MM-yyyy hh:mm")
	@Column(name = "created" , nullable = false)
	private Date created;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "dd-MM-yyyy hh:mm")
	@Column(name = "updated" , nullable = false)
	private Date updated;
	
	@CreatedBy
    @Column(name = "created_by")
	private String createdBy;
	
	@LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
	
	
	@PrePersist
	protected void onCreate() {
		updated = created = new Date();
		createdBy = getUsernameOfLoggedInUser();
	}

	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
		lastModifiedBy = getUsernameOfLoggedInUser();
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	
	private String getUsernameOfLoggedInUser() {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userName;
    }
	
	
}
