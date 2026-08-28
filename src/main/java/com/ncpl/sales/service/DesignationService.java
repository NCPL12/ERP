package com.ncpl.sales.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.Designation;
import com.ncpl.sales.repository.DesignationRepo;

@Service
public class DesignationService {
	@Autowired
	DesignationRepo designationRepo;
	/**
	 * add designation
	 * @param designation
	 * @return designationObj
	 */
	public Designation saveDesignation(Designation designation) {
		
		Designation designationObj = designationRepo.save(designation);
		return designationObj;
		
	}
	
	/**
	 * get list of designation
	 * @return designationList
	 */
	public List<Designation> getAllDesignation(){
		List<Designation> designationList = designationRepo.findAll();
		Collections.sort(designationList);
		return designationList;
	}
	/**
	 * delete designation by id
	 * @param id
	 */
	public void deleteDesignation(Integer id) {
		designationRepo.deleteById(id);
	}
	/**
	 * check for duplicate designation by name
	 * @param name
	 * @return response
	 */
	public boolean checkDesignationNameExists(String name,Integer designationId) {
		boolean response;
		List<Designation>  designationList;
		if(designationId.equals(0)) {
			designationList=designationRepo.checkDesignationNameExist(name);
			
		}else {
			designationList=designationRepo.checkDesignationNameExistForEdit(name,designationId);
		}
		if(designationList.size()>=1) {
			response = true;
		}
		else {
			response = false;
		}
		return response;
	}

	/**get designation by id**/
	public Optional<Designation> getDesignationById(Integer id){
		Optional<Designation> designation = designationRepo.findById(id);
		return designation;
	}

}
