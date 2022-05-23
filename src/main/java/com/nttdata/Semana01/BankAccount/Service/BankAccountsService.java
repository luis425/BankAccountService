package com.nttdata.Semana01.BankAccount.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Repository.BankAccountsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class BankAccountsService {

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
	
}
