package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.PartyCategory;

@Repository
public interface PartyCategoryRepo extends JpaRepository<PartyCategory, Integer>{
	
	@Query(" from PartyCategory where party_id=?1 ")
	List<PartyCategory> getPartyCategorybyPartyId(String partyId);

	@Query("select count(*) from PartyCategory where party_id=?1 ")
	int getPartyCategoryCountbyPartyId(String partyId);

}