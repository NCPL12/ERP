package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.ReturnableItems;
@Repository
public interface ReturnableItemsRepo extends JpaRepository<ReturnableItems, Integer>{

	@Query("SELECT ri FROM ReturnableItems ri JOIN FETCH ri.returnable WHERE ri.returnedQty <> 0")
	List<ReturnableItems> findAllNonZeroReturned();

}
