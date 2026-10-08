package com.ncpl.sales.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.Category;

import com.ncpl.sales.repository.CategoryRepo;

@Service
public class CategoryService {
	
	@Autowired
	CategoryRepo categoryRepo;
	
	/* This is used to find the Category by its Id */
	
	public Optional<Category>findCategoryById(int id) {
		
		Optional<Category>category=categoryRepo.findById(id);
		
		return category;
	}

	/* This is used to find the List of Category in the party  */
	
	
	public List<Category> getCategoryList(){
		
	
         
		   List<Category> categoryList= categoryRepo.findAll();
		 
		   return categoryList;
	}

	public Boolean saveCategory(Category category){
		categoryRepo.save(category);
		return true;
	}
   
	public void deleteCategory(int id) {
		categoryRepo.deleteById(id);
	}

	public boolean checkCategoryNameExists(String name,Integer id) {
		boolean response;
		List<Category>  cityList;
		if(id.equals(0)) {
			cityList=categoryRepo.checkCategoryNameExist(name);
			
		}else {
			cityList=categoryRepo.checkCategoryNameExistForEdit(name, id);
		}
		if(cityList.size()>=1) {
			response = true;
		}
		else {
			response = false;
		}
		return response;
	}
}
