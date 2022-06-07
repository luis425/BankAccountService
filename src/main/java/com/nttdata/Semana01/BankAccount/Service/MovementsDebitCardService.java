package com.nttdata.Semana01.BankAccount.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.nttdata.Semana01.BankAccount.Entity.MovementsDebitCard;
import com.nttdata.Semana01.BankAccount.Entity.TypeBankAccounts;
import com.nttdata.Semana01.BankAccount.Repository.MovementsDebitCardRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service 
public class MovementsDebitCardService {

	@Autowired
	MovementsDebitCardRepository movementsDebitCardRepository;
	
	public Mono<MovementsDebitCard> createMovementDebitCard(MovementsDebitCard movementsDebitCard) {
		return movementsDebitCardRepository.save(movementsDebitCard);
	}
	
	public Mono<MovementsDebitCard> getMovementsDebitCardbyId(String id) {
		return movementsDebitCardRepository.findById(id);
	}
	
	public Flux<MovementsDebitCard> getAllMovementsDebitCard() {
		return movementsDebitCardRepository.findAll();
	}
	
}
