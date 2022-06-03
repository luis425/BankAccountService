package com.nttdata.Semana01.BankAccount.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nttdata.Semana01.BankAccount.DTO.Customer;
import com.nttdata.Semana01.BankAccount.DTO.Debt;
import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Repository.BankAccountsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class BankAccountsService {

	private WebClient customerServiceClient = WebClient.builder().baseUrl("http://localhost:8081").build();
	
	private WebClient debtServiceClient = WebClient.builder().baseUrl("http://localhost:8084").build();
	
	@Autowired
	BankAccountsRepository bankAccountsRepository;
	
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
	
	public Mono<Customer> comunicationWebClientCustomerObtenerCustomerbyDni(String dni) throws InterruptedException {

		Mono<Customer> customerServiceClientResponse = customerServiceClient.get()
				.uri("/customer/customerbydni/".concat(dni)).accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(Customer.class).log().doOnError(ex -> {
					throw new RuntimeException("the exception message is - " + ex.getMessage());
				});
		long temporizador = (1 * 1000);
		Thread.sleep(temporizador);
		
		return customerServiceClientResponse;

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
	
}
