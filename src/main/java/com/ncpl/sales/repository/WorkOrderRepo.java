package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.WorkOrder;
@Repository
public interface WorkOrderRepo extends JpaRepository<WorkOrder, String>{
	@Query( value = "SELECT * FROM  tbl_work_order WHERE id IN (:id)",nativeQuery = true)
	List<WorkOrder> findWorkOrderListById(@Param("id") String woId);

}
