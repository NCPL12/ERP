package com.ncpl.sales.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.FileEntity;

@Repository
public interface FileRepo extends JpaRepository<FileEntity, Long>{
	
	@Query(" from FileEntity where sales_order_id=?1 ")
	Optional<FileEntity> findBySalesOrderId(String salesOrderId);

}
