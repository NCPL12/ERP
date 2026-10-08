package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.City;
import com.ncpl.sales.model.Designation;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.PartyContact;
import com.ncpl.sales.model.Type;
import com.ncpl.sales.repository.PartyRepo;
@Service
public class PartyService {
	@Autowired
	PartyRepo partyRepo;
	@Autowired
	CityService cityservice;
	@Autowired
	CategoryService categoryservice;
	@Autowired
	PartyCategoryService partyCategoryService;
	@Autowired
	TypeService typeService;
	@Autowired
	DesignationService designationService;
	@Autowired
	PartyAddressService addressService;

	
	public Party saveParty(Party party) {
		
		//Setting party_type in database..
		Optional<Type> typeObj = typeService.findTypeById(Integer.parseInt(party.getType()));
		party.setParty_type(typeObj.get());
		party.setPartyName(party.getPartyName().trim());
		party.setAddr1(party.getAddr1().trim());
		party.setPhone1(party.getPhone1().trim());
		party.setPhone2(party.getPhone2().trim());
	
		List<PartyContact> contactList =party.getContacts();
		if( contactList!=null){
		for(PartyContact partyContact :contactList){
			int designationId=Integer.parseInt(partyContact.getDesignation());
		    Optional<Designation> designation = designationService.getDesignationById(designationId);
		    partyContact.setParty_contact_designation(designation.get());
		    partyContact.setParty(party);
		}}
		int cityId = party.getCity();
	   Optional<City> city = cityservice.findCityById(cityId);
	   party.setParty_city(city.get());  
		  Party newParty= partyRepo.save(party);
		   return newParty;
		
	}

	public List<Party> getPartyList() {
		List<Party> partyList=partyRepo.findAll();
		Collections.sort(partyList);
		
		return partyList;
	}

	// NEW: fast list for the Party List page (one query, only needed columns)
	public List<Map<String, Object>> getPartyListRows() {
		List<Object[]> rows = partyRepo.findPartyListRows();
		List<Map<String, Object>> result = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("id", r[0]);
			m.put("partyName", r[1]);
			m.put("phone1", r[2]);
			m.put("website", r[3]);
			m.put("remarks", r[4]);
			m.put("type", r[5]);
			m.put("city", r[6]);
			m.put("state", r[7]);
			m.put("country", r[8]);
			result.add(m);
		}
		return result;
	}
	
	public Party getPartyById(String id) {
		Party party = partyRepo.findById(id);
		party.setCity(party.getParty_city().getId());
		return party;
		
	}
	
	public List<Party> getPartyListbyTypeSupplier() {
		List<Integer> types=new ArrayList<Integer>();
        types.add(2);
		types.add(3);
		List<Party> partyList=partyRepo.findByTypeSupplier(types);
		return partyList;		
	}
	public List<Party> getPartyListByTypeCustomer() {
		List<Integer> types=new ArrayList<Integer>();
        types.add(1);
		types.add(3);
		List<Party> partyList=partyRepo.findByTypeSupplier(types);
		return partyList;		
	}
	public List<Party> getPartyListByTypeContractor() {
		List<Integer> types=new ArrayList<Integer>();
        types.add(2);
		types.add(6);
		List<Party> partyList=partyRepo.findByTypeSupplier(types);
		return partyList;		
	}
	public boolean checkDuplicatePartyName(String partyName) {
	boolean duplicateName = false;
		List<Party> partyList = getPartyList();
		for (Party party : partyList) {
			if(party.getPartyName().equalsIgnoreCase(partyName)){
				duplicateName =true;
			}
		}
		return duplicateName;
	}

	public Map<Object, Object> findPartById(String partyId) {
		Party party = partyRepo.findById(partyId);
		Map<Object, Object> addressMap = new HashMap<Object, Object>();
		
		if(party==null) {
			Optional<PartyAddress> partyaddr=addressService.getAddressByAddressId(partyId);
			partyId=partyaddr.get().getParty().getId();
			party= partyRepo.findById(partyId);
			addressMap.put("shippingAddr", partyaddr.get());
		}else {
			addressMap.put("shippingAddr", party);
		}
		addressMap.put("mainParty", party);
		
		List<PartyAddress> addressList = addressService.getAddressById(partyId);
		addressMap.put("addresses", addressList);
		return addressMap;
	}

	public List<Party> getPartyListByTypeCustomerWhereSoExist() {
		List<Integer> types=new ArrayList<Integer>();
        types.add(1);
		types.add(3);
		List<Party> partyList=partyRepo.findPartyWhereSOExist(types);
		System.out.println(partyList.size());
		return partyList;
	}
	
	public List<Party> getPartyListWhereStockExists() {
		List<Party> partyList=partyRepo.findPartyWhereStockExists();
		
		return partyList;
	}

	public Party getPartyByPartyName(String partyName) {
		Party party=partyRepo.findByName(partyName);
		return null;
	}
	

/**	public void updateParty(Party party) {
		// TODO Auto-generated method stub
		Party PartyUpdated = partyRepo.findById(party.getId());
		partyRepo.save(party);
	}**/



}