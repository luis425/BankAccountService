package com.nttdata.Semana01.BankAccount.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat; 

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
public class CreditCardResponse {
	
	private String id; 

	private String numberCreditCard;

	// Clave de cuenta - Deberia incriptarse
	private int keyCredit;

	// Monto Maximo del Credito
	private double availableBalanceCreditCardMaximum;

	// Monto Dispoible del Credito
	private double availableBalanceCreditCard;

	@JsonFormat(pattern = "dd-MM-yyyy", timezone = "GMT-05:00")
	private Date dateCreationCreditCard;

	// Estado
	private boolean statusAccount;
 	
	//private Credits credits;
	
}

