package com.ncpl.sales.service;

import java.util.List;
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
	
}
