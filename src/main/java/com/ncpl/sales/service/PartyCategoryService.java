package com.ncpl.sales.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.PartyCategory;
import com.ncpl.sales.repository.PartyCategoryRepo;

@Service
public class PartyCategoryService {

	@Autowired
	PartyCategoryRepo partyCategoryRepo;
	


	public void savepartyCategory(PartyCategory partyCategory) {
		// TODO Auto-generated method stub
		partyCategoryRepo.save(partyCategory);
	
	
	}

   public List<PartyCategory> getPartyCategorybyPartyId(String partyId){
		List<PartyCategory> partyCategoryList=partyCategoryRepo.getPartyCategorybyPartyId(partyId);
		return partyCategoryList;
	}

	public int getPartyCategoryCountbyPartyId(String partyId){
		return partyCategoryRepo.getPartyCategoryCountbyPartyId(partyId);
	}

	public void deleteById(int id) {
		// TODO Auto-generated method stub
		partyCategoryRepo.deleteById(id);
	}

}
