package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ncpl.sales.model.SalesOrderDesign;

public interface SalesOrderDesignRepo extends JpaRepository<SalesOrderDesign, Long>{
	@Query(" from SalesOrderDesign where sales_item_id=?1 ")
	List<SalesOrderDesign> getDesginListBySoItemId(String salesItemId);
	@Query(" from SalesOrderDesign where sales_item_id=?1 ")
	SalesOrderDesign getDesginObjBySoItemId(String salesItemId);
	
	@Query(" from SalesOrderDesign where item_master_id=?1 and sales_item_id=?2 ")
	SalesOrderDesign findDesignByItemIdAndSalesItemId(String itemId, String salesItemId);
	
	@Query("from SalesOrderDesign where created>= :d1 AND updated <= :d2 and sales_item_id=:soItemId") 
	SalesOrderDesign getDesginObjBySoItemIdByDate(Timestamp d1, Timestamp d2,String soItemId);
	@Query(" from SalesOrderDesign where sales_item_id=?1 ")
	Optional<SalesOrderDesign> getDesginObjBySalesItemId(String salesItemId);
	
	// Batch fetch by primary keys
	List<SalesOrderDesign> findByIdIn(List<Long> ids);
	

	/*
	 * @Query("select a from SalesOrderDesign a join a.DesignItems b where b.sales_order_design_id = :prop1 and b.item_id=:prop2"
	 * ) SalesOrderDesign getDesignByItemIdAndDesignId(@Param("prop1") String
	 * salesOrderDesignId, @Param("prop2") String itemId);
	 */
	//@Query("select a from SalesOrderDesign a join a.DesignItems b where b.sales_order_design_id = :prop1 and b.item_id=:prop2")
	/*@Query(value = "select * from sales_order_design_items s where s.sales_order_design_id = :sales_order_design_id and s.item_id = :item_id",nativeQuery = true) 
	void getDesignByItemIdAndDesignId(@Param("sales_order_design_id") long sales_order_design_id, @Param("item_id") String item_id);*/


}
