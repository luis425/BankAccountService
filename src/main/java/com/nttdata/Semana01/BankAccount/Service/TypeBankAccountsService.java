package com.nttdata.Semana01.BankAccount.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.BankAccount.Entity.TypeBankAccounts;
import com.nttdata.Semana01.BankAccount.Repository.TypeBankAccountsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class TypeBankAccountsService {

	@Autowired
	TypeBankAccountsRepository typeBankAccountsRepository;
	
	public Mono<TypeBankAccounts> createTypeBankAccounts(TypeBankAccounts customerType) {
		return typeBankAccountsRepository.save(customerType);
	}
	
	public Mono<TypeBankAccounts> getTypeBankAccountsbyId(Integer id) {
		return typeBankAccountsRepository.findById(id);
	}
	
	public Flux<TypeBankAccounts> getAllTypeBankAccounts() {
		return typeBankAccountsRepository.findAll();
	}

}