package com.ncpl.sales.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.ncpl.sales.model.PartyBank;
import com.ncpl.sales.repository.PartyBankRepo;



@Service
public class PartyBankService {

    @Autowired
    PartyBankRepo partyBankRepo;

    public void savePartyBank(PartyBank partyBank){
        partyBankRepo.save(partyBank);
    }
    public List<PartyBank> getPartyBankBypartyId(String partyId) {
		List<PartyBank> partyBankList = partyBankRepo.getPartyBankByPartyId(partyId);
		return partyBankList;
    }
    public void deletePartyBankDetails(Integer id) {
		partyBankRepo.deleteById(id);
    }
    public int getTotalNoofBankDetails(String partyId){
		return partyBankRepo.getNoofBankDetailsByPartyId(partyId);
	}
    

}