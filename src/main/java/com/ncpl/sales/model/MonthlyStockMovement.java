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
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "tbl_monthly_stock_movement", uniqueConstraints = @UniqueConstraint(
        name = "uk_monthly_stock_item_date", columnNames = { "item_master_id", "report_date" }))
public class MonthlyStockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "item_master_id", nullable = false) private String itemMasterId;
    @Column(name = "report_date", nullable = false) private LocalDate reportDate;
    @Column(name = "model_no") private String modelNo;
    @Column(name = "description", length = 1000) private String description;
    @Column(name = "closing_qty", precision = 19, scale = 6, nullable = false) private BigDecimal closingQty;
    @Column(name = "closing_rate", precision = 19, scale = 8, nullable = false) private BigDecimal closingRate;
    @Column(name = "closing_value", precision = 19, scale = 2, nullable = false) private BigDecimal closingValue;
    @Column(name = "frozen", nullable = false) private boolean frozen;
    @Column(name = "source", length = 100) private String source;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    public Long getId() { return id; }
    public String getItemMasterId() { return itemMasterId; }
    public void setItemMasterId(String value) { this.itemMasterId = value; }
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate value) { this.reportDate = value; }
    public String getModelNo() { return modelNo; }
    public void setModelNo(String value) { this.modelNo = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
    public BigDecimal getClosingQty() { return closingQty; }
    public void setClosingQty(BigDecimal value) { this.closingQty = value; }
    public BigDecimal getClosingRate() { return closingRate; }
    public void setClosingRate(BigDecimal value) { this.closingRate = value; }
    public BigDecimal getClosingValue() { return closingValue; }
    public void setClosingValue(BigDecimal value) { this.closingValue = value; }
    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean value) { this.frozen = value; }
    public String getSource() { return source; }
    public void setSource(String value) { this.source = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
}
