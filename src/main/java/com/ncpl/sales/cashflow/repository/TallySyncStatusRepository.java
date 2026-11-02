package com.ncpl.sales.cashflow.repository;

import com.ncpl.sales.cashflow.entity.TallySyncStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for TallySyncStatusEntity - tracks Tally sync status
 */
@Repository
public interface TallySyncStatusRepository extends JpaRepository<TallySyncStatusEntity, Long> {
    
    /**
     * Get the most recent sync status
     */
    Optional<TallySyncStatusEntity> findFirstByOrderByIdDesc();
}
