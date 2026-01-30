package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ncpl.sales.model.PurchaseCopy;


public interface PurchaseCopyRepo extends JpaRepository<PurchaseCopy, Integer>{

	@Query(" from PurchaseCopy where po_number=?1 ")
	public PurchaseCopy findByPoNo(String poNumber);

}
