package com.ncpl.sales.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.LaptopAssignment;

@Repository
public interface LaptopAssignmentRepo extends JpaRepository<LaptopAssignment, Integer> {

	Optional<LaptopAssignment> findByCompanyAsset_IdAndDateReturnedIsNull(int companyAssetId);

}
