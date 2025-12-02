package com.udemy.ex.model;

import java.util.Date;

import lombok.Data;

@Data
public class Emplpyee {

	private Integer empNumber;

	private String empName;

	private String jobTitle;

	private Integer mgrNumber;

	private Date hireDate;

}
