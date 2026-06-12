package com.ncpl.sales.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Tds;

@Repository
public interface TdsRepo extends JpaRepository<Tds, Integer>{
	
	@Query(" from Tds where soNumber=?1 ")
	List<Tds> getTdsListBySoNumber(String soNumber);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tbl_tds_lots WHERE tds_item_id IN (SELECT tds_item_id FROM tbl_tds_items WHERE tds_id IN (SELECT tds_id FROM tbl_tds WHERE so_number = ?1))", nativeQuery = true)
	void deleteLotsBySoNumber(String soNumber);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tbl_tds_items WHERE tds_id IN (SELECT tds_id FROM tbl_tds WHERE so_number = ?1)", nativeQuery = true)
	void deleteItemsBySoNumber(String soNumber);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tbl_tds WHERE so_number = ?1", nativeQuery = true)
	void deleteTdsBySoNumber(String soNumber);

}
