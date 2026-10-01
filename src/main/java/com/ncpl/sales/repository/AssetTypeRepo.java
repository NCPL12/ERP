package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.AssetType;

@Repository
public interface AssetTypeRepo extends JpaRepository<AssetType, Integer> {
	List<AssetType> findByNameIgnoreCase(String name);
}
