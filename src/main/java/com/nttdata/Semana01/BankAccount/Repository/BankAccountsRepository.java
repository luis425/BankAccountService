package com.nttdata.Semana01.BankAccount.Repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;

@Repository
public interface BankAccountsRepository extends ReactiveCrudRepository<BankAccounts, String>{
}
