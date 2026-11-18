package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.entity.RecurringTransactionEntity;
import com.ncpl.sales.cashflow.repository.RecurringTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class RecurringTransactionManagementService {
    
    private final RecurringTransactionRepository repository;
    
    public RecurringTransactionManagementService(RecurringTransactionRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Create a new recurring transaction
     */
    public RecurringTransactionEntity createRecurringTransaction(RecurringTransactionEntity transaction) {
        if (transaction.getCreatedDate() == null) {
            transaction.setCreatedDate(LocalDate.now());
        }
        transaction.setLastUpdatedDate(LocalDate.now());
        transaction.setIsActive(true);
        return repository.save(transaction);
    }
    
    /**
     * Get all active recurring transactions
     */
    public List<RecurringTransactionEntity> getAllActiveTransactions() {
        return repository.findByIsActiveOrderByNextDueDateAsc(true);
    }
    
    /**
     * Get all transactions (including inactive)
     */
    public List<RecurringTransactionEntity> getAllTransactions() {
        return repository.findAll();
    }
    
    /**
     * Get transaction by ID
     */
    public Optional<RecurringTransactionEntity> getTransactionById(Long id) {
        return repository.findById(id);
    }
    
    /**
     * Update an existing recurring transaction
     */
    public RecurringTransactionEntity updateRecurringTransaction(Long id, RecurringTransactionEntity updates) {
        Optional<RecurringTransactionEntity> existing = repository.findById(id);
        if (existing.isPresent()) {
            RecurringTransactionEntity transaction = existing.get();
            
            if (updates.getName() != null) transaction.setName(updates.getName());
            if (updates.getDescription() != null) transaction.setDescription(updates.getDescription());
            if (updates.getType() != null) transaction.setType(updates.getType());
            if (updates.getDayOfMonth() != null) transaction.setDayOfMonth(updates.getDayOfMonth());
            if (updates.getAmount() != null) transaction.setAmount(updates.getAmount());
            if (updates.getCategory() != null) transaction.setCategory(updates.getCategory());
            if (updates.getNextDueDate() != null) transaction.setNextDueDate(updates.getNextDueDate());
            if (updates.getNotes() != null) transaction.setNotes(updates.getNotes());
            if (updates.getIsActive() != null) transaction.setIsActive(updates.getIsActive());
            
            transaction.setLastUpdatedDate(LocalDate.now());
            
            return repository.save(transaction);
        }
        throw new IllegalArgumentException("Transaction not found with ID: " + id);
    }
    
    /**
     * Delete a recurring transaction (soft delete - mark as inactive)
     */
    public void deleteRecurringTransaction(Long id) {
        Optional<RecurringTransactionEntity> existing = repository.findById(id);
        if (existing.isPresent()) {
            RecurringTransactionEntity transaction = existing.get();
            transaction.setIsActive(false);
            transaction.setLastUpdatedDate(LocalDate.now());
            repository.save(transaction);
        }
    }
    
    /**
     * Get total monthly obligations
     */
    public double getTotalMonthlyAmount() {
        return getAllActiveTransactions().stream()
            .filter(t -> "MONTHLY".equals(t.getType()))
            .mapToDouble(t -> t.getAmount().doubleValue())
            .sum();
    }
    
    /**
     * Get upcoming transactions for the next N days
     */
    public List<RecurringTransactionEntity> getUpcomingTransactions(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        return repository.findByNextDueDateBetween(today, endDate);
    }
}
