package com.ncpl.sales.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.Make;
import com.ncpl.sales.repository.MakeRepo;

@Service
public class MakeService {
	@Autowired
	MakeRepo makeRepo;
	
	 public Boolean saveMake(Make make){
	        makeRepo.save(make);
	        return true;
	    }
	
	 public List<Make> getMakeList(){
	        List<Make> makeList=makeRepo.findAll();
	        Collections.sort(makeList);
	        return makeList;
	    }
	 
	 public Optional<Make> findMakeById(Integer id) {
			Optional<Make> make = makeRepo.findById(id);
			return make;
		}
	 public void deleteMake(Integer id){
		 	makeRepo.deleteById(id);
	 }
	 
	 public boolean checkMakeNameExists(String name,Integer id) {
			boolean response;
			List<Make>  makeList;
			if(id.equals(0)) {
				makeList=makeRepo.checkMakeNameExist(name);
				
			}else {
				makeList=makeRepo.checkMakeNameExistForEdit(name, id);
			}
			if(makeList.size()>=1) {
				response = true;
			}
			else {
				response = false;
			}
			return response;
		}

	public Make getMakeByNaeme(String name) {
		Make makeObj=makeRepo.findMakeByName(name);
		return makeObj;
	}
	    
	    
	    
}
