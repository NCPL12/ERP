package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.ncpl.sales.model.PurchaseOrder;


@Repository
public interface PurchaseRepo extends JpaRepository<PurchaseOrder, String> {
	
	@Query("SELECT po FROM PurchaseOrder po where po.poNumber = ?1 AND po.version = ?2")
	public Optional<PurchaseOrder> findPurchaseOrderByNoAndVersion(String poNumber, String version);
	@Query(" from PurchaseOrder where party_id=?1")
	public List<PurchaseOrder> getPurchaseListByPartyId(String partyId);
	@Query(" from PurchaseOrder where created>= :d1  and created<= :d2")
	public List<PurchaseOrder> findPurchaseOrderListByDate(Timestamp d1, Timestamp d2);
	@Query("SELECT po FROM PurchaseOrder po WHERE po.archive = false ORDER BY po.created DESC")
	public List<PurchaseOrder> findAllPO();
	@Query(" from PurchaseOrder where archive=1 ")
	public List<PurchaseOrder> findAllArchivedPOList();
	@Query(value = "SELECT * FROM  tbl_purchase_order WHERE po_number NOT IN (SELECT po_number from tbl_grn) and archive=0",nativeQuery = true)
	public List<PurchaseOrder> getPurchaseListWhereGrnNotDone();
	@Query(value="SELECT distinct po.*, \r\n" + 
			"    ( SELECT sum(pi.quantity)\r\n" + 
			"      FROM tbl_purchase_items pi \r\n" + 
			"      WHERE po.po_number=pi.po_number and po.archive=0) poQty,\r\n" + 
			"    ( SELECT sum(gi.received_quantity)\r\n" + 
			"      FROM tbl_grn g inner join tbl_grn_items gi on g.grn_id=gi.grn_id\r\n" + 
			"      WHERE  po.po_number=g.po_number and po.archive=0) grnQty\r\n" + 
			"FROM tbl_purchase_order po \r\n" + 
			"group by po.po_number\r\n" + 
			"having poQty>grnQty;",nativeQuery = true)
	public List<PurchaseOrder> getpendingPoList();
	
	@Query("SELECT DISTINCT po FROM PurchaseOrder po LEFT JOIN FETCH po.party WHERE po.poNumber IN :poNumbers")
	List<PurchaseOrder> findByPoNumberIn(@Param("poNumbers") List<String> poNumbers);
	
	long countByArchiveFalse();
	
	@Query(value = "SELECT COUNT(*) FROM tbl_purchase_order WHERE archive = 0", nativeQuery = true)
	long countActivePurchaseOrders();

	@Query(value = "SELECT COUNT(*) FROM tbl_purchase_order po WHERE po.archive = 0 AND NOT EXISTS (SELECT 1 FROM tbl_grn g WHERE g.po_number = po.po_number)", nativeQuery = true)
	long countPurchaseOrdersWithoutGRN();

	@Query(value = "SELECT COUNT(DISTINCT po.po_number) FROM tbl_purchase_order po " +
			"WHERE po.archive = 0 AND EXISTS (SELECT 1 FROM tbl_grn g WHERE g.po_number = po.po_number) " +
			"AND (SELECT SUM(pi.quantity) FROM tbl_purchase_items pi WHERE pi.po_number = po.po_number) > " +
			"(SELECT COALESCE(SUM(gi.received_quantity), 0) FROM tbl_grn g JOIN tbl_grn_items gi ON g.grn_id = gi.grn_id WHERE g.po_number = po.po_number)", nativeQuery = true)
	long countPendingPurchaseOrdersPartialGRN();
}
