package com.ncpl.sales.service;

import com.ncpl.sales.model.Country;
import com.ncpl.sales.repository.CountryRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CountryService {
    @Autowired
    CountryRepo countryRepo;
    
    public List<Country> countryList(){
        List<Country> countryList= countryRepo.findAll();
        return countryList;
    }
    
    public boolean saveCountry(Country country){
		countryRepo.save(country);
		return true;
	}
}