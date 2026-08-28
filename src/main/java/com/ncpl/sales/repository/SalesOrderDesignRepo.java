package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ncpl.sales.model.SalesOrderDesign;

public interface SalesOrderDesignRepo extends JpaRepository<SalesOrderDesign, Long>{
	@Query(" from SalesOrderDesign where sales_item_id=?1 ")
	List<SalesOrderDesign> getDesginListBySoItemId(String salesItemId);
	@Query(" from SalesOrderDesign where sales_item_id IN :salesItemIds ")
	List<SalesOrderDesign> getDesginListBySoItemIds(@Param("salesItemIds") List<String> salesItemIds);
	@Query(" from SalesOrderDesign where sales_item_id=?1 ")
	SalesOrderDesign getDesginObjBySoItemId(String salesItemId);
	
	@Query(" from SalesOrderDesign where item_master_id=?1 and sales_item_id=?2 ")
	SalesOrderDesign findDesignByItemIdAndSalesItemId(String itemId, String salesItemId);
	
	@Query("from SalesOrderDesign where created>= :d1 AND updated <= :d2 and sales_item_id=:soItemId") 
	SalesOrderDesign getDesginObjBySoItemIdByDate(Timestamp d1, Timestamp d2,String soItemId);
	@Query(" from SalesOrderDesign where sales_item_id=?1 ")
	Optional<SalesOrderDesign> getDesginObjBySalesItemId(String salesItemId);
	
	List<SalesOrderDesign> findByIdIn(List<Long> ids);
	
	@Query("SELECT DISTINCT sod.salesItemId FROM SalesOrderDesign sod")
	List<String> findDistinctSalesItemIds();
}