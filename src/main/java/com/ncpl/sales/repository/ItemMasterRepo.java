package com.ncpl.sales.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.ItemMaster;

@Repository
public interface ItemMasterRepo extends JpaRepository<ItemMaster,String>{

	@Query(" from ItemMaster where model=?1 ")
	public ItemMaster getItemByModelNo(String model);
	@Query(value="SELECT * from tbl_item_master WHERE created>=:fromDate and created<=:toDate",nativeQuery = true)
	List<ItemMaster> findItemListByDateUpdated(Timestamp fromDate, Timestamp toDate);
	@Query(value = "SELECT * FROM  tbl_item_master WHERE id IN (SELECT item_master_id from tbl_stock where quantity>0)",nativeQuery = true)
	public List<ItemMaster> findAllItemsWhereStockQtyNonZero();
	@Query(" from ItemMaster where tool_tracker=false and company_assets=false ")
	public List<ItemMaster> findItemListWithoutTools();
	@Query(" from ItemMaster where tool_tracker=true ")
	public List<ItemMaster> findItemListWithTools();
	@Query(" from ItemMaster where model=?1 ")
	public boolean existsByModelNumber(String model);
	@Query(" from ItemMaster where company_assets=true ")
	public List<ItemMaster> findItemListWithComapnyAssets();
	
	// Optimized search methods for pagination
		Page<ItemMaster> findByModelContainingIgnoreCaseOrItemNameContainingIgnoreCaseOrHsnCodeContainingIgnoreCase(
			String model, String itemName, String hsnCode, Pageable pageable);
		
		long countByModelContainingIgnoreCaseOrItemNameContainingIgnoreCaseOrHsnCodeContainingIgnoreCase(
			String model, String itemName, String hsnCode);
		
		// Pagination methods for toolTracker items
		@Query(value = "SELECT * FROM tbl_item_master WHERE tool_tracker=1 AND (model LIKE CONCAT('%', :search, '%') OR item_name LIKE CONCAT('%', :search, '%') OR hsn_code LIKE CONCAT('%', :search, '%'))", nativeQuery = true)
		Page<ItemMaster> findToolTrackerItemsWithSearch(@Param("search") String search, Pageable pageable);
		
		@Query(value = "SELECT * FROM tbl_item_master WHERE tool_tracker=1", nativeQuery = true)
		Page<ItemMaster> findToolTrackerItems(Pageable pageable);
		
		@Query(value = "SELECT COUNT(*) FROM tbl_item_master WHERE tool_tracker=1 AND (model LIKE CONCAT('%', :search, '%') OR item_name LIKE CONCAT('%', :search, '%') OR hsn_code LIKE CONCAT('%', :search, '%'))", nativeQuery = true)
		long countToolTrackerItemsWithSearch(@Param("search") String search);
		
		@Query(value = "SELECT COUNT(*) FROM tbl_item_master WHERE tool_tracker=1", nativeQuery = true)
		long countToolTrackerItems();
		
		@Query("SELECT im FROM ItemMaster im WHERE im.id IN :itemIds")
		List<ItemMaster> findByIdIn(@Param("itemIds") List<String> itemIds);
		
}
