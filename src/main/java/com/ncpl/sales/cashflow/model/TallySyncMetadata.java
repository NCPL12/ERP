package com.ncpl.sales.cashflow.model;

import java.time.LocalDateTime;

/**
 * Tracks Tally synchronization state for incremental imports.
 * Stores last sync timestamp to identify and import only new/modified invoices.
 */
public class TallySyncMetadata {
    private Long id;
    private LocalDateTime lastSyncTime;
    private LocalDateTime lastSyncEndTime;
    private int invoicesSynced;
    private String lastSyncStatus;  // SUCCESS, FAILED, IN_PROGRESS
    private String lastErrorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TallySyncMetadata() {
    }

    public TallySyncMetadata(LocalDateTime lastSyncTime, LocalDateTime lastSyncEndTime,
                            int invoicesSynced, String lastSyncStatus) {
        this.lastSyncTime = lastSyncTime;
        this.lastSyncEndTime = lastSyncEndTime;
        this.invoicesSynced = invoicesSynced;
        this.lastSyncStatus = lastSyncStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public int getInvoicesSynced() {
        return invoicesSynced;
    }

    public void setInvoicesSynced(int invoicesSynced) {
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
