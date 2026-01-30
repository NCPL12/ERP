package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.ReturnableItems;
@Repository
public interface ReturnableItemsRepo extends JpaRepository<ReturnableItems, Integer>{

}
