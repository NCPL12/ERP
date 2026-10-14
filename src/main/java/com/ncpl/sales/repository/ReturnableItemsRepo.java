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
 @Query("select ri from ReturnableItems ri join fetch ri.returnable r where r.id = :id")
 List<ReturnableItems> findByReturnableId(@org.springframework.data.repository.query.Param("id") int id);
 @Query(value = "select ri from ReturnableItems ri join fetch ri.returnable r where ri.returnedQty <> 0 and (:keyword = '' or cast(r.dcId as string) like concat('%', :keyword, '%') or exists (select si.id from DeliveryChallanItems di, SalesItem si where di.dcItemId = ri.dcItemId and si.id = di.description and lower(si.description) like lower(concat('%', :keyword, '%'))))",
 countQuery = "select count(ri) from ReturnableItems ri join ri.returnable r where ri.returnedQty <> 0 and (:keyword = '' or cast(r.dcId as string) like concat('%', :keyword, '%') or exists (select si.id from DeliveryChallanItems di, SalesItem si where di.dcItemId = ri.dcItemId and si.id = di.description and lower(si.description) like lower(concat('%', :keyword, '%'))))")
 org.springframework.data.domain.Page<ReturnableItems> searchReturnedItems(@org.springframework.data.repository.query.Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

	@Query("SELECT ri FROM ReturnableItems ri JOIN FETCH ri.returnable WHERE ri.returnedQty <> 0")
	List<ReturnableItems> findAllNonZeroReturned();

	// @D0014 lazy-loaded, paginated Returnables list (see README.md)
	@Query(value = "SELECT ri FROM ReturnableItems ri JOIN ri.returnable r WHERE ri.returnedQty <> 0 "
			+ "AND (:orderNo = '' OR CAST(r.dcId AS string) LIKE CONCAT('%', :orderNo, '%'))",
			countQuery = "SELECT COUNT(ri) FROM ReturnableItems ri JOIN ri.returnable r WHERE ri.returnedQty <> 0 "
			+ "AND (:orderNo = '' OR CAST(r.dcId AS string) LIKE CONCAT('%', :orderNo, '%'))")
	Page<ReturnableItems> findAllNonZeroReturnedPaged(@Param("orderNo") String orderNo, Pageable pageable);


}
