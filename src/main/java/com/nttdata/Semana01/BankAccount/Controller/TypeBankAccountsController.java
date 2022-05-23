package com.nttdata.Semana01.BankAccount.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.BankAccount.Entity.TypeBankAccounts;
import com.nttdata.Semana01.BankAccount.Service.TypeBankAccountsService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/typeBankAccounts")
public class TypeBankAccountsController {

	@Autowired
	TypeBankAccountsService typeBankAccountsService;

	private Integer codigoValidator;

	// Registrar CustomerType

	@PostMapping
	public Mono<TypeBankAccounts> createTypeBankAccounts(@RequestBody TypeBankAccounts typeBankAccounts) {

		boolean validationvalue = this.validationRegisterTypeBankAccountsRequest(typeBankAccounts);

		if (validationvalue) {

			try {

				var typeBankAccount = this.typeBankAccountsService.getTypeBankAccountsbyId(typeBankAccounts.getId());

				List<TypeBankAccounts> list1 = new ArrayList<>();

				typeBankAccount.flux().collectList().subscribe(list1::addAll);

				long temporizador = (5 * 1000);
				Thread.sleep(temporizador);

				log.info("Obtener valor para validar Id --->" + list1);

				codigoValidator = this.validardor(list1);

				if (codigoValidator != 0 && codigoValidator.equals(typeBankAccounts.getId())) {
					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
							"El Id de Tipo Cuenta Bancario ya existe"));
				} else {
					return this.typeBankAccountsService.createTypeBankAccounts(typeBankAccounts);
				}

			} catch (InterruptedException e) {
				log.info(e.toString());
				Thread.currentThread().interrupt();
				return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
			}

		} else {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		}

	}

	@GetMapping
	public Mono<ResponseEntity<Flux<TypeBankAccounts>>> getAllCustomerType() {
		Flux<TypeBankAccounts> list = this.typeBankAccountsService.getAllTypeBankAccounts();
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list));
	}

	public Integer validardor(List<TypeBankAccounts> list1) {

		if (list1.isEmpty()) {
			codigoValidator = 0;
		} else {
			codigoValidator = list1.get(0).getId();
		}

		return codigoValidator;
	}

	public boolean validationRegisterTypeBankAccountsRequest(TypeBankAccounts typeBankAccounts) {

		boolean validatorTypeBankAccounts;

		if (typeBankAccounts.getId() == null || typeBankAccounts.getId() == 0) {
			validatorTypeBankAccounts = false;
		} else if (typeBankAccounts.getDescription() == null || typeBankAccounts.getDescription().equals("")) {
			validatorTypeBankAccounts = false;
		} else {

			if (typeBankAccounts.getCommission() == null) {
				typeBankAccounts.setCommission(0);
			}

			if (typeBankAccounts.getMaximumLimit() == null) {
				typeBankAccounts.setMaximumLimit(0);
			}

			validatorTypeBankAccounts = true;
		}

		return validatorTypeBankAccounts;
	}
}
