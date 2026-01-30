package com.ncpl.sales.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.ncpl.sales.repository.TypeRepo;
import com.ncpl.sales.model.Type;

@Service
public class TypeService {
    @Autowired
    private TypeRepo typeRepo; 
    public Boolean saveType(Type type){
        typeRepo.save(type);
        return true;
    }

    public List<Type> getTypeList(){
        List<Type> typeList=typeRepo.findAll();
        Collections.sort(typeList);
        return typeList;
    }
    public Optional<Type> findTypeById(Integer id) {
		Optional<Type> type = typeRepo.findById(id);
		return type;
	}
    public void deleteType(Integer id){
       typeRepo.deleteById(id);
    }

    /**public boolean checktypeExists(String name){
		List<Type> typeList=typeRepo.checkTypeNameExist(name);
		boolean response;
		if(typeList.size()>=1){
		   response=true;
		}
		else{
            response=false;
		}
		return response;
    }**/
    public boolean checkTypeNameExists(String name,Integer id) {
		boolean response;
		List<Type>  typeList;
		if(id.equals(0)) {
			typeList=typeRepo.checkTypeNameExist(name);
			
		}else {
			typeList=typeRepo.checkTypeNameExistForEdit(name, id);
		}
		if(typeList.size()>=1) {
			response = true;
		}
		else {
			response = false;
		}
		return response;
	}
}
