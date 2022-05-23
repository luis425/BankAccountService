package com.nttdata.Semana01.BankAccount.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Document
@Data
@Builder
public class TypeBankAccounts {

	@Id
	private Integer id;
	 
	private String description;
	
	// Manejo Por porcentaje 
	private Integer commission;
	
	// Limite maximo
	private Integer maximumLimit;
	
	
}
