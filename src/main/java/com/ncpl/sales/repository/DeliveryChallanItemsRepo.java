package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.DeliveryChallanItems;

@Repository
public interface DeliveryChallanItemsRepo extends JpaRepository<DeliveryChallanItems,Integer>{
	@Query( value = "SELECT * FROM  tbl_dc_items WHERE description IN (:soItemId)",nativeQuery = true)
	List<DeliveryChallanItems> getDcItemListBySalesItemId(@Param("soItemId") String soItemId);
	
	@Query(" from DeliveryChallanItems where updated>= :d1 and updated<=:d2 and description=:grnSoItemId")
	List<DeliveryChallanItems> findByDate(Timestamp d1, Timestamp d2, String grnSoItemId);

	@Query(" from DeliveryChallanItems where updated<= :d1  and description=:grnSoItemId")
	List<DeliveryChallanItems> findByDateFrom(Timestamp d1, String grnSoItemId);
    
	@Query(" from DeliveryChallanItems where updated>= :d1  and updated<= :d2 and description=:grnSoItemId")
	List<DeliveryChallanItems> findDcListBetweenDate(Timestamp d1, Timestamp d2,String grnSoItemId);
	@Query(" from DeliveryChallanItems where  updated<= :d1")
	List<DeliveryChallanItems> findDcListLessThanDate(Timestamp d1);
	@Query(" from DeliveryChallanItems where updated>= :d1 and updated<= :d2 and description=:grnSoItemId")
	List<DeliveryChallanItems> findByBetweenDateAndSoItem(Timestamp d1, Timestamp d2,String grnSoItemId);
	
	@Query( value = "SELECT * FROM  tbl_dc_items WHERE (description IN (:soItemId)) and (delivered_quantity<>0 or todays_qty<>0)",nativeQuery = true)
	List<DeliveryChallanItems> getDcItemListBySOItemIdWhereDcQtyNonZero(@Param("soItemId") String soItemId);
	
	@Query("SELECT dci FROM DeliveryChallanItems dci LEFT JOIN FETCH dci.deliveryChallan WHERE dci.description IN :salesItemIds")
	List<DeliveryChallanItems> findBySalesItemIdIn(@Param("salesItemIds") List<String> salesItemIds);
    
    
}