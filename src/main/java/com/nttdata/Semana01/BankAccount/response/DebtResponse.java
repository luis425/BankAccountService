package com.nttdata.Semana01.BankAccount.response;

import java.util.Date;
 

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtResponse {

	private String id; 
	
	private String dniCustomer;
	
	private double debtBalance;
	
	private Date expirateDate;
}
