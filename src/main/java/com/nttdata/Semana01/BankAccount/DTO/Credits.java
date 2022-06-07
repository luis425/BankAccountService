package com.nttdata.Semana01.BankAccount.DTO;

import java.util.Date;
 

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.response.CustomerResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor 
@Data
@Builder
public class Credits {
	
	private String id;

	private TypeCredits typeCredits;

	private String numberCredits;

	// Clave de cuenta - Deberia incriptarse
	private int keyCredit;

	// Monto Maximo del Credito
	private double availableBalanceCreditMaximum;

	// Monto Dispoible del Credito
	private double availableBalanceCredit;

	@JsonFormat(pattern = "dd-MM-yyyy", timezone = "GMT-05:00")
	private Date dateCreationCredit;

	// Estado
	private boolean statusAccount;

	private CustomerResponse customer;

	// Relacionar con Cuenta
	private boolean statusRelationAccount;

	// Dependiendo del Flag statusRelationAccount Validamos si se desea tener
	// relacion con una cuenta Bancaria
	private BankAccounts bankAccounts;

}