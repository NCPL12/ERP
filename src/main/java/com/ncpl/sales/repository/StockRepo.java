package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Stock;
@Repository
public interface StockRepo extends JpaRepository<Stock,String>{
	@Query(" from Stock where item_master_id=?1 and quantity>0")
	List<Stock> getAllStockByItemId(String itemId);
	
	@Query(" from Stock where item_master_id=?1 and party_id=?2")
	List<Stock> findStockListByClientName(String itemId, String clientName);
	
	@Query(" from Stock where item_master_id=?1 and party_id=?2 and stockId!=?3")
	List<Stock> findStockListByClientNameForEdit(String itemId, String clientName, String stockId);
	
	@Query("from Stock  where updated >= :fromDate and updated <= :toDate")
	List<Stock> findStockByDateUpdated(Timestamp fromDate,Timestamp toDate);
	@Query("from Stock  where updated <= :date")
	public List<Stock> getStockByDate(Date date);
	@Query(" from Stock where item_master_id=?1 and party_id=?2")
	Stock findStockByClientAndItemId(String itemId, String clientId);
	
	
	/*
	 * @Query(" from Stock where item_master_id=?1 ") Stock findStockByItemId(String
	 * itemId);
	 */
	
	@Query("from Stock where created>= :d1 AND updated <= :d2 and item_master_id=:itemId") 
	List<Stock> findStockByItemId(Timestamp d1, Timestamp d2,String itemId);
	
	@Query("from Stock where item_master_id=?1 and activity=?2")
	public List<Stock> findStockByActivity(String itemId,String activity);

	
	  @Query("from Stock where item_master_id=:itemId AND created>= :sqlFromDate and created<= :sqlToDate order by updated desc")
	  public List<Stock> findStockByUpdatedWithDescOrder(String itemId, Timestamp sqlFromDate, Timestamp sqlToDate);

	@Query(" from Stock where party_id=?1")
	List<Stock> findStockListByClientId(String id);
	
	@Query(value = "SELECT * FROM tbl_stock s WHERE s.party_id = ?1 AND s.quantity <> 0 " +
            "AND s.item_master_id IN (SELECT i.id FROM tbl_item_master i WHERE i.company_assets = false)",nativeQuery = true)
	List<Stock> findStockListByPartyId(String id);

	@Query("from Stock where item_master_id=:itemId AND quantity>0 AND updated<= :sqlToDate order by updated desc")
	List<Stock> getStockByItemIdWithDate(String itemId,Timestamp sqlToDate );
	 
}
