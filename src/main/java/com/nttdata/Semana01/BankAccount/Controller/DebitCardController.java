package com.nttdata.Semana01.BankAccount.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Entity.DebitCard; 
import com.nttdata.Semana01.BankAccount.Service.BankAccountsService;
import com.nttdata.Semana01.BankAccount.Service.DebitCardService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/debitCard")
public class DebitCardController {

	@Autowired
	BankAccountsService bankAccountsService;
	
	@Autowired
	DebitCardService debitCardService;
	
	private String codigoValidatorBankAccount;
	
	@PostMapping
	public Mono<DebitCard> createDebitCard(@RequestBody DebitCard debitCard) {

		boolean validationvalue = this.validationRegisterDebitCardRequest(debitCard);

		if (validationvalue) {

			try {

				var bankAccount = this.bankAccountsService.getAllBankAccountsByNumberAccount(debitCard.getBankAccounts().getNumberAccount());

				List<BankAccounts> list1 = new ArrayList<>();

				bankAccount.collectList().subscribe(list1::addAll);

				long temporizador = (5 * 1000);
				Thread.sleep(temporizador);

				log.info("Obtener valor para validar Id --->" + list1);

				codigoValidatorBankAccount = this.validardorBankAccount(list1, debitCard);

				if (codigoValidatorBankAccount.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El BankAccount no existe, para realizar la relacion con la DebitCard"));
				} else {
					return this.debitCardService.createDebitCard(debitCard);
				}

			} catch (InterruptedException e) {
				log.info(e.toString());
				Thread.currentThread().interrupt();
				return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
			}

		} else {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		}

	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteDebitCardById(@PathVariable String id) {

		try {
			return this.debitCardService.deleteDebitCard(id).map(r -> ResponseEntity.ok().<Void>build())
					.defaultIfEmpty(ResponseEntity.notFound().build());

		} catch (Exception e) {
			log.info(e.toString());
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}

	}
	
	public boolean validationRegisterDebitCardRequest(DebitCard debitCard) {

		boolean validatorTypeBankAccounts;

		if (debitCard.getId() != null ) {
			validatorTypeBankAccounts = false;
		} else if (debitCard.getNumberDebitCard() == null || debitCard.getNumberDebitCard().equals("")) {
			validatorTypeBankAccounts = false;
		} else if (!debitCard.isStatusAccount()) {
			validatorTypeBankAccounts = false;
		} else if (debitCard.getBankAccounts() == null || debitCard.getBankAccounts().getNumberAccount().equals("")) {
			validatorTypeBankAccounts = false;
		}else { 
			validatorTypeBankAccounts = true;
		}

		return validatorTypeBankAccounts;
	}

	
	public String validardorBankAccount(List<BankAccounts> bankAccounts,DebitCard debitCard) {

		if (bankAccounts.isEmpty()) {
			codigoValidatorBankAccount = "";
		} else {
			codigoValidatorBankAccount = bankAccounts.get(0).getNumberAccount();
	
			// Setear Valor de Bank Account al Request  para el registro
			
			debitCard.setBankAccounts(bankAccounts.get(0));
			
		}

		return codigoValidatorBankAccount;
	}

}
