package com.ncpl.sales.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ncpl.sales.model.DesignItems;

public interface SalesOrderDesignItemsRepo extends JpaRepository<DesignItems, Long>{
	@Query(" from DesignItems where item_id=?1 and design_id=?2")
	List<DesignItems> findDesignItemListByItemIdAndDesignId(String itemId, long id);
	
	@Query(" from DesignItems where item_id=?1 and design_id=?2")
	DesignItems findDesignItemObjByItemIdAndDesignId(String itemId, long id);
	@Query(" from DesignItems where design_id=?1")
	List<DesignItems> findDesignItemListByDesignId(long designId);
	@Query(" from DesignItems where item_id=?1")
	List<DesignItems> findDesignItemListByItemId(String itemId);
	@Query(" from DesignItems where item_id=?1 and design_id=?2")
	Optional<DesignItems> findDesignItemByItemIdAndDesignId(String itemId, long id);
	
	@Query("SELECT di FROM DesignItems di JOIN FETCH di.salesOrderDesign sod WHERE sod.salesItemId IN :salesItemIds")
	List<DesignItems> findBySalesItemIdIn(@Param("salesItemIds") List<String> salesItemIds);

}
