package com.th.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Employee {
	@Id
	@GeneratedValue
	private int employeeId;
	private String employeeName, designation, gender;
	private double salary;

}
