package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.CompanyAssets;
@Repository
public interface CompanyAssetsRepo extends JpaRepository<CompanyAssets, Integer>{

}
