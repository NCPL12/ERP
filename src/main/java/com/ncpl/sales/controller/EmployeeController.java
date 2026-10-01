package com.ncpl.sales.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ncpl.sales.model.EmployeeMaster;
import com.ncpl.sales.service.EmployeeService;

/** Employee master CRUD for the User Management "Employees" tab. */
@RestController
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;

	@GetMapping("/api/employees/all")
	@ResponseBody
	public List<EmployeeMaster> getAllEmployees() {
		return employeeService.getEmployeeList();
	}

	@PostMapping("/api/employees")
	@ResponseBody
	public ResponseEntity<?> createEmployee(
			@RequestParam("empId") String empId,
			@RequestParam("name") String name,
			@RequestParam(value = "contactNo", required = false) String contactNo) {

		for (EmployeeMaster e : employeeService.getEmployeeList()) {
			if (empId.equalsIgnoreCase(e.getEmpId())) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body("Employee ID '" + empId + "' already exists.");
			}
		}

		EmployeeMaster employee = new EmployeeMaster();
		employee.setEmpId(empId);
		employee.setName(name);
		employee.setContactNo(contactNo);
		return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.saveEmployee(employee));
	}

	@PutMapping("/api/employees/{id}")
	@ResponseBody
	public ResponseEntity<?> updateEmployee(
			@PathVariable("id") int id,
			@RequestParam("empId") String empId,
			@RequestParam("name") String name,
			@RequestParam(value = "contactNo", required = false) String contactNo) {

		Optional<EmployeeMaster> existing = employeeService.getEmployeeById(id);
		if (!existing.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found with ID: " + id);
		}

		for (EmployeeMaster e : employeeService.getEmployeeList()) {
			if (!e.getId().equals(id) && empId.equalsIgnoreCase(e.getEmpId())) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body("Employee ID '" + empId + "' already exists.");
			}
		}

		EmployeeMaster employee = existing.get();
		employee.setEmpId(empId);
		employee.setName(name);
		employee.setContactNo(contactNo);
		return ResponseEntity.ok(employeeService.saveEmployee(employee));
	}

	@DeleteMapping("/api/employees/{id}")
	@ResponseBody
	public ResponseEntity<?> deleteEmployee(@PathVariable("id") int id) {
		if (!employeeService.getEmployeeById(id).isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found with ID: " + id);
		}
		employeeService.deleteEmployee(id);
		return ResponseEntity.ok("Employee deleted successfully with ID: " + id);
	}
}
