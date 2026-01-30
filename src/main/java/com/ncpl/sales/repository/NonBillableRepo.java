package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ncpl.sales.model.NonBillable;
@Repository
public interface NonBillableRepo extends JpaRepository<NonBillable, Integer>{

}
