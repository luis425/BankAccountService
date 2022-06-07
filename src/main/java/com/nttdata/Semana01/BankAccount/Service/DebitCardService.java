package com.nttdata.Semana01.BankAccount.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Entity.DebitCard; 
import com.nttdata.Semana01.BankAccount.Repository.DebitCardRepository; 

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class DebitCardService {

	@Autowired
	DebitCardRepository debitCardRepository;
	
	public Mono<DebitCard> createDebitCard(DebitCard debitCard) {
		return debitCardRepository.save(debitCard);
	}
	
	public Mono<DebitCard> getDebitCardbyId(String id) {
		return debitCardRepository.findById(id);
	}
	
	public Flux<DebitCard> getAllDebitCard() {
		return debitCardRepository.findAll();
	}
	
	public Mono<DebitCard> deleteDebitCard(String id) {
			return debitCardRepository.findById(id).flatMap(existsDebitCard -> debitCardRepository
					.delete(existsDebitCard).then(Mono.just(existsDebitCard)));
	}
	
	public Flux<DebitCard> getAllBankAccountsByNumberDebitCard(String numberdebitcard) {
		return debitCardRepository.findAll().filter(x -> x.getNumberDebitCard().equals(numberdebitcard));
	}
	
}
