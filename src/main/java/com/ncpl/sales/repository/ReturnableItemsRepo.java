package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.ReturnableItems;
@Repository
public interface ReturnableItemsRepo extends JpaRepository<ReturnableItems, Integer>{

	@Query("SELECT ri FROM ReturnableItems ri JOIN FETCH ri.returnable WHERE ri.returnedQty <> 0")
	List<ReturnableItems> findAllNonZeroReturned();

	// @D0014 lazy-loaded, paginated Returnables list (see README.md)
	@Query(value = "SELECT ri FROM ReturnableItems ri JOIN ri.returnable r WHERE ri.returnedQty <> 0 "
			+ "AND (:orderNo = '' OR CAST(r.dcId AS string) LIKE CONCAT('%', :orderNo, '%'))",
			countQuery = "SELECT COUNT(ri) FROM ReturnableItems ri JOIN ri.returnable r WHERE ri.returnedQty <> 0 "
			+ "AND (:orderNo = '' OR CAST(r.dcId AS string) LIKE CONCAT('%', :orderNo, '%'))")
	Page<ReturnableItems> findAllNonZeroReturnedPaged(@Param("orderNo") String orderNo, Pageable pageable);

	List<ReturnableItems> findByReturnableId(int returnableId);

}
