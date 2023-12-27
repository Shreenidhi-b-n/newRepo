package com.th.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.th.entity.Employee;

public interface EmployeesRepository extends JpaRepository<Employee,Integer> {
	
}
