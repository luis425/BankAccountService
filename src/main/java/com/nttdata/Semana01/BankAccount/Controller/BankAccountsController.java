package com.nttdata.Semana01.BankAccount.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

import com.nttdata.Semana01.BankAccount.DTO.Bank;
import com.nttdata.Semana01.BankAccount.DTO.Customer;
import com.nttdata.Semana01.BankAccount.DTO.CustomerType;
import com.nttdata.Semana01.BankAccount.DTO.Debt;
import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Entity.TypeBankAccounts;
import com.nttdata.Semana01.BankAccount.Repository.BankAccountsRepository;
import com.nttdata.Semana01.BankAccount.Service.BankAccountsService;
import com.nttdata.Semana01.BankAccount.Service.TypeBankAccountsService;
import com.nttdata.Semana01.BankAccount.api.client.CustomerApiClient;
import com.nttdata.Semana01.BankAccount.response.BankAccountResponse;
import com.nttdata.Semana01.BankAccount.response.CustomerResponse;
import com.nttdata.Semana01.BankAccount.response.DebtResponse;

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
	 
	@Autowired
	BankAccountsRepository bankAccountsRepository;

	private String codigoValidatorCustomer ="";
	
	private String codigoValidatorDebt = "";

	private Integer codigoValidatorTypeBankAccounts = 0;

	private String messageBadRequest;
	
	@Autowired
	CustomerApiClient customerApiClient; 

	private static final String BANK_ACCOUNTS_CONTACT_TO_CUSTOMERSERVICE = "bankAccountsContactToCustomerService";

	// Validar Servicio de Comunicacion con el Servicio de Customer

	@GetMapping(value = "/ObtenerCustomerbyDNI/{dni}")
	public Mono<Customer>  getCustomerByDNI(@PathVariable String dni) throws InterruptedException {

		try {

			Mono<Customer> fluxcustomer = this.bankAccountsService.comunicationWebClientCustomerObtenerCustomerbyDni(dni); 

			long temporizador = (5 * 1000);

			Thread.sleep(temporizador);
 
			return fluxcustomer;
			 
		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}
		 
	}
	
	@GetMapping(value = "/ObtenerDebtbyDNI/{dni}")
	public Mono<Debt> getDebtByDNI(@PathVariable String dni) throws InterruptedException {

		try {

			Mono<Debt> fluxdebt = this.bankAccountsService.comunicationWebClientDebtObtenerCustomerbyDni(dni); 

			long temporizador = (5 * 1000);

			Thread.sleep(temporizador);
 
			return fluxdebt;
			 
		} catch (InterruptedException e) {
			log.info(e.toString());
			Thread.currentThread().interrupt();
			return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
		}
	}

	@PostMapping
	@CircuitBreaker(name = BANK_ACCOUNTS_CONTACT_TO_CUSTOMERSERVICE, fallbackMethod = "bankAccountContacttoCustomer")
	public Mono<BankAccounts> create(@RequestBody BankAccounts bankAccounts)
			throws InterruptedException, ExecutionException, ParseException {

		boolean validationvalue = this.validationRegisterRequest(bankAccounts);

		if (validationvalue) {
 
		//Sin Mock 
			 	
		//Llamado con WebClient a Servicio de Customer y Debt para realizar las validaciones solicitadas 
			
			// Service para obtener Datos del Customer
			
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
			
			
			 
			/* ------------- Comentar sin Mock ------------------- */
			
			//listCustomer = this.comunicationWebClientObtenerCustomerMock();
			
			// Descomentar para validar el Mock del Service Debt
			
			//listDebt = this.comunicationWebClientObtenerDebtMock();
			 
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
								"El Cliente Personal solo puede tener  un m??ximo de una cuenta de ahorro, una cuenta corriente o cuentas a plazo fijo."));
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

			Flux<BankAccounts> banksAccountsflux = this.bankAccountsService
					.getAllBankAccountsByNumberAccount(numberAccount);

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

	@GetMapping(value = "/bankAccountbyNumberAccountResponse/{numberAccount}")
	public Mono<ResponseEntity<BankAccountResponse>> getbankAccountbyNumberAccountResponse(@PathVariable String numberAccount) {

		try {

			Flux<BankAccountResponse> customerflux = this.bankAccountsService.getBankAccountbyNumberAccountResponse(numberAccount);

			List<BankAccountResponse> list1 = new ArrayList<>();

			customerflux.collectList().subscribe(list1::addAll);

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
	 
	@PostMapping("/bankAccountReg")
	public Mono<BankAccounts> createYankiAccount(@RequestBody BankAccounts bankAccounts) { 
		 return this.bankAccountsRepository.save(bankAccounts);  
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
			//bankAccounts.getCustomer().setDirectionCustomer(list1.get(0).getDirectionCustomer());
			//bankAccounts.getCustomer().setEmailCustomer(list1.get(0).getEmailCustomer());
			//bankAccounts.getCustomer().setPhoneNumberCustomer(list1.get(0).getPhoneNumberCustomer());
			//bankAccounts.getCustomer().setBirthDateCustomer(list1.get(0).getBirthDateCustomer());
			//bankAccounts.getCustomer().setRegisterDateCustomer(list1.get(0).getRegisterDateCustomer());
			//bankAccounts.getCustomer().setDniCustomer(list1.get(0).getDniCustomer());
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
	

	@GetMapping(value = "/BankAccountResponse/{numberAccount}")
	public Flux<BankAccountResponse>  getBankAccountsByNumberAccountResponse(@PathVariable String numberAccount) {
		
		return bankAccountsService.getBankAccountbyNumberAccountResponse(numberAccount);
	
	}
	
	
	// Metodo para Mock

	public List<Customer> comunicationWebClientObtenerCustomerMock() throws ParseException {

		List<Customer> customers = new ArrayList<>();
		 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateBirth = sdf.parse("2001-03-01"); 
		
		customers.add(new Customer(
				"6288256a24f51675daabff60", 
				"CP1", 
				"PRUEBACLIENTEACTUALIZAR", 
				"APELLIDOCLIENTE4",
				"DIRECCIONCLIENTE4", 
				"EMAIL322@PRUEBA.COM",
				"2132132100", 
				dateBirth, 
				new Date(), 
				"213210011",
				new CustomerType(1, "Personal"), 
				new Bank("628570778f9e833491ad8ba4", "cb1", "PRUEBABANCOACTUALIZACION","PRUEBADIRECCIONACTUALIZACION")));

		log.info("Vista Customer con Dni Filtrado -->" + customers);

		return customers;
	}
	
	
	public List<Debt> comunicationWebClientObtenerDebtMock() throws ParseException {

		List<Debt> debt = new ArrayList<>();
		 
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateBirth = sdf.parse("2001-03-01"); 
		
		String dateregisterdebt="2022-05-31";    
		Date debtregister = sdf.parse(dateregisterdebt); 
		
		String expiradate="2022-07-01";    
		Date debtexpira = sdf.parse(expiradate); 
		
		debt.add(new Debt(
				"a386158d-f2f8-41bc-a932-91c5a11c5996",
				10,
				debtregister,
				debtexpira,
				true,
				new Customer(
						"6288256a24f51675daabff60", 
						"CP1", 
						"PRUEBACLIENTEACTUALIZAR", 
						"APELLIDOCLIENTE4",
						"DIRECCIONCLIENTE4", 
						"EMAIL322@PRUEBA.COM",
						"2132132100", 
						dateBirth, 
						new Date(), 
						"213210011",
						new CustomerType(1, "Personal"), 
						new Bank("628570778f9e833491ad8ba4", "cb1", "PRUEBABANCOACTUALIZACION","PRUEBADIRECCIONACTUALIZACION"))));

		log.info("Vista Debt con Dni Filtrado -->" + debt);

		return debt;
	}
	
		
}
