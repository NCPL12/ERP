package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.TdsItems;

@Repository
public interface TdsItemRepo extends JpaRepository<TdsItems, Integer>{

}
