package com.ncpl.sales.repository;

import java.util.List;

import com.ncpl.sales.model.PartyBank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface PartyBankRepo extends JpaRepository<PartyBank, Integer> {
    @Query("from PartyBank where party_id=?1 ")
    List<PartyBank> getPartyBankByPartyId(String partyId);
    
    @Query("select count(*) from PartyBank where party_id=?1 ")
	int getNoofBankDetailsByPartyId(String partyId);
}