package com.ncpl.sales.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.DeliveryChallan;

@Repository
public interface DeliveryChallanRepo extends 
        JpaRepository<DeliveryChallan, Integer>, 
        JpaSpecificationExecutor<DeliveryChallan> {

    // Find DC by DC ID
    Optional<DeliveryChallan> findByDcId(Integer dcId);

    // Find DCs by SO Number
    List<DeliveryChallan> findBySoNumber(String soNumber);

    // Find DCs by archive status
    List<DeliveryChallan> findByArchive(boolean archive);

    // Find DCs by item description (corrected)
    @Query("SELECT DISTINCT dc FROM DeliveryChallan dc JOIN dc.items i " +
           "WHERE LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DeliveryChallan> findByItemDescription(@Param("keyword") String keyword);

    // Generic search by DC ID, SO number, client PO, or party name
	@Query("SELECT DISTINCT dc FROM DeliveryChallan dc " +
	       "LEFT JOIN SalesOrder so ON so.id = dc.soNumber " +
	       "LEFT JOIN so.party p " +
	       "WHERE dc.archive = false " +
	       "AND (CAST(dc.dcId AS string) LIKE CONCAT('%', :keyword, '%') " +
	       "OR LOWER(dc.soNumber) LIKE CONCAT('%', LOWER(:keyword), '%') " +
	       "OR LOWER(so.clientPoNumber) LIKE CONCAT('%', LOWER(:keyword), '%') " +
	       "OR LOWER(p.partyName) LIKE CONCAT('%', LOWER(:keyword), '%'))")
    List<DeliveryChallan> searchByKeyword(@Param("keyword") String keyword);

	// Pageable variant for server-side DataTables
	@Query("SELECT DISTINCT dc FROM DeliveryChallan dc " +
	       "LEFT JOIN SalesOrder so ON so.id = dc.soNumber " +
	       "LEFT JOIN so.party p " +
	       "WHERE dc.archive = false " +
	       "AND (CAST(dc.dcId AS string) LIKE CONCAT('%', :keyword, '%') " +
	       "OR LOWER(dc.soNumber) LIKE CONCAT('%', LOWER(:keyword), '%') " +
	       "OR LOWER(so.clientPoNumber) LIKE CONCAT('%', LOWER(:keyword), '%') " +
	       "OR LOWER(p.partyName) LIKE CONCAT('%', LOWER(:keyword), '%'))")
	Page<DeliveryChallan> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Fetch all active (non-archived) DCs
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.archive = false")
    List<DeliveryChallan> findAllActiveDc();

    // Fetch all active (non-archived) DCs with pagination
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.archive = false")
    Page<DeliveryChallan> findAllActive(Pageable pageable);

    // Fetch all non-archived DCs (alias used by services)
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.archive = false")
    List<DeliveryChallan> findAllDc();

    // Fetch DCs by Sales Order ID
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.soNumber = :soId")
    List<DeliveryChallan> getAllDcBySoId(@Param("soId") String soId);

    // Fetch DC list by DC id (as a list for compatibility)
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.dcId = :dcId")
    List<DeliveryChallan> findDcListById(@Param("dcId") int dcId);

    // Fetch archived DCs
    @Query("SELECT dc FROM DeliveryChallan dc WHERE dc.archive = true")
    List<DeliveryChallan> findDcListArchived();

	// Advanced, column-wise, pageable search
	@Query("SELECT DISTINCT dc FROM DeliveryChallan dc " +
	       "LEFT JOIN SalesOrder so ON so.id = dc.soNumber " +
	       "LEFT JOIN so.party p " +
	       "LEFT JOIN PartyAddress pa ON pa.id = so.shippingAddress " +
	       "WHERE dc.archive = false " +
	       "AND (:dcId IS NULL OR dc.dcId = :dcId) " +
	       "AND (:soNumber IS NULL OR LOWER(dc.soNumber) LIKE CONCAT('%', LOWER(:soNumber), '%')) " +
	       "AND (:clientName IS NULL OR LOWER(p.partyName) LIKE CONCAT('%', LOWER(:clientName), '%')) " +
	       "AND (:clientPo IS NULL OR LOWER(so.clientPoNumber) LIKE CONCAT('%', LOWER(:clientPo), '%')) " +
	       "AND (:shipping IS NULL OR LOWER(pa.addr1) LIKE CONCAT('%', LOWER(:shipping), '%') OR LOWER(p.addr1) LIKE CONCAT('%', LOWER(:shipping), '%'))")
	Page<DeliveryChallan> searchAdvanced(
	        @Param("dcId") Integer dcId,
	        @Param("soNumber") String soNumber,
	        @Param("clientName") String clientName,
	        @Param("clientPo") String clientPo,
	        @Param("shipping") String shipping,
	        Pageable pageable);
	
	@Query("SELECT dc FROM DeliveryChallan dc WHERE dc.dcId IN :dcIds")
	List<DeliveryChallan> findByDcIdIn(@Param("dcIds") List<Integer> dcIds);

}