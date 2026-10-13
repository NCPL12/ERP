package com.ncpl.sales.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.EmployeeMaster;
import com.ncpl.sales.repository.EmployeeRepo;

@Service
public class EmployeeService {
	@Autowired
	EmployeeRepo employeeRepo;

	public List<EmployeeMaster> getEmployeeList(){
		List<EmployeeMaster> employeeList=employeeRepo.findAll();
		return employeeList;
	}
	
	public Optional<EmployeeMaster> getEmployeeById(int empId){
		Optional<EmployeeMaster> empObj=employeeRepo.findById(empId);
		return empObj;
	}
}
