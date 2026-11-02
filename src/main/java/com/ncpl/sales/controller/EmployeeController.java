package com.ncpl.sales.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ncpl.sales.model.EmployeeMaster;
import com.ncpl.sales.service.EmployeeService;

/** Employee options used by the User Management name field. */
@RestController
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;

	@GetMapping("/api/employees/all")
	public List<EmployeeMaster> getAllEmployees() {
		return employeeService.getEmployeeList();
	}
}
