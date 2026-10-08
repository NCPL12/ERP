package com.ncpl.sales.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.ncpl.sales.model.PhotoId;
import com.ncpl.sales.repository.CategoryRepo;
import com.ncpl.sales.repository.PhotoIdRepo;

@Service
public class PhotoIdService {
	
	PhotoIdRepo photoIdRepo;
	
	/*
	 * public void addPhotoId(PhotoId photoId) {
	 * 
	 * photoIdRepo.save(photoId);
	 * 
	 * }
	 */
	
	
	@Autowired
	CategoryRepo categoryRepo;
	
	/* This is used to find the Category by its Id */
	
	public Optional<PhotoId>findPhotoIdById(int id) {
		
		Optional<PhotoId> photoId=photoIdRepo.findById(id);
		
		return photoId;
	}

	/* This is used to find the List of photoId in the user  */
	
	
	public List<PhotoId> getPhotoIdList(){
		
	
         
		   List<PhotoId> photoIdList= photoIdRepo.findAll();
		 
		   return photoIdList;
	}


}
