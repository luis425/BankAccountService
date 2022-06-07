package com.nttdata.Semana01.BankAccount.Controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.BankAccount.DTO.Customer;
import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Entity.TypeBankAccounts;
import com.nttdata.Semana01.BankAccount.Service.BankAccountsService;
import com.nttdata.Semana01.BankAccount.Service.TypeBankAccountsService;
import com.nttdata.Semana01.BankAccount.api.client.CustomerApiClient;
import com.nttdata.Semana01.BankAccount.response.CreditCardResponse;
import com.nttdata.Semana01.BankAccount.response.CustomerResponse;
import com.nttdata.Semana01.BankAccount.response.DebtResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/bankAccountsNew")
public class BankAccountIIController {
	
	@Autowired
	TypeBankAccountsService typeBankAccountsService;

	@Autowired
	BankAccountsService bankAccountsService;

	private String codigoValidatorCustomer ="";
	
	private String codigoValidatorDebt = "";

	private Integer codigoValidatorTypeBankAccounts = 0;

	private String messageBadRequest;
	
	@Autowired
	CustomerApiClient customerApiClient; 
	
	private static final String BANK_ACCOUNTS_CONTACT_TO_CUSTOMERSERVICE = "bankAccountsContactToCustomerService";

	@PostMapping
	@CircuitBreaker(name = BANK_ACCOUNTS_CONTACT_TO_CUSTOMERSERVICE, fallbackMethod = "bankAccountContacttoCustomer")
	public Mono<BankAccounts> create(@RequestBody BankAccounts bankAccounts)
			throws InterruptedException, ExecutionException, ParseException {

		boolean validationvalue = this.validationRegisterRequest(bankAccounts);

		if (validationvalue) {
  		
			CustomerResponse  endpointResponseCustomer = this.bankAccountsService.comunicationWebClientCustomerObtenerCustomerbyDniResponse(bankAccounts.getCustomer().getDniCustomer());
			
			DebtResponse  endpointResponseDebt = this.bankAccountsService.comunicationWebClientDebtObtenerbyDni(bankAccounts.getCustomer().getDniCustomer());
			
			/* 
			
			---- LLamado con Webclient -----------
			
			List<CustomerResponse> listAccount = this.customerApiClient.getCustomertbyDNI(bankAccounts.getCustomer().getDniCustomer());
			
			//log.info("ver list -->" + listAccount);
			 
			//Mono<Debt> debtendpointResponse = this.bankAccountsService.comunicationWebClientDebtObtenerCustomerbyDni(bankAccounts.getCustomer().getDniCustomer());

			//debtendpointResponse.flux().collectList().subscribe(listDebt::addAll);
			
			*/
			
			var typeBanksAccounts = this.typeBankAccountsService.getTypeBankAccountsbyId(bankAccounts.getTypeBankAccounts().getId());

			List<TypeBankAccounts> listtypeBanksAccounts = new ArrayList<>();

			typeBanksAccounts.flux().collectList().subscribe(listtypeBanksAccounts::addAll); 
			
			
			try {
 				
				long temporizador1 = (5 * 1000);

				Thread.sleep(temporizador1);
				 	
				codigoValidatorCustomer = this.validardorCustomer(endpointResponseCustomer, bankAccounts);

				log.info("Validar Codigo Repetido --->" + codigoValidatorCustomer);

				codigoValidatorTypeBankAccounts = this.validardorTypeBankAccounts(listtypeBanksAccounts, bankAccounts);

				log.info("Obtener valor para validar Id --->" + codigoValidatorTypeBankAccounts);
				
				codigoValidatorDebt = this.validardorSielCustomertieneunaDebt(endpointResponseDebt);
				
				log.info("Obtener Debt para si el cliente tiene un Deuda --->" + codigoValidatorDebt);

				if (codigoValidatorCustomer.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Codigo de Customer no existe"));
				}

				if (codigoValidatorTypeBankAccounts == 0) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Id de Tipo Cuenta Bancario no existe"));
				}

				if (!codigoValidatorDebt.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Cliente que se desea Registrar, posee deuda"));
				}
				
				// Validar dependiendo el Tipo de Cliente

				if (endpointResponseCustomer.getCustomertype().getId().equals(1)) {

					// Personal

					Flux<BankAccounts> listFluxBankAccounts = this.bankAccountsService
							.getAllBankAccountsByCodeCustomerAndIdTypeBankAccount(
									bankAccounts.getCustomer().getCodeCustomer(),
									bankAccounts.getTypeBankAccounts().getId());

					List<BankAccounts> listBankAccounts = new ArrayList<>();

					listFluxBankAccounts.collectList().subscribe(listBankAccounts::addAll);

					long temporizador2 = (5 * 1000);

					Thread.sleep(temporizador2);

					log.info("Obtener valor para si hay registro --->" + listBankAccounts.toString());

					if (listBankAccounts.isEmpty()) {

						if (bankAccounts.getTypeBankAccounts().getId().equals(3)
								&& bankAccounts.getDateLastBankAccount() == null) {
							return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
									"Es necesario registrar el atributo dateLastBankAccount, cuando el Tipo de Cuenta es Plazo Fijo, con el formato yyyy-MM-ddT08:55:17.688+00:00"));
						} else {

							bankAccounts.setDateCreationBankAccount(new Date());
							return this.bankAccountsService.createBankAccountsRepository(bankAccounts);

						}

					} else {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"El Cliente Personal solo puede tener  un máximo de una cuenta de ahorro, una cuenta corriente o cuentas a plazo fijo."));
					}

				} else if (endpointResponseCustomer.getCustomertype().getId().equals(2)) { 

					// Empresarial

					if (bankAccounts.getTypeBankAccounts().getId().equals(1)) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"Un Cliente Empresarial no puede tener una Cuenta de Ahorro."));
					} else if (bankAccounts.getTypeBankAccounts().getId().equals(3)) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"Un Cliente Empresarial no puede tener una cuenta de Plazo Fijo."));
					} else {

						bankAccounts.setDateCreationBankAccount(new Date());
						return this.bankAccountsService.createBankAccountsRepository(bankAccounts);
					}

				} else if (endpointResponseCustomer.getCustomertype().getId().equals(3)) { 

					// VIP
					
					if (bankAccounts.getCreditCard() == null || bankAccounts.getCreditCard().getNumberCreditCard().equals("")) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"Para Creacion VIP, se requiere creditCard.numberCreditCard."));
					}
					
					CreditCardResponse endpointResponseCreditCard = this.bankAccountsService.comunicationWebCreditCardbyNumberCreditCard(bankAccounts.getCreditCard().getNumberCreditCard());
					
					if (endpointResponseCreditCard == null) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"No posee tarjeta de Credito al momento de la creacion de la cuenta VIP."));
					} else {

						bankAccounts.setDateCreationBankAccount(new Date());
						bankAccounts.setCreditCard(endpointResponseCreditCard);
						return this.bankAccountsService.createBankAccountsRepository(bankAccounts);
					}
					
				}else { 

					// PYME

					if (bankAccounts.getCreditCard() == null || bankAccounts.getCreditCard().getNumberCreditCard().equals("")) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"Para Creacion PYME, se requiere creditCard.numberCreditCard."));
					}
					
					
					CreditCardResponse endpointResponseCreditCard = this.bankAccountsService.comunicationWebCreditCardbyNumberCreditCard(bankAccounts.getCreditCard().getNumberCreditCard());
					
					if (endpointResponseCreditCard == null) {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"No posee tarjeta de Credito al momento de la creacion de la cuenta PYME."));
					} else {

						bankAccounts.setDateCreationBankAccount(new Date());
						bankAccounts.setCreditCard(endpointResponseCreditCard);
						return this.bankAccountsService.createBankAccountsRepository(bankAccounts);
					}

				}

			} catch (InterruptedException e) {
				log.info(e.toString());
				Thread.currentThread().interrupt();
				return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
			}

		} else {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, messageBadRequest));
		}

	}
	
	public boolean validationRegisterRequest(BankAccounts bankAccounts) {

		boolean validatorbankAccounts;

		if (bankAccounts.getId() != null) {
			validatorbankAccounts = false;
			messageBadRequest = "id es autogenerado, no se puede enviar en el Request";
		} else if (bankAccounts.getTypeBankAccounts().getId() == null
				|| bankAccounts.getTypeBankAccounts().getId() == 0) {
			validatorbankAccounts = false;
			messageBadRequest = "typeBankAccounts.Id no puede ser vacio";
		} else if (bankAccounts.getNumberAccount() == null || bankAccounts.getNumberAccount().equals("")) {
			validatorbankAccounts = false;
			messageBadRequest = "NumberAccount no puede ser vacio";
		} else if (bankAccounts.getKeyAccount() == 0) {
			validatorbankAccounts = false;
			messageBadRequest = "KeyAccount no puede ser vacio";
		} else if (bankAccounts.getAvailableBalanceAccount() == 0.00) {
			validatorbankAccounts = false;
			messageBadRequest = "availableBalanceAccount no puede ser vacio";
		} else if (bankAccounts.getCustomer().getDniCustomer() == null
				|| bankAccounts.getCustomer().getDniCustomer().equals("")) {
			validatorbankAccounts = false;
			messageBadRequest = "customer.dniCustomer no puede ser vacio";
		} else {
			validatorbankAccounts = true;
		}

		return validatorbankAccounts;
	}

	public String validardorCustomer(CustomerResponse  endpointResponseCustomer, BankAccounts bankAccounts) {

		if (endpointResponseCustomer == null) {
			codigoValidatorCustomer = "";
		} else {
			codigoValidatorCustomer = endpointResponseCustomer.getCodeCustomer();

			bankAccounts.getCustomer().setId(endpointResponseCustomer.getId());
			bankAccounts.getCustomer().setCodeCustomer(codigoValidatorCustomer);
			bankAccounts.getCustomer().setNameCustomer(endpointResponseCustomer.getNameCustomer());
			bankAccounts.getCustomer().setLastNameCustomer(endpointResponseCustomer.getLastNameCustomer()); 
			bankAccounts.getCustomer().setCustomertype(endpointResponseCustomer.getCustomertype());
			bankAccounts.getCustomer().setBank(endpointResponseCustomer.getBank());
			
			log.info(" Validar Lista para el Request BankAccount ---> " + endpointResponseCustomer);

		}

		return codigoValidatorCustomer;
	}
	
	public String validardorSielCustomertieneunaDebt(DebtResponse list1) {

		if (list1 == null) {
			codigoValidatorDebt = "";
		} else {
			codigoValidatorDebt = list1.getDniCustomer(); 

		}

		return codigoValidatorDebt;
	}
	

	public Integer validardorTypeBankAccounts(List<TypeBankAccounts> list1, BankAccounts bankAccounts) {

		if (list1.isEmpty()) {
			codigoValidatorTypeBankAccounts = 0;
		} else {
			codigoValidatorTypeBankAccounts = list1.get(0).getId();

			bankAccounts.getTypeBankAccounts().setId(codigoValidatorTypeBankAccounts);
			bankAccounts.getTypeBankAccounts().setDescription(list1.get(0).getDescription());
			bankAccounts.getTypeBankAccounts().setCommission(list1.get(0).getCommission());
			bankAccounts.getTypeBankAccounts().setMaximumLimit(list1.get(0).getMaximumLimit());
		}

		return codigoValidatorTypeBankAccounts;
	}

	public Mono<Customer> bankAccountContacttoCustomer(Throwable ex) {
		log.info("Message ---->" + ex.getMessage());
		Customer mockServiceResponse = null;
		return Mono.just(mockServiceResponse);
	}
	 
	
}
