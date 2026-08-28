package com.ncpl.sales.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.NonBillable;
import com.ncpl.sales.model.NonBillableItems;
import com.ncpl.sales.repository.NonBillableRepo;

@Service
public class NonBillableService {
	@Autowired
	NonBillableRepo nonBillableRepo;
	
	public List<NonBillable> getNonBillableList() {
		List<NonBillable> nonBillableList = nonBillableRepo.findAll();
		return nonBillableList;
	}

	public List<NonBillableItems> getNonBillableItemsByNonBillableId(int id) {
		Optional<NonBillable> nonBillableObj = nonBillableRepo.findById(id);
		List<NonBillableItems> list = nonBillableObj.get().getItems();
		return list;
	}

}
