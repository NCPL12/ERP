package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.InvoiceCopy;

@Repository
public interface InvoiceCopyRepo  extends JpaRepository<InvoiceCopy, String>{

	@Query("from InvoiceCopy where invId=?1")
	public InvoiceCopy getInvCopyByInvNo(String invNo);
	
}
