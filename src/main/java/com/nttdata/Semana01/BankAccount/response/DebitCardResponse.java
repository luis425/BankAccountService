package com.nttdata.Semana01.BankAccount.response;
 

import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;

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
public class DebitCardResponse {

	private String numberDebitCard;
	
	private BankAccounts bankAccounts;
}
