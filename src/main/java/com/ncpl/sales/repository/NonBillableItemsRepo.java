package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ncpl.sales.model.City;
import com.ncpl.sales.model.NonBillableItems;

public interface NonBillableItemsRepo extends JpaRepository<NonBillableItems, Integer>{

}
