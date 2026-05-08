package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.ncpl.sales.model.PurchaseItem;

@Repository
public interface PurchaseItemRepo extends JpaRepository<PurchaseItem,Integer>{
	
	@Query(" from PurchaseItem where po_number=?1 ")
	List<PurchaseItem> findByPurchaseOrder_PoNumber(String poNumber);
	@Query(" from PurchaseItem where model_no=?1 ")
	List<PurchaseItem> findByModelNumber(String model);
	@Query(" from PurchaseItem where sales_item_id=?1 ")
	List<PurchaseItem> findBySalesItemId(String soItemId);
	@Query(" from PurchaseItem where sales_item_id=?1 and model_no=?2")
	PurchaseItem findPoItemBySoItemAndItemId(String soItemId, String itemId);
	@Query(" from PurchaseItem where model_no=?1 order by purchase_item_id desc ")
	List<PurchaseItem> findByModelNumberWithLatestPoItem(String model);
//	@Query("select sistinct model_no from PurchaseItem")
//	List<PurchaseItem> findModelNumber();
	@Query(" from PurchaseItem where sales_item_id=?1 and model_no=?2")
	List<PurchaseItem> findPoItemListBySoItemAndItemId(String soItemId, String itemId);
	@Query(" from PurchaseItem where model_no=?1 order by unitPrice")
	List<PurchaseItem> findCheapestPurchaseItem(String modelNo);

	@Query("SELECT pi FROM PurchaseItem pi LEFT JOIN FETCH pi.purchaseOrder WHERE pi.description IN :salesItemIds")
	List<PurchaseItem> findBySalesItemIdIn(@Param("salesItemIds") List<String> salesItemIds);

	@Query(value = "SELECT po.po_number, " +
			"COALESCE(SUM(pi.amount), 0) AS total, " +
			"COALESCE(SUM(pi.amount * im.gst / 100.0), 0) AS gstTotal " +
			"FROM tbl_purchase_order po " +
			"LEFT JOIN tbl_purchase_items pi ON pi.po_number = po.po_number " +
			"LEFT JOIN tbl_item_master im ON im.id = pi.model_no " +
			"WHERE po.archive = 0 " +
			"GROUP BY po.po_number",
			nativeQuery = true)
	List<Object[]> getActivePoTotals();

	@Query(value = "SELECT po.po_number, " +
			"COALESCE(SUM(pi.amount), 0) AS total, " +
			"COALESCE(SUM(pi.amount * im.gst / 100.0), 0) AS gstTotal " +
			"FROM tbl_purchase_order po " +
			"LEFT JOIN tbl_purchase_items pi ON pi.po_number = po.po_number " +
			"LEFT JOIN tbl_item_master im ON im.id = pi.model_no " +
			"WHERE po.archive = 1 " +
			"GROUP BY po.po_number",
			nativeQuery = true)
	List<Object[]> getArchivedPoTotals();


}
