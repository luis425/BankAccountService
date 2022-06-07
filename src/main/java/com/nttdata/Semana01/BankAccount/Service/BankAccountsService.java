package com.nttdata.Semana01.BankAccount.Service;

 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.nttdata.Semana01.BankAccount.DTO.Customer;
import com.nttdata.Semana01.BankAccount.DTO.Debt;
import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Repository.BankAccountsRepository;
import com.nttdata.Semana01.BankAccount.Repository.DebitCardRepository;
import com.nttdata.Semana01.BankAccount.config.CreditCardProperties;
import com.nttdata.Semana01.BankAccount.config.CustomerApiProperties;
import com.nttdata.Semana01.BankAccount.config.DebtApiProperties;
import com.nttdata.Semana01.BankAccount.response.BankAccountResponse;
import com.nttdata.Semana01.BankAccount.response.CreditCardResponse;
import com.nttdata.Semana01.BankAccount.response.CustomerResponse;
import com.nttdata.Semana01.BankAccount.response.DebitCardResponse;
import com.nttdata.Semana01.BankAccount.response.DebtResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono; 

@Slf4j
@RequiredArgsConstructor
@Service 
public class BankAccountsService {

	private WebClient customerServiceClient = WebClient.builder().baseUrl("http://localhost:8081").build();
	
	private WebClient debtServiceClient = WebClient.builder().baseUrl("http://localhost:8084").build();
	
	private final CustomerApiProperties customerApiProperties;
	
	private final CreditCardProperties creditCardProperties;
	
	private final DebtApiProperties debtApiProperties;
	
	@Autowired
	BankAccountsRepository bankAccountsRepository;
	
	@Autowired
	DebitCardRepository debitCardRepository;
	
	public Mono<BankAccounts> createBankAccountsRepository(BankAccounts bankAccounts) {
		return bankAccountsRepository.save(bankAccounts);
	}
	
	public Flux<BankAccounts> getAllBankAccountsByCodeCustomerAndIdTypeBankAccount(String codeCustomer, Integer typeBankAccounts) {
		return bankAccountsRepository.findAll().filter(x -> x.getCustomer().getCodeCustomer().equals(codeCustomer) && x.getTypeBankAccounts().getId().equals(typeBankAccounts));
	}
	
	public Flux<BankAccounts> getAllBankAccountsByNumberAccount(String numberAccount) {
		return bankAccountsRepository.findAll().filter(x -> x.getNumberAccount().equals(numberAccount));
	}
	
	public Flux<BankAccounts> getAllBankAccountsByCodeCustomer(String codeCustomer) {
		return bankAccountsRepository.findAll().filter(x -> x.getCustomer().getCodeCustomer().equals(codeCustomer));
	}
	
	public Flux<BankAccounts> getAllBankAccountsByDni(String dni) {
		return bankAccountsRepository.findAll().filter(x -> x.getCustomer().getDniCustomer().equals(dni));
	}
	
	/*
	 public List<CustomerResponse> comunicationWebClientCustomerObtenerCustomerbyDni(String dni) throws InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		List<CustomerResponse> result = new ArrayList<>();
		customerServiceClient.get()
				.uri("/customer/customerbydni/".concat(dni))
				.retrieve()
				.bodyToFlux(CustomerResponse.class)
				.publishOn(Schedulers.fromExecutor(executor))
				.subscribe(assuranceResponse -> result.add(assuranceResponse));
		executor.awaitTermination(3, TimeUnit.SECONDS);
		 
		return result;

	}*/
	
	@SuppressWarnings("unchecked")
	public CustomerResponse comunicationWebClientCustomerObtenerCustomerbyDniResponse(String dni) { 
		String uri = customerApiProperties.getBaseUrl() + "/customer/customerbydniResponse/".concat(dni);
		RestTemplate restTemplate = new RestTemplate();
		CustomerResponse result = restTemplate.getForObject(uri, CustomerResponse.class); 
		log.info("Ver lista --->" + result);
		return result;

	}
	
	public Mono<Debt> comunicationWebClientDebtObtenerCustomerbyDni(String dni) throws InterruptedException {

		Mono<Debt> debtServiceClientResponse = debtServiceClient.get()
				.uri("/debt/debtbyDniCustomer/".concat(dni)).accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Debt.class).log().doOnError(ex -> {
					throw new RuntimeException("the exception message is - " + ex.getMessage());
				});
		long temporizador = (5 * 1000);
		Thread.sleep(temporizador);
		
		return debtServiceClientResponse;

	}
	
	public Mono<Customer> comunicationWebClientCustomerObtenerCustomerbyDni(String dni) throws InterruptedException {

		Mono<Customer> debtServiceClientResponse = customerServiceClient.get()
				.uri("/customer/customerbydni/".concat(dni)).accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Customer.class).log().doOnError(ex -> {
					throw new RuntimeException("the exception message is - " + ex.getMessage());
				});
		long temporizador = (5 * 1000);
		Thread.sleep(temporizador);
		
		return debtServiceClientResponse;

	}
	
	@SuppressWarnings("unchecked")
	public DebtResponse comunicationWebClientDebtObtenerbyDni(String dni) { 
		String uri = debtApiProperties.getBaseUrl() + "/debt/debtbyDniCustomerResponse/".concat(dni);
		RestTemplate restTemplate = new RestTemplate();
		DebtResponse result = restTemplate.getForObject(uri, DebtResponse.class); 
		log.info("Ver lista --->" + result);
		return result;

	}
	
	public Flux<BankAccountResponse> getBankAccountbyNumberAccountResponse(String numberAccount) {
		return bankAccountsRepository.findAll().filter(x -> x.getNumberAccount().equals(numberAccount))
				.map(bankAccount -> BankAccountResponse.builder()
						.id(bankAccount.getId())
						.typeBankAccounts(bankAccount.getTypeBankAccounts())
						.numberAccount(bankAccount.getNumberAccount())
						.keyAccount(bankAccount.getKeyAccount())
						.availableBalanceAccount(bankAccount.getAvailableBalanceAccount())
						.dateLastBankAccount(bankAccount.getDateLastBankAccount())
						.statusAccount(bankAccount.isStatusAccount())
						.customer(bankAccount.getCustomer())
						.build());
	}
	
	public Flux<DebitCardResponse> getDebitCardbydniResponse(String numberDebitCard) {
		return debitCardRepository.findAll().filter(x -> x.getNumberDebitCard().equals(numberDebitCard))
				.map(debitcard -> DebitCardResponse.builder()
						.numberDebitCard(debitcard.getNumberDebitCard()) 
						.bankAccounts(debitcard.getBankAccounts().get(0))
						.build());
	}
	
	@SuppressWarnings("unchecked")
	public CreditCardResponse comunicationWebCreditCardbyNumberCreditCard(String numbercreditCard) { 
		String uri = creditCardProperties.getBaseUrl() + "/creditCard/creditCardbynumbercreditCardResponse/".concat(numbercreditCard);
		RestTemplate restTemplate = new RestTemplate();
		CreditCardResponse result = restTemplate.getForObject(uri, CreditCardResponse.class); 
		log.info("Ver lista --->" + result);
		return result;

	}
}
