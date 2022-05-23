package com.nttdata.Semana01.BankAccount.DTO;

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
public class Bank {
	
	private String id;
	 
	private String code;
	
	private String bankName;
	
	private String directionMain;
	
	
}
