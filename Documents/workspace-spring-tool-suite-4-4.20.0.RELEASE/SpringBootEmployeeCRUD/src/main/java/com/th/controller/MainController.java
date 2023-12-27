package com.th.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.th.service.EmployeeService;

import io.swagger.v3.oas.annotations.Parameter;

import com.th.entity.Employee;

@RestController
@RequestMapping("employeeapi")
public class MainController {
    
	@Autowired
	private EmployeeService employeeService;
	
	@GetMapping("employees")
	public List<Employee> getAllEmployees() {
		return employeeService.getAllEmployees();	
    }
	
	@PostMapping("employees")
	public Employee addNewEmployee(@RequestBody Employee employee) {
		return employeeService.addNewEmployee(employee);
	}
	
	@RequestMapping(value="/upload",method = RequestMethod.POST,consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<Object> uploadFile(
			@RequestParam("file") MultipartFile file) throws Exception {
		
		return employeeService.uploadFile(file);
	}
	
	@RequestMapping(value="/download",method = RequestMethod.GET)
	public ResponseEntity<Object> downloadFile(
			@Parameter(description = "Enter the downloadFileName")@RequestHeader("downloadFileName") String downloadFileName,
			@Parameter(description = "Enter the documentType")@RequestHeader("documentType") String type) throws Exception {
		return employeeService.downloadFile(downloadFileName, type);
	}
	
	@GetMapping("employee/{employeeId}")
	public ResponseEntity<Employee> getEmployeeById(@PathVariable("employeeId") int employeeId) {
		return employeeService.getEmployeeById(employeeId);
    }
	
	@PutMapping("/employee")
	public ResponseEntity<Employee> entireUpdateEmployee(@RequestBody Employee employee) {
		Employee existingEmployee = employeeService.getEmployeeById(employee.getEmployeeId()).getBody();
		if (existingEmployee==null)
			return ResponseEntity.notFound().build();

		existingEmployee=employee;
		return ResponseEntity.ok(employeeService.addNewEmployee(existingEmployee));
    }

	//@PatchMapping("employee/{employeeId}")
	

	@DeleteMapping("employee/{employeeId}")
	public String deleteEmployee(@PathVariable("employeeId") int employeeId) {

		return employeeService.deleteEmployee(employeeId);
    }
}