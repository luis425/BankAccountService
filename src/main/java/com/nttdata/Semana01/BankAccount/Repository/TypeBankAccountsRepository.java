package com.nttdata.Semana01.BankAccount.Repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.nttdata.Semana01.BankAccount.Entity.TypeBankAccounts;

@Repository
public interface TypeBankAccountsRepository extends ReactiveCrudRepository<TypeBankAccounts, Integer>{
}