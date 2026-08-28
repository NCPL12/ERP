package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.PartyContact;

@Repository
public interface PartyContactRepo extends JpaRepository<PartyContact, Integer>{
	@Query(" from PartyContact where party_id=?1 ")
	List<PartyContact> getContactByPartyId(String partyId);
	
	

}
