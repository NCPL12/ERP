package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.WorkOrderItems;
@Repository
public interface WorkOrderItemsRepo extends JpaRepository<WorkOrderItems, Integer>{

}
