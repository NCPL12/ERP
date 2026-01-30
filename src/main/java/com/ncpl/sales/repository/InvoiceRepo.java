package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.Invoice;

@Repository
public interface InvoiceRepo extends JpaRepository<Invoice,String>{
	@Query( value = "SELECT * FROM  tbl_invoice WHERE invoice_id IN (:id)",nativeQuery = true)
	List<Invoice> findInvoiceListById(@Param("id") String invoiceId);
	@Query( value = "SELECT * FROM  tbl_invoice WHERE so_number IN (:soId)",nativeQuery = true)
	List<Invoice> findInvoiceBySoId(@Param("soId") String soId);
	@Query(" from Invoice where dcNumber=?1 ")
	public List<Invoice> getItemByDcNo(String dcNumber);
	@Query(" from Invoice where  soNumber =?1 and dcNumber=?2")
	List<Invoice> findInvoiceBySoIdWhereDcNoIsAll(String soId,String dcNumber);
	

}
