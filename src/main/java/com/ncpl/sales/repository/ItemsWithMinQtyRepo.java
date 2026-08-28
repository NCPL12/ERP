package com.ncpl.sales.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.ItemsWithMinQty;


@Repository
public interface ItemsWithMinQtyRepo  extends JpaRepository<ItemsWithMinQty, Integer> {

	@Query(" from ItemsWithMinQty where item_id=?1 ")
	Optional<ItemsWithMinQty> findbyItemId(String itemId);

}
