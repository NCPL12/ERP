package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.ncpl.sales.model.City;
@Repository
public interface CityRepo extends JpaRepository<City, Integer>{
    @Query( value = "SELECT * FROM City where name=:name",nativeQuery = true)
	List<City> checkCityNameExist(String name);
	
	@Query(" from City where name=?1 and id!=?2")
	List<City> checkCityNameExistForEdit(String name, Integer cityId);
	/*
	 * @Query(value="SELECT count(*) FROM City", nativeQuery = true) int
	 * findAllCityCount();
	 * 
	 * @Query(value="SELECT * FROM City LIMIT ?,?", nativeQuery = true) Object
	 * findCityWithPage(int start, int length);
	 */
}
