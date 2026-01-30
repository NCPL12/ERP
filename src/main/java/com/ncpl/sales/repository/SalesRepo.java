package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.SalesOrder;

@Repository

public interface SalesRepo extends JpaRepository<SalesOrder, String> {

	@Query( value = "SELECT * FROM  tbl_sales_order WHERE id IN (:id)",nativeQuery = true)
	public List<SalesOrder> findSalesOrderById(@Param("id") List<String> id);
	@Query(" from SalesOrder where party_id=?1 and archive=0")
	public List<SalesOrder> getSalesListByPartyId(String partyId);
	@Query(" from SalesOrder where party_id=?1")
	public List<SalesOrder> getSalesListByParty(String partyId);
	
	@Query(" from SalesOrder where party_id=?1")
	public List<SalesOrder> getSalesListByClientId(String partyId);
	
	@Query( value = "SELECT * FROM  tbl_sales_order WHERE party_id IN (:partyId) and archive=0",nativeQuery = true)
	public List<SalesOrder> getSalesOrdersByPartyId(@Param("partyId") String partyId);
	
	@Query(" from SalesOrder where client_po_number=?1 ")
	public SalesOrder getSalesOrderByClientPoNumber(String clientPoNO);
	@Query(" from SalesOrder where region=:region  AND updated >= :sqlFromDate and updated <= :sqlToDate and archive=0")
	public List<SalesOrder> findByRegion( String region,Timestamp sqlFromDate, Timestamp sqlToDate);
	@Query(" from SalesOrder where archive=0 ")
	public List<SalesOrder> findAllSalesOrder();
	@Query(" from SalesOrder where archive=1 ")
	public List<SalesOrder> findArchivedSalesList();
	 
	@Query(value = "SELECT * FROM  tbl_sales_order WHERE id NOT IN (SELECT so_number from tbl_dc) and archive=0",nativeQuery = true)
	public List<SalesOrder> getSalesListWhereDCNotDone();
	@Query(value = "SELECT * FROM  tbl_sales_order WHERE id IN (SELECT so_number from tbl_dc) and archive=0",nativeQuery = true)
	public List<SalesOrder> getSalesListWhereDCDone();
	@Query(value = "SELECT distinct so.* FROM tbl_sales_order so where so.id\r\n" + 
			"in (select si.sales_order_id from tbl_sales_item si where si.id not in(select d.sales_item_id from sales_order_design d) and si.units_id<>20 and si.quantity<>0 and si.unit_price>0) and so.archive=0",nativeQuery = true)
	public List<SalesOrder> getSalesOrderWithoutDesign();
	@Query(value="SELECT distinct so.*, \r\n" + 
			"    ( SELECT sum(si.quantity)\r\n" + 
			"      FROM tbl_sales_item si \r\n" + 
			"      WHERE so.id=si.sales_order_id and so.archive=0) salesQty,\r\n" + 
			"    ( SELECT sum(di.todays_qty)\r\n" + 
			"      FROM tbl_dc d inner join tbl_dc_items di on d.dc_id=di.dc_id\r\n" + 
			"      WHERE  so.id=d.so_number and so.archive=0) dc_qty\r\n" + 
			"FROM tbl_sales_order so \r\n" + 
			"group by so.id\r\n" + 
			"having salesQty>dc_qty;",nativeQuery = true)
	public List<SalesOrder> getpendingSoList();
	@Query(value = "SELECT * FROM tbl_sales_order so where so.id in \r\n" + 
			"(select si.sales_order_id from tbl_sales_item si where si.id in\r\n" + 
			"(select td.description from tbl_tds_items td where td.site_quantity>0 and td.tds_approved=1)and \r\n" + 
			"si.id not in(select pi.sales_item_id from tbl_purchase_items pi))and so.archive=0;",nativeQuery = true)
	public ArrayList<SalesOrder> getTdsApprovedAndPoNotDoneListDashboard();
	@Query(value = "SELECT distinct so.* FROM tbl_sales_order so where so.id\r\n" + 
			"in (select si.sales_order_id from tbl_sales_item si where si.id not in(select d.sales_item_id from sales_order_design d) and si.units_id<>20 and si.quantity<>0 and si.unit_price>0) and so.party_id IN (:partyId) and so.archive=0 and so.region='Bangalore'",nativeQuery = true)
	public List<SalesOrder> findSalesOrderWithoutDesignByPartyIdBan(@Param("partyId") String partyId);
	
	@Query(value = "SELECT distinct so.* FROM tbl_sales_order so where so.id\r\n" + 
			"in (select si.sales_order_id from tbl_sales_item si where si.id not in(select d.sales_item_id from sales_order_design d) and si.units_id<>20 and si.quantity<>0 and si.unit_price>0) and so.party_id IN (:partyId) and so.archive=0 and so.region='Mangalore'",nativeQuery = true)
	public List<SalesOrder> findSalesOrderWithoutDesignByPartyIdMan(@Param("partyId") String partyId);
	@Query(" from SalesOrder where updated >= :sqlFromDate and updated <= :sqlToDate and archive=0")
	public List<SalesOrder> findSalesListByDate(Timestamp sqlFromDate,Timestamp sqlToDate);
	@Query(value = "SELECT DISTINCT so.* " +
		    "FROM tbl_sales_order so " +
		    "JOIN tbl_sales_item si ON si.sales_order_id = so.id " +
		    "JOIN sales_order_design d ON d.sales_item_id = si.id " +
		    "JOIN sales_order_design_items dit ON dit.design_id = d.design_id " +
		    "LEFT JOIN tbl_purchase_items pi " +
		    "  ON pi.sales_item_id = d.sales_item_id AND pi.model_no = dit.item_id " +
		    "WHERE pi.purchase_item_id IS NULL " +
		    "  AND si.units_id <> 20 " +
		    "  AND si.quantity <> 0 " +
		    "  AND si.unit_price > 0 " +
		    "  AND so.archive = 0", nativeQuery = true)
	public List<SalesOrder> getSalesOrderWithDesign();
	
	@Query(" from SalesOrder where client_po_number=?1 ")
	public Optional<SalesOrder> getSalesOrderByClientPoNo(String clientPoNO);
}

