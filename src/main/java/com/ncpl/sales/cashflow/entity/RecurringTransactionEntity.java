package com.ncpl.sales.cashflow.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cashflow_recurring_transactions")
public class RecurringTransactionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // e.g., "Electricity Bill", "Salary", "Water Bill"
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private String type; // MONTHLY, QUARTERLY, YEARLY, WEEKLY
    
    @Column(nullable = false)
    private Integer dayOfMonth; // Day of month when it recurs (1-31)
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String category; // UTILITY, SALARY, RENT, SUBSCRIPTION, OTHER
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDate nextDueDate;
    
    @Column
    private String notes;
    
    @Column(nullable = false, updatable = false)
    private LocalDate createdDate = LocalDate.now();
    
    @Column
    private LocalDate lastUpdatedDate;

    // Constructors
    public RecurringTransactionEntity() {}

    public RecurringTransactionEntity(String name, String description, String type, 
                                    Integer dayOfMonth, BigDecimal amount, String category,
                                    LocalDate nextDueDate) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.dayOfMonth = dayOfMonth;
        this.amount = amount;
        this.category = category;
        this.nextDueDate = nextDueDate;
        this.isActive = true;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(Integer dayOfMonth) { this.dayOfMonth = dayOfMonth; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(LocalDate lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
}
