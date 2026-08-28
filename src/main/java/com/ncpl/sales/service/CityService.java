package com.ncpl.sales.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.City;
import com.ncpl.sales.repository.CityRepo;

@Service
public class CityService {
	@Autowired
	CityRepo cityRepo;
	

	public List<City> getCityList() {
		List<City> cityList = cityRepo.findAll();
		Collections.sort(cityList);
		return cityList;
	}

	public Optional<City> findCityById(int id) {
		Optional<City> city = cityRepo.findById(id);

		return city;
	}

	public boolean saveCity(City city) {
		cityRepo.save(city);
		return true;
	}

	public void deleteType(int id) {
		cityRepo.deleteById(id);
	}

	public boolean checkCityNameExists(String name,Integer cityId) {
		boolean response;
		List<City>  cityList;
		if(cityId.equals(0)) {
			cityList=cityRepo.checkCityNameExist(name);
			
		}else {
			cityList=cityRepo.checkCityNameExistForEdit(name, cityId);
		}
		if(cityList.size()>=1) {
			response = true;
		}
		else {
			response = false;
		}
		return response;
	}

	/*
	 * public Map<String, Object> getCityMapObject(int start,int length,int draw) {
	 * Map<String, Object> mapObj =new HashMap<>(); int
	 * totalCity=cityRepo.findAllCityCount();
	 * mapObj.put("data",cityRepo.findCityWithPage(start, length));
	 * mapObj.put("recordsFiltered",totalCity);
	 * mapObj.put("recordsTotal",totalCity); // Needed to show Pagination in
	 * Datatable mapObj.put("draw",draw); return mapObj; }
	 */
}
