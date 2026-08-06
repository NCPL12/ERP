package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Lot;

@Repository
public interface LotRepo extends JpaRepository<Lot, Integer> {
	@Query(value = "SELECT l.* FROM tbl_tds_lots l "
			+ "INNER JOIN tbl_tds_items ti ON l.tds_item_id = ti.tds_item_id "
			+ "INNER JOIN tbl_tds t ON ti.tds_id = t.tds_id "
			+ "WHERE t.so_number = :soNumber AND ti.description = :salesItemId "
			+ "AND (:itemMasterId IS NULL OR :itemMasterId = '' OR ti.model_number = :itemMasterId)", nativeQuery = true)
	List<Lot> findLotsBySoNumberAndDescription(@Param("soNumber") String soNumber,
			@Param("salesItemId") String salesItemId, @Param("itemMasterId") String itemMasterId);

	@Query(value = "SELECT l.lot_number, l.quantity, ti.description, ti.model_number FROM tbl_tds_lots l "
			+ "INNER JOIN tbl_tds_items ti ON l.tds_item_id = ti.tds_item_id "
			+ "INNER JOIN tbl_tds t ON ti.tds_id = t.tds_id "
			+ "WHERE t.so_number = :soNumber", nativeQuery = true)
	List<Object[]> findAllLotsBySoNumber(@Param("soNumber") String soNumber);
}
