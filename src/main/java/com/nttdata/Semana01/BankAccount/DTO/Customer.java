package com.nttdata.Semana01.BankAccount.DTO;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Customer {

	private String id;
	
	private String codeCustomer;
	
	private String nameCustomer;
	
	private String lastNameCustomer;
	
	private String directionCustomer;
	
	private String emailCustomer;
	
	private String phoneNumberCustomer;
	
	@JsonFormat(pattern="yyyy-MM-dd",shape=JsonFormat.Shape.STRING)
	private Date birthDateCustomer;
	
	@JsonFormat(pattern="dd-MM-yyyy" , timezone="GMT-05:00")
	private Date registerDateCustomer;
	
	private String dniCustomer;
	
	private CustomerType customertype;
	
	private Bank bank; 
	
}