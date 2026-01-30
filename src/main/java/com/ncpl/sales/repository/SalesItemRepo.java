package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ncpl.sales.model.SalesItem;


public interface SalesItemRepo  extends JpaRepository<SalesItem, String>{

	@Query(" from SalesItem where sales_order_id=?1 and unit_price>0 and archive=0")
	List<SalesItem> getSalesItemsBySalesOrderId(String salesOrderId);
	@Query(" from SalesItem where sales_order_id=?1 and archive=0")
	List<SalesItem> findSalesItemsBySalesOrderId(String salesOrderId);
	
	@Query(" from SalesItem where sales_order_id=?1")
	List<SalesItem> getSalesItemListBySalesOrderId(String id);
	@Query(" from SalesItem where description=?1")
	SalesItem findSalesItemByDescription(String description);
	@Query(value=" SELECT si.* FROM tbl_sales_item si JOIN si.tbl_sales_order so JOIN sales_order_design d ON d.sales_item_id = si.id JOIN sales_order_design_items di ON di.design.id = d.id WHERE si.description = :description AND so.client_po_number = :clientPoNum AND di.item_id = :itemId",nativeQuery = true)
	SalesItem findSalesItemByDesc(@Param("description") String description,
            @Param("clientPoNum") String clientPoNum,
            @Param("itemId") String itemId);
	@Query("SELECT si FROM SalesItem si WHERE si.description = :description AND si.salesOrder.clientPoNumber = :clientPoNum")
	List<SalesItem> findByDescriptionAndClientPoNumber(@Param("description") String description,
            @Param("clientPoNum") String clientPoNum);
	@Query("SELECT si FROM SalesItem si WHERE si.description = :description AND si.salesOrder.clientPoNumber = :clientPoNum AND si.slNo=:slNo")
	SalesItem findByDescAndClientPoNumberAndSlNo(@Param("description") String description,
            @Param("clientPoNum") String clientPoNum,@Param("slNo") String slNo);
	
}
