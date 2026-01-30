package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Designation;

@Repository
public interface DesignationRepo extends JpaRepository<Designation,Integer> {
	
	@Query( value = "SELECT * FROM  designation where name=:name",nativeQuery = true)
	List<Designation> checkDesignationNameExist(String name);
	
	@Query(" from Designation where name=?1 and id!=?2")
	List<Designation> checkDesignationNameExistForEdit(String name, Integer designationId);

}
