package com.nttdata.Semana01.BankAccount.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.BankAccount.DTO.Customer;
import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Entity.TypeBankAccounts;
import com.nttdata.Semana01.BankAccount.Service.BankAccountsService;
import com.nttdata.Semana01.BankAccount.Service.TypeBankAccountsService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/bankAccounts")
public class BankAccountsController {

	@Autowired
	TypeBankAccountsService typeBankAccountsService;

	@Autowired
	BankAccountsService bankAccountsService;

	private String codigoValidatorCustomer;

	private Integer codigoValidatorTypeBankAccounts;

	private String messageBadRequest;

	private WebClient customerServiceClient = WebClient.builder().baseUrl("http://localhost:8081").build();

	private static final String BANK_ACCOUNTS_CONTACT_TO_CUSTOMERSERVICE = "bankAccountsContactToCustomerService";
	
	@PostMapping
	@CircuitBreaker(name = BANK_ACCOUNTS_CONTACT_TO_CUSTOMERSERVICE, fallbackMethod = "bankAccountContacttoCustomer")
	public Mono<BankAccounts> create(@RequestBody BankAccounts bankAccounts) throws InterruptedException {

		boolean validationvalue = this.validationRegisterRequest(bankAccounts);

		if (validationvalue) {

			var typeBanksAccounts = this.typeBankAccountsService
					.getTypeBankAccountsbyId(bankAccounts.getTypeBankAccounts().getId());

			List<TypeBankAccounts> listtypeBanksAccounts = new ArrayList<>();

			typeBanksAccounts.flux().collectList().subscribe(listtypeBanksAccounts::addAll);

			Mono<Customer> endpointResponse = customerServiceClient.get()
					.uri("/customer/".concat(bankAccounts.getCustomer().getCodeCustomer()))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Customer.class).log()
					.doOnError(ex -> {
						throw new RuntimeException("the exception message is - " + ex.getMessage());
					});

			List<Customer> listCustomer = new ArrayList<>();

			endpointResponse.flux().collectList().subscribe(listCustomer::addAll);

			try {

				long temporizador = (10 * 1000);

				Thread.sleep(temporizador);

				codigoValidatorCustomer = this.validardorCustomer(listCustomer, bankAccounts);

				log.info("Validar Codigo Repetido --->" + codigoValidatorCustomer);

				codigoValidatorTypeBankAccounts = this.validardorTypeBankAccounts(listtypeBanksAccounts, bankAccounts);

				log.info("Obtener valor para validar Id --->" + codigoValidatorTypeBankAccounts);

				if (codigoValidatorCustomer.equals("")) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Codigo de Customer no existe"));
				}

				if (codigoValidatorTypeBankAccounts == 0) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Id de Tipo Cuenta Bancario no existe"));
				}

				// Validar dependiendo el Tipo de Cliente

				if (listCustomer.get(0).getCustomertype().getId().equals(1)) {

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

				} else {

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

	@GetMapping(value = "/{numberAccount}")
	public Mono<ResponseEntity<BankAccounts>> getBankAccountsByNumberAccount(@PathVariable String numberAccount) {

		try {

			Flux<BankAccounts> banksAccountsflux = this.bankAccountsService.getAllBankAccountsByNumberAccount(numberAccount);

			List<BankAccounts> list1 = new ArrayList<>();

			banksAccountsflux.collectList().subscribe(list1::addAll);

			long temporizador = (5 * 1000);

			Thread.sleep(temporizador);

			if (list1.isEmpty()) {
				return null;

			} else {
				return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list1.get(0)))
						.defaultIfEmpty(ResponseEntity.notFound().build());
			}

		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}

	}

	@GetMapping(value = "/BankAccountsbyCodeCustomer/{codeCustomer}")
	public Mono<ResponseEntity<Flux<BankAccounts>>> getBankAccountsByCodeCustomer(@PathVariable String codeCustomer) {
		Flux<BankAccounts> list = this.bankAccountsService.getAllBankAccountsByCodeCustomer(codeCustomer);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
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
		} else if (bankAccounts.getCustomer().getCodeCustomer() == null
				|| bankAccounts.getCustomer().getCodeCustomer().equals("")) {
			validatorbankAccounts = false;
			messageBadRequest = "customer.codeCustomer no puede ser vacio";
		} else {
			validatorbankAccounts = true;
		}

		return validatorbankAccounts;
	}

	public String validardorCustomer(List<Customer> list1, BankAccounts bankAccounts) {

		if (list1.isEmpty()) {
			codigoValidatorCustomer = "";
		} else {
			codigoValidatorCustomer = list1.get(0).getCodeCustomer();

			bankAccounts.getCustomer().setId(list1.get(0).getId());
			bankAccounts.getCustomer().setCodeCustomer(codigoValidatorCustomer);
			bankAccounts.getCustomer().setNameCustomer(list1.get(0).getNameCustomer());
			bankAccounts.getCustomer().setLastNameCustomer(list1.get(0).getLastNameCustomer());
			bankAccounts.getCustomer().setDirectionCustomer(list1.get(0).getDirectionCustomer());
			bankAccounts.getCustomer().setEmailCustomer(list1.get(0).getEmailCustomer());
			bankAccounts.getCustomer().setPhoneNumberCustomer(list1.get(0).getPhoneNumberCustomer());
			bankAccounts.getCustomer().setDniCustomer(list1.get(0).getDniCustomer());
			bankAccounts.getCustomer().setCustomertype(list1.get(0).getCustomertype());
			bankAccounts.getCustomer().setBank(list1.get(0).getBank());

		}

		return codigoValidatorCustomer;
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
