package com.ncpl.sales.repository;

import java.sql.Timestamp;
//import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.GrnItems;

@Repository
public interface GrnItemRepo extends JpaRepository<GrnItems , Integer>{
	@Query(" from GrnItems where po_item_id=?1 ")
	List<GrnItems> findByPoItemId(String poItemId);
	
	@Query(" from GrnItems where updated>= :d1 and updated<=:d2")
	List<GrnItems> findByDate(Timestamp d1, Timestamp d2);
	
	@Query("from GrnItems where created>= :d1 AND updated <= :d2 and po_item_id=:poId") 
	List<GrnItems> findByDateAndPoId(Timestamp d1, Timestamp d2,String poId);
	 
	
	/*
	 * @Query("SELECT item, SUM(item.receivedQuantity) as qty FROM GrnItems item WHERE item.updated BETWEEN :fromDate AND :toDate GROUP BY item.description"
	 * ) List<GrnItems> findInwardQuantityBewteenDates(Timestamp fromDate, Timestamp
	 * toDate);
	 */
	 
	
	
	@Query("SELECT item, SUM(item.receivedQuantity) as qty FROM GrnItems item WHERE item.updated<=:fromDate GROUP BY item.description") 
	List<GrnItems> findInwardQuantityBewteenDates(Timestamp fromDate);
	 
    @Query(" from GrnItems where po_item_id=?1 ")
	List<GrnItems> findGrnObjByPoItemId(String poItemId);

    @Query(" from GrnItems where po_item_id=:poItemId and updated<=:d2")
    List<GrnItems> findByPoItemIdAndUpdatedDate(String poItemId, Timestamp d2);
    
    @Query(" from GrnItems where grn_id=?1 ")
  	List<GrnItems> findGrnItemsByGrnId(String grnId);
    @Query(" from GrnItems where po_item_id=?1 and receivedQuantity!=0")
	List<GrnItems> findByPoItemIdWhereRcvdQtyNonZero(String poItemId);
    
 // Batch query method for finding GRN items by multiple purchase item IDs (description field maps to po_item_id)
 	List<GrnItems> findByDescriptionIn(List<String> purchaseItemIds);
}
