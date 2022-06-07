package com.nttdata.Semana01.BankAccount.Entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat; 

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Document
@Data
@Builder
public class DebitCard {

	@Id
	private String id;
	 
	private String numberDebitCard;
	 
	//private double availableBalanceAccount;
	 
	@JsonFormat(pattern="dd-MM-yyyy" , timezone="GMT-05:00")
	private Date dateCreationDebitCard; 
	
	// Estado 
	private boolean statusAccount; 
	
	//private BankAccounts bankAccounts;
	private List<BankAccounts> bankAccounts;
	
	private String dniCustomer;
}
