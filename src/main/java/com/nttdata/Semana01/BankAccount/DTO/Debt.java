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
public class Debt {
 
	private String id; 
	  
	// Monto De la Deuda
	private double debtBalance;
	 
	@JsonFormat(pattern="dd-MM-yyyy" , timezone="GMT-05:00")
	private Date dateRegister;
	
	@JsonFormat(pattern="dd-MM-yyyy" , timezone="GMT-05:00")
	private Date expirateDate;
	 
	// Estado 
	private boolean statusDebt; 
	
	private Customer customer;
	
}
