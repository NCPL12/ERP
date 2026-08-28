package com.ncpl.sales.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.PartyContact;
import com.ncpl.sales.repository.PartyContactRepo;



@Service
public class PartyContactService {
	
	
	@Autowired
	PartyContactRepo partyContactRepo;
	
	public void savePartyContacts(PartyContact partyContact) {
		
		partyContactRepo.save(partyContact);
		
	}
	
	/**
	 * get contact list by party id
	 * @param partyId
	 * @return contactList
	 */

	public List<PartyContact> getContactById(String partyId) {
		List<PartyContact> contactList = partyContactRepo.getContactByPartyId(partyId);
		return contactList;
	}

	public void deletePartyContactById(int id) {
		// TODO Auto-generated method stub
		partyContactRepo.deleteById(id);
	}

}
