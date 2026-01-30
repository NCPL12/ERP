package com.ncpl.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ncpl.sales.model.EmployeeMaster;

public interface EmployeeRepo extends JpaRepository<EmployeeMaster, Integer>{

}
