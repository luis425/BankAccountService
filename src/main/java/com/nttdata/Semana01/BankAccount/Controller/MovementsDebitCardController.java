package com.nttdata.Semana01.BankAccount.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Entity.DebitCard;
import com.nttdata.Semana01.BankAccount.Entity.MovementsDebitCard;
import com.nttdata.Semana01.BankAccount.Service.BankAccountsService;
import com.nttdata.Semana01.BankAccount.Service.DebitCardService;
import com.nttdata.Semana01.BankAccount.Service.MovementsDebitCardService;
import com.nttdata.Semana01.BankAccount.util.Utils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/debitCardMovement")
public class MovementsDebitCardController {


	private String codigoValidatorMovementsBankAccounts;
	 
	@Autowired
	BankAccountsService bankAccountsService;
	
	@Autowired
	MovementsDebitCardService movementsDebitCardService;
	
	@Autowired
	DebitCardService debitCardService;
	
	
	@GetMapping
	public Mono<ResponseEntity<Flux<MovementsDebitCard>>> getAllMovementsDebitCard() {
		Flux<MovementsDebitCard> list = this.movementsDebitCardService.getAllMovementsDebitCard();
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list));
	}

	@GetMapping(value = "/MovementsDebitCard/{id}")
	public Mono<MovementsDebitCard> getMovementsDebitCardById(@PathVariable String id) {
		return this.movementsDebitCardService.getMovementsDebitCardbyId(id); 
	}
	
	@PostMapping(value = "/retreats")
	public Mono<MovementsDebitCard> createMovementsBankAccountsRetreats(@RequestBody MovementsDebitCard movementsdebitCard) {

		boolean validationvalue = this.validationRegisterRequest(movementsdebitCard); 

		if (validationvalue) {

			try {

				Flux<DebitCard> debitCard = this.debitCardService.getAllBankAccountsByNumberDebitCard(movementsdebitCard.getDebitCard().getNumberDebitCard());

				List<DebitCard> listDebitcard = new ArrayList<>();

				debitCard.collectList().subscribe(listDebitcard::addAll);
				
				Flux<BankAccounts> bankAccount = this.bankAccountsService.getAllBankAccountsByNumberAccount(movementsdebitCard.getDebitCard().getBankAccounts().get(0).getNumberAccount());

				List<BankAccounts> list1 = new ArrayList<>();

				bankAccount.collectList().subscribe(list1::addAll);

				long temporizador = (7 * 1000);

				Thread.sleep(temporizador);

				codigoValidatorMovementsBankAccounts = this.validardor(list1, movementsdebitCard);

				log.info("Verificar Numero de Cuenta -->" + codigoValidatorMovementsBankAccounts);
				
				if(listDebitcard.isEmpty()) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Debit Card no existe."));
				}

				if (codigoValidatorMovementsBankAccounts.equals("")) {

					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.numberAccountnoexiste));

				} else {

					if (list1.get(0).getKeyAccount() == movementsdebitCard.getDebitCard().getBankAccounts().get(0).getKeyAccount()) {

						// Bank Account 
						Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount() - movementsdebitCard.getAmount();

						list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

						BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1,movementsdebitCard.getDebitCard().getBankAccounts().get(0));
						
						this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();
						
						// Debit Card Monto
						
						//listDebitcard.get(0).getBankAccounts().get(0).setAvailableBalanceAccount(descuento);
						//listDebitcard.get(0).getBankAccounts().get(0).setStatusAccount(true);
						//this.debitCardService.createDebitCard(listDebitcard.get(0)).subscribe();
						
						// Registro del Movimiento 
						
						movementsdebitCard.setId(UUID.randomUUID().toString());

						return this.movementsDebitCardService.createMovementDebitCard(movementsdebitCard);
						
					} else {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.claveincorrecto));
					}

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
	
	public BankAccounts validationUpdateBankAccountsRequest(List<BankAccounts> list1, BankAccounts bankAccounts) {

		bankAccounts.setId(list1.get(0).getId());
		bankAccounts.setTypeBankAccounts(list1.get(0).getTypeBankAccounts());
		bankAccounts.setNumberAccount(list1.get(0).getNumberAccount());
		bankAccounts.setKeyAccount(list1.get(0).getKeyAccount());
		bankAccounts.setAvailableBalanceAccount(list1.get(0).getAvailableBalanceAccount());
		bankAccounts.setCustomer(list1.get(0).getCustomer());
		bankAccounts.setStatusAccount(true);
		bankAccounts.setDateCreationBankAccount(list1.get(0).getDateCreationBankAccount());
		bankAccounts.setDateLastBankAccount(list1.get(0).getDateLastBankAccount());
		bankAccounts.setCreditCard(list1.get(0).getCreditCard());

		return bankAccounts;
	}
	
	public boolean validationRegisterRequest(MovementsDebitCard movementsDebitCard) {

		boolean validator;

		if (movementsDebitCard.getAmount() == 0.00) {
			validator = false;
		}  else if (movementsDebitCard.getDescription() == null || movementsDebitCard.getDescription().equals("")) {
			validator = false;
		} else {
			
			if (movementsDebitCard.getDateMovement() == null) {
				movementsDebitCard.setDateMovement(new Date());
			}
			
			validator = true;
		}

		return validator;
	}
	
	public String validardor(List<BankAccounts> list1, MovementsDebitCard movementsDebitCard) {

		if (list1.isEmpty()) {
			codigoValidatorMovementsBankAccounts = "";
		} else {
			codigoValidatorMovementsBankAccounts = list1.get(list1.size() - 1).getNumberAccount();

			movementsDebitCard.getDebitCard().getBankAccounts().get(0).setId(list1.get(list1.size() - 1).getId());
			movementsDebitCard.getDebitCard().getBankAccounts().get(0).setTypeBankAccounts(list1.get(list1.size() - 1).getTypeBankAccounts());
			movementsDebitCard.getDebitCard().getBankAccounts().get(0).setNumberAccount(codigoValidatorMovementsBankAccounts);
			movementsDebitCard.getDebitCard().getBankAccounts().get(0).setKeyAccount(list1.get(list1.size() - 1).getKeyAccount());
			movementsDebitCard.getDebitCard().getBankAccounts().get(0).setAvailableBalanceAccount(list1.get(list1.size() - 1).getAvailableBalanceAccount());
			movementsDebitCard.getDebitCard().getBankAccounts().get(0).setCustomer(list1.get(list1.size() - 1).getCustomer());
			
		}

		return codigoValidatorMovementsBankAccounts;
	}
	
}
