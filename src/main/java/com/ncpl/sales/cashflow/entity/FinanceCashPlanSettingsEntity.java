package com.ncpl.sales.cashflow.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cashflow_finance_plan_settings")
public class FinanceCashPlanSettingsEntity {
    @Id
    private Long id;

    @Column(name = "opening_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "balance_as_of")
    private LocalDate balanceAsOf;

    @Column(name = "minimum_cash_reserve", nullable = false, precision = 15, scale = 2)
    private BigDecimal minimumCashReserve = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }
    public LocalDate getBalanceAsOf() { return balanceAsOf; }
    public void setBalanceAsOf(LocalDate balanceAsOf) { this.balanceAsOf = balanceAsOf; }
    public BigDecimal getMinimumCashReserve() { return minimumCashReserve; }
    public void setMinimumCashReserve(BigDecimal minimumCashReserve) { this.minimumCashReserve = minimumCashReserve; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
