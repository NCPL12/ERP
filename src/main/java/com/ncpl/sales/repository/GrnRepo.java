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
	@Query("SELECT g FROM Grn g WHERE g.archive = 0")
	public Page<Grn> findAllGrn(Pageable pageable);
	@Query(" from Grn where archive=1 ")
	public List<Grn> findAllGrnArchived();
	
	// Batch query method for finding GRNs by multiple PO numbers
	public List<Grn> findByPoNumberIn(List<String> poNumbers);
	
	// Search methods for pagination
	@Query("SELECT g FROM Grn g " +
			"JOIN PurchaseOrder po ON po.poNumber = g.poNumber " +
			"JOIN po.party p " +
			"WHERE g.archive = 0 AND " +
			"(LOWER(g.grnId) LIKE LOWER(:keyword) OR " +
			"LOWER(g.poNumber) LIKE LOWER(:keyword) OR " +
			"LOWER(g.invoiceNo) LIKE LOWER(:keyword) OR " +
			"LOWER(p.partyName) LIKE LOWER(:keyword))")
	public Page<Grn> searchGrns(@Param("keyword") String keyword, Pageable pageable);
	
	@Query("SELECT COUNT(g) FROM Grn g " +
			"JOIN PurchaseOrder po ON po.poNumber = g.poNumber " +
			"JOIN po.party p " +
			"WHERE g.archive = 0 AND " +
			"(LOWER(g.grnId) LIKE LOWER(:keyword) OR " +
			"LOWER(g.poNumber) LIKE LOWER(:keyword) OR " +
			"LOWER(g.invoiceNo) LIKE LOWER(:keyword) OR " +
			"LOWER(p.partyName) LIKE LOWER(:keyword))")
	public long countSearchGrns(@Param("keyword") String keyword);
	
	@Query("SELECT COUNT(g) FROM Grn g WHERE g.archive = 0")
	public long countAllNonArchivedGrns();
	
	// Column-specific search: each param is optional (pass null to skip). When non-null, use LIKE with wildcards already in value.
	// searchPoDate: filters by PO date (po.updated) using DATE_FORMAT dd-mm-yy so user can type e.g. "13-05-25"
	@Query("SELECT g FROM Grn g " +
			"JOIN PurchaseOrder po ON po.poNumber = g.poNumber " +
			"JOIN po.party p " +
			"WHERE g.archive = 0 " +
			"AND (:searchGrnId IS NULL OR :searchGrnId = '' OR LOWER(g.grnId) LIKE LOWER(:searchGrnId)) " +
			"AND (:searchPoNumber IS NULL OR :searchPoNumber = '' OR LOWER(g.poNumber) LIKE LOWER(:searchPoNumber)) " +
			"AND (:searchPoDate IS NULL OR :searchPoDate = '' OR LOWER(FUNCTION('DATE_FORMAT', po.updated, '%d-%m-%y')) LIKE LOWER(CONCAT('%', :searchPoDate, '%'))) " +
			"AND (:searchInvoiceNo IS NULL OR :searchInvoiceNo = '' OR LOWER(g.invoiceNo) LIKE LOWER(:searchInvoiceNo)) " +
			"AND (:searchVendor IS NULL OR :searchVendor = '' OR LOWER(p.partyName) LIKE LOWER(:searchVendor))")
	public Page<Grn> searchGrnsByColumns(@Param("searchGrnId") String searchGrnId, @Param("searchPoNumber") String searchPoNumber,
			@Param("searchPoDate") String searchPoDate, @Param("searchInvoiceNo") String searchInvoiceNo, @Param("searchVendor") String searchVendor, Pageable pageable);
	
	@Query("SELECT COUNT(g) FROM Grn g " +
			"JOIN PurchaseOrder po ON po.poNumber = g.poNumber " +
			"JOIN po.party p " +
			"WHERE g.archive = 0 " +
			"AND (:searchGrnId IS NULL OR :searchGrnId = '' OR LOWER(g.grnId) LIKE LOWER(:searchGrnId)) " +
			"AND (:searchPoNumber IS NULL OR :searchPoNumber = '' OR LOWER(g.poNumber) LIKE LOWER(:searchPoNumber)) " +
			"AND (:searchPoDate IS NULL OR :searchPoDate = '' OR LOWER(FUNCTION('DATE_FORMAT', po.updated, '%d-%m-%y')) LIKE LOWER(CONCAT('%', :searchPoDate, '%'))) " +
			"AND (:searchInvoiceNo IS NULL OR :searchInvoiceNo = '' OR LOWER(g.invoiceNo) LIKE LOWER(:searchInvoiceNo)) " +
			"AND (:searchVendor IS NULL OR :searchVendor = '' OR LOWER(p.partyName) LIKE LOWER(:searchVendor))")
	public long countSearchGrnsByColumns(@Param("searchGrnId") String searchGrnId, @Param("searchPoNumber") String searchPoNumber,
			@Param("searchPoDate") String searchPoDate, @Param("searchInvoiceNo") String searchInvoiceNo, @Param("searchVendor") String searchVendor);
	
	/*
	 * @Query("SELECT * FROM Release_date_type a LEFT JOIN cache_media b on a.id=b.id"
	 * ) public List<ReleaseDateType> FindAllWithDescriptionQuery();
	 */
}
