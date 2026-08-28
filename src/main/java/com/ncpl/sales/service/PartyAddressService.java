package com.ncpl.sales.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.City;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.repository.PartyAddressRepo;

@Service
public class PartyAddressService {

	@Autowired
	PartyAddressRepo partyAddressRepo;
	@Autowired
	CityService cityservice;

	public void savePartyAddress(PartyAddress partyContact) {
		// TODO Auto-generated method stub

		int cityId = partyContact.getCity();
		Optional<City> city = cityservice.findCityById(cityId);
		partyContact.setPartyaddr_city(city.get());
		partyAddressRepo.save(partyContact);
	}

	public List<PartyAddress> getAddressById(String partyId) {
		List<PartyAddress> addressList = partyAddressRepo.getAddressByPartyId(partyId);
		return addressList;
		
	}
	public List<PartyAddress> getAllPartyAddresses() {
		List<PartyAddress> addressList = partyAddressRepo.findAll();
		return addressList;
		
	}
	

	
	public void deletePartyAddress(String id) {
		partyAddressRepo.deleteById(id);
	}
	public int getTotalNoofAddress(String partyId){
		return partyAddressRepo.getNoofAddressByPartyId(partyId);
	}
	
	public Optional<PartyAddress> getAddressByAddressId(String id) {
		Optional<PartyAddress> pa = partyAddressRepo.findById(id);
		return pa;
	}

	
	//For updating party Address
	public void updatePartyAddress(PartyAddress partyAddress) {
		
	Optional<PartyAddress> paddressUpdate = partyAddressRepo.findById(partyAddress.getId());
	Date createdDate = paddressUpdate.get().getCreated();
	partyAddress.setCreated(createdDate);
	int cityId = partyAddress.getCity();
	Optional<City> city = cityservice.findCityById(cityId);
	partyAddress.setPartyaddr_city(city.get());
	//	PartyAddress paddressUpdate = partyAddressRepo.findById(partyAddress.getId());
		partyAddressRepo.save(partyAddress);
	}

}
