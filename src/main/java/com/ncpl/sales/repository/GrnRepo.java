package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Grn;

@Repository
public interface GrnRepo extends JpaRepository<Grn, String>{
	@Query( value = "SELECT * FROM  tbl_grn WHERE grn_id IN (:grnId)",nativeQuery = true)
	public List<Grn> getGrnListById(@Param("grnId") String grnId);
	@Query(" from Grn where po_number=?1 ")
	public List<Grn> findGrnListByPoNumber(String poNumber);
	@Query(" from Grn where archive=0 order by created desc ")
	public List<Grn> findAllGrn();
	@Query("SELECT g FROM Grn g WHERE g.archive = 0 order by g.created desc")
	public Page<Grn> findAllGrn(Pageable pageable);
	@Query(" from Grn where archive=1 ")
	public List<Grn> findAllGrnArchived();
	
	// Batch query method for finding GRNs by multiple PO numbers
	public List<Grn> findByPoNumberIn(List<String> poNumbers);
	
	// Search methods for pagination
	@Query("SELECT g FROM Grn g WHERE g.archive = 0 AND " +
		   "(LOWER(g.grnId) LIKE LOWER(:keyword) OR " +
		   "LOWER(g.poNumber) LIKE LOWER(:keyword) OR " +
		   "LOWER(g.invoiceNo) LIKE LOWER(:keyword))")
	public Page<Grn> searchGrns(@Param("keyword") String keyword, Pageable pageable);
	
	@Query("SELECT COUNT(g) FROM Grn g WHERE g.archive = 0 AND " +
		   "(LOWER(g.grnId) LIKE LOWER(:keyword) OR " +
		   "LOWER(g.poNumber) LIKE LOWER(:keyword) OR " +
		   "LOWER(g.invoiceNo) LIKE LOWER(:keyword))")
	public long countSearchGrns(@Param("keyword") String keyword);
	
	@Query("SELECT COUNT(g) FROM Grn g WHERE g.archive = 0")
	public long countAllNonArchivedGrns();
	
	/*
	 * @Query("SELECT * FROM Release_date_type a LEFT JOIN cache_media b on a.id=b.id"
	 * ) public List<ReleaseDateType> FindAllWithDescriptionQuery();
	 */
}
