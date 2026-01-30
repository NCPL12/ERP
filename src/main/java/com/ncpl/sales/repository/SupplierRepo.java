package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Supplier;

@Repository
public interface SupplierRepo extends JpaRepository<Supplier, String>{
	@Query(" from Supplier where item_master_id=?1 ")
	List<Supplier> getAllSupplierByItemId(String itemId);
	
	@Query(" from Supplier where item_master_id=?1 and preferred='Yes'")
	List<Supplier> getSupplierWithPreferredYes(String itemId);

	@Query(" from Supplier where item_master_id=?1 and party_id=?2")
	List<Supplier> findSupplierListBySupplierName(String itemId, String supplierName);
	
	@Query(" from Supplier where item_master_id=?1 and party_id=?2 and supplierId!=?3")
	List<Supplier> findSupplierListBySupplierNameForEdit(String itemId, String supplierName,String supplierId);
	
	@Query(" from Supplier where item_master_id=?1 and party_id=?2")
	Supplier findSupplierByItemIdAndClientId(String itemId, String supplierName);

}
