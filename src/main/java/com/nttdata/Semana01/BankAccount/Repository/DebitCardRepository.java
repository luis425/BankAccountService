package com.nttdata.Semana01.BankAccount.Repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
 
import com.nttdata.Semana01.BankAccount.Entity.DebitCard;

@Repository
public interface DebitCardRepository extends ReactiveCrudRepository<DebitCard, String>{
}
