package com.ncpl.sales.cashflow.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for tracking Tally sync status
 */
@Entity
@Table(name = "cashflow_tally_sync_status")
public class TallySyncStatusEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
    
    @Column(name = "last_sync_end_time")
    private LocalDateTime lastSyncEndTime;
    
    @Column(name = "invoices_synced")
    private Integer invoicesSynced;
    
    @Column(name = "last_sync_status", length = 20)
    private String lastSyncStatus; // SUCCESS, FAILED, PENDING
    
    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public TallySyncStatusEntity() {
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getLastSyncTime() {
        return lastSyncTime;
    }
    
    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
    
    public LocalDateTime getLastSyncEndTime() {
        return lastSyncEndTime;
    }
    
    public void setLastSyncEndTime(LocalDateTime lastSyncEndTime) {
        this.lastSyncEndTime = lastSyncEndTime;
    }
    
    public Integer getInvoicesSynced() {
        return invoicesSynced;
    }
    
    public void setInvoicesSynced(Integer invoicesSynced) {
        this.invoicesSynced = invoicesSynced;
    }
    
    public String getLastSyncStatus() {
        return lastSyncStatus;
    }
    
    public void setLastSyncStatus(String lastSyncStatus) {
        this.lastSyncStatus = lastSyncStatus;
    }
    
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
