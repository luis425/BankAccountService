package com.nttdata.Semana01.BankAccount.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.BankAccount.Entity.MovementsBankAccounts;
import com.nttdata.Semana01.BankAccount.Repository.MovementsBankAccountsRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class MovementsBankAccountsService {
	
	@Autowired
	MovementsBankAccountsRepository movementsBankAccountsRepository;
	
	public Mono<MovementsBankAccounts> createMovementsBankAccounts(MovementsBankAccounts bankAccounts) {
		return movementsBankAccountsRepository.save(bankAccounts);
	}
	
	public Flux<MovementsBankAccounts> getAllMovementsBankAccountsByNumberAccount(String numberAccount,Integer ultimaFechaEnviado) {
		return movementsBankAccountsRepository.findAll().filter(x -> x.getBankAccounts().getNumberAccount().equals(numberAccount)
				                                               && x.getDateMovement().getMonth()+1 == ultimaFechaEnviado);
	}
	
	public Flux<MovementsBankAccounts> getAllMovementsBankAccountsbyCodeCustomer(String codeCustomer) {
		return movementsBankAccountsRepository.findAll().filter(x -> x.getBankAccounts().getCustomer().getCodeCustomer().equals(codeCustomer));
	}
	
	public Flux<MovementsBankAccounts> getAllMovementsBankAccount() {
		return movementsBankAccountsRepository.findAll();
	}

	public Flux<MovementsBankAccounts> getMovementsBankAccountsbyNumberAccount(String numberAccount) {
		return movementsBankAccountsRepository.findAll().filter(x -> x.getBankAccounts().getNumberAccount().equals(numberAccount));
	}
	
}
