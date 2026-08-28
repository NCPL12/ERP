package com.ncpl.sales.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.CompanyAssets;
@Repository
public interface CompanyAssetsRepo extends JpaRepository<CompanyAssets, Integer>{
	
	@Query(" from CompanyAssets where model=?1 ")
	Optional<CompanyAssets> getCompanyAssetObjByModel(String model);

}
