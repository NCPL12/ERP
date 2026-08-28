package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Party;

@Repository
public interface PartyRepo extends JpaRepository<Party, Long>{

	@Query( value = "SELECT * FROM  tbl_party WHERE id = :id",nativeQuery = true)
	public Party findById( @Param("id")String id);
	
	//Added ascending order
	@Query( value = "SELECT * FROM  tbl_party WHERE type_id IN (:types) ORDER BY party_name ASC",nativeQuery = true)
	public List<Party> findByTypeSupplier(@Param("types") List<Integer> types);
	
	
	@Query( value = "SELECT * FROM  tbl_party WHERE type_id IN (:types) and id IN (SELECT party_id FROM tbl_sales_order) ORDER BY party_name ASC",nativeQuery = true)
	public List<Party> findPartyWhereSOExist(@Param("types") List<Integer> types);
	
	@Query(value="SELECT distinct p.* FROM tbl_party p WHERE p.id IN (SELECT s.party_id FROM tbl_stock s where s.quantity>0)",nativeQuery = true)
	public List<Party> findPartyWhereStockExists();
	@Query( value = "SELECT * FROM  tbl_party WHERE BINARY  party_name = :partyName",nativeQuery = true)
	//@Query("SELECT p FROM Party p WHERE p.partyName = :partyName")
	public Party findByName(String partyName);
	
}
