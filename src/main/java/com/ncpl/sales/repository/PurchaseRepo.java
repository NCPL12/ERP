package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
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
	@Query("SELECT po FROM PurchaseOrder po WHERE po.archive=0 ORDER BY po.created DESC")
	public List<PurchaseOrder> findAllPO();

	// Lightweight projection for the "new GRN" PO dropdown - avoids loading every
	// full PurchaseOrder (with its eager party join) just to read poNumber+updated.
	@Query("SELECT new map(po.poNumber as poNumber, po.updated as updated) FROM PurchaseOrder po WHERE po.archive=0 ORDER BY po.created DESC")
	public List<Map<String, Object>> findAllPoNumbersAndDates();
	@Query("SELECT po FROM PurchaseOrder po WHERE po.archive=1 ORDER BY po.created DESC")
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

	@Query(value =
		"SELECT po.po_number, p.party_name, c.name AS city_name, " +
		"COALESCE(SUM(pi.amount), 0) AS total, " +
		"COALESCE(SUM(pi.amount * im.gst / 100.0), 0) AS gst_total, " +
		"po.created, po.version, po.archive " +
		"FROM tbl_purchase_order po " +
		"JOIN tbl_party p ON p.id = po.party_id " +
		"LEFT JOIN city c ON c.id = p.city_id " +
		"LEFT JOIN tbl_purchase_items pi ON pi.po_number = po.po_number " +
		"LEFT JOIN tbl_item_master im ON im.id = pi.model_no " +
		"WHERE po.archive = 0 " +
		"GROUP BY po.po_number, p.party_name, c.name, po.created, po.version, po.archive " +
		"ORDER BY po.created DESC",
		nativeQuery = true)
	List<Object[]> findDashboardData();

}
