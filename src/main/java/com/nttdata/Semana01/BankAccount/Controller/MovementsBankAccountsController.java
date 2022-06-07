package com.nttdata.Semana01.BankAccount.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nttdata.Semana01.BankAccount.Entity.BankAccounts;
import com.nttdata.Semana01.BankAccount.Entity.MovementsBankAccounts;
import com.nttdata.Semana01.BankAccount.Service.BankAccountsService;
import com.nttdata.Semana01.BankAccount.Service.MovementsBankAccountsService;
import com.nttdata.Semana01.BankAccount.util.Utils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/movementsBankAccounts")
public class MovementsBankAccountsController {

	@Autowired
	BankAccountsService bankAccountsService;

	@Autowired
	MovementsBankAccountsService movementsBankAccountsService;

	private String codigoValidatorMovementsBankAccounts;
 
	@PostMapping(value = "/retreats")
	public Mono<MovementsBankAccounts> createMovementsBankAccountsRetreats(
			@RequestBody MovementsBankAccounts movementsBankAccounts) {

		boolean validationvalue = this.validationRegisterRequest(movementsBankAccounts);

		if (validationvalue) {

			try {

				Flux<BankAccounts> bankAccount = this.bankAccountsService
						.getAllBankAccountsByNumberAccount(movementsBankAccounts.getBankAccounts().getNumberAccount());

				List<BankAccounts> list1 = new ArrayList<>();

				bankAccount.collectList().subscribe(list1::addAll);

				long temporizador = (7 * 1000);

				Thread.sleep(temporizador);

				codigoValidatorMovementsBankAccounts = this.validardor(list1, movementsBankAccounts);

				log.info("Verificar Numero de Cuenta -->" + codigoValidatorMovementsBankAccounts);

				if (codigoValidatorMovementsBankAccounts.equals("")) {

					return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.numberAccountnoexiste));

				} else {

					if (list1.get(0).getKeyAccount() == movementsBankAccounts.getBankAccounts().getKeyAccount()) {

						// Condicion dependiendo el tipo de Banco

						if (list1.get(0).getTypeBankAccounts().getId().equals(1)) {

							// Ahorro - Validar Maximos Retiros por Mes 

							Integer ultimaFechaEnviado = movementsBankAccounts.getDateMovement().getMonth() + 1; 

							Flux<MovementsBankAccounts> movementbankAccount = this.movementsBankAccountsService
							.getAllMovementsBankAccountsByNumberAccount(movementsBankAccounts.getBankAccounts().getNumberAccount(),ultimaFechaEnviado);

							List<MovementsBankAccounts> list2 = new ArrayList<>();

							movementbankAccount.collectList().subscribe(list2::addAll);

							long temporizador2 = (7 * 1000);

							Thread.sleep(temporizador2);

							log.info("Obtener Valor de Movimientos realizados de la numero de cuenta enviado --->" + list2);

							if (list2.isEmpty()) {

								if (movementsBankAccounts.getAmount() > list1.get(list1.size() - 1).getAvailableBalanceAccount()) {

									return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.superosaldo));

								} else {

									Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount() - movementsBankAccounts.getAmount();

									list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

									BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1,movementsBankAccounts.getBankAccounts());

									this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

									movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

									return this.movementsBankAccountsService.createMovementsBankAccounts(movementsBankAccounts);

								}

							} else {
								
								log.info("Obtener maximo Limite Tipo Ahorro --->" + list2.get(0).getBankAccounts().getTypeBankAccounts().getMaximumLimit());
								
								log.info("Tamaño de la lista Tipo Ahorro ---->" + list2.size());

								Integer ultimaFechaMesRegistrado = list2.get(list2.size() - 1).getDateMovement().getMonth() + 1;

								log.info("Ultioma Fecha Mes Obtenida Tipo Ahorro --->" + ultimaFechaMesRegistrado);

								log.info("Ultioma Fecha Mes Enviada Tipo Ahorro  --->" + ultimaFechaEnviado);

								if (list2.size() >= list2.get(list2.size() - 1).getBankAccounts().getTypeBankAccounts().getMaximumLimit()) {

									return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.superolimitsaldo));

								} else {

									Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount() - movementsBankAccounts.getAmount();

									list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

									BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1,movementsBankAccounts.getBankAccounts());

									this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

									movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

									return this.movementsBankAccountsService.createMovementsBankAccounts(movementsBankAccounts);

								}

							}

						} else if (list1.get(0).getTypeBankAccounts().getId().equals(2)) {

							// Corriente

							if (movementsBankAccounts.getAmount() > list1.get(list1.size() - 1).getAvailableBalanceAccount()) {

								return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.superosaldo));

							} else {

								Double totaldescuento = movementsBankAccounts.getAmount() + movementsBankAccounts.getAmount() / list1.get(list1.size() - 1).getTypeBankAccounts().getCommission();

								log.info("Total Descuento Corriente" + totaldescuento);

								Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount() - totaldescuento;

								list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

								BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1,movementsBankAccounts.getBankAccounts());

								this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

								movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

								// Seteo de amount enviado mas la comision calculada
								movementsBankAccounts.setAmount(totaldescuento);
								
								return this.movementsBankAccountsService.createMovementsBankAccounts(movementsBankAccounts);

							}

						} else {

							// Plazo Fijo

							if (movementsBankAccounts.getAmount() > list1.get(list1.size() - 1).getAvailableBalanceAccount()) {

								return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.superosaldo));

							} else {

								DateFormat dateFormat = new SimpleDateFormat(Utils.formatoDate);
								 
								String strDateEstimado = dateFormat.format(list1.get(list1.size() - 1).getDateLastBankAccount());
									
								log.info("Fecha estimanda para el retiro Fijo " + strDateEstimado);

								String strDateActual = dateFormat.format(new Date());

								log.info("Fecha Actual para retiro Fijo " + strDateActual);

								if (strDateEstimado.equals(strDateActual)) {

									Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount() - movementsBankAccounts.getAmount();

									list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

									BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1,movementsBankAccounts.getBankAccounts());

									this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

									movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

									return this.movementsBankAccountsService.createMovementsBankAccounts(movementsBankAccounts);

								} else {
									return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.fechanoPermitido));
								}

							}

						}

					} else {
						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.claveincorrecto));
					}

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

	@PostMapping(value = "/deposits")
	public Mono<MovementsBankAccounts> createMovementsBankAccountsDeposists(@RequestBody MovementsBankAccounts movementsBankAccounts) {

		boolean validationvalue = this.validationRegisterRequest(movementsBankAccounts);

		if (validationvalue) {

			try {

				Flux<BankAccounts> bankAccount = this.bankAccountsService.getAllBankAccountsByNumberAccount(movementsBankAccounts.getBankAccounts().getNumberAccount());

				List<BankAccounts> list1Destination = new ArrayList<>();

				if (movementsBankAccounts.getNumberAccountDestination() != null) {

					Flux<BankAccounts> bankAccountDestination = this.bankAccountsService.getAllBankAccountsByNumberAccount(movementsBankAccounts.getNumberAccountDestination());

					bankAccountDestination.collectList().subscribe(list1Destination::addAll);

				}

				List<BankAccounts> list1 = new ArrayList<>();

				bankAccount.collectList().subscribe(list1::addAll);

				long temporizador = (7 * 1000);

				Thread.sleep(temporizador);

				codigoValidatorMovementsBankAccounts = this.validardor(list1, movementsBankAccounts);

				log.info("Verificar lista de Banco -->" + codigoValidatorMovementsBankAccounts);

				if (movementsBankAccounts.getNumberAccountDestination() != null) {

					// Depostitar a Numero Destinatario

					if (codigoValidatorMovementsBankAccounts.equals("")) {

						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.numberAccountnoexiste));

					} else {

						if (list1Destination.isEmpty()) {
							return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.numberAccountdestinatarionoexiste));
						} else {

							if (list1.get(0).getKeyAccount() == movementsBankAccounts.getBankAccounts().getKeyAccount()) {

								BankAccounts bankAccountsDestination = new BankAccounts();

								// Condicion dependiendo el tipo de Banco

								if (list1.get(0).getTypeBankAccounts().getId().equals(1)) {

									// Ahorro por mes 

									Integer ultimaFechaEnviado = movementsBankAccounts.getDateMovement().getMonth() + 1;

									Flux<MovementsBankAccounts> movementbankAccount = this.movementsBankAccountsService.getAllMovementsBankAccountsByNumberAccount(
													movementsBankAccounts.getBankAccounts().getNumberAccount(),
													ultimaFechaEnviado);

									List<MovementsBankAccounts> list2 = new ArrayList<>();

									movementbankAccount.collectList().subscribe(list2::addAll);

									long temporizador2 = (5 * 1000);

									Thread.sleep(temporizador2);

									log.info("Obtener Valor de Movimientos realizados de la numero de cuenta enviado --->"+ list2);

									if (list2.isEmpty()) {

										if (movementsBankAccounts.getAmount() > list1.get(list1.size() - 1)
												.getAvailableBalanceAccount()) {

											return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.depositincomplete));

										} else {
											

											Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount()
													- movementsBankAccounts.getAmount();

											list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

											BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(
													list1, movementsBankAccounts.getBankAccounts());

											this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

											Double depositodestination = list1Destination.get(list1Destination.size() - 1).getAvailableBalanceAccount() + movementsBankAccounts.getAmount();

											list1Destination.get(list1Destination.size() - 1)
													.setAvailableBalanceAccount(depositodestination);

											BankAccounts updateBankAccountsDestination = this
													.validationUpdateBankAccountsDestinationRequest(list1Destination,
															bankAccountsDestination);

											this.bankAccountsService
													.createBankAccountsRepository(updateBankAccountsDestination).subscribe();

											movementsBankAccounts
													.setMovementsBankAccountsCode(UUID.randomUUID().toString());

											return this.movementsBankAccountsService
													.createMovementsBankAccounts(movementsBankAccounts);

										}

									} else {

										log.info("Obtener maximo Limite --->" + list2.get(0).getBankAccounts().getTypeBankAccounts().getMaximumLimit());
										
										log.info("Tamaño de la lista --->" + list2.size());

										Integer ultimaFechaMesRegistrado = list2.get(list2.size() - 1).getDateMovement().getMonth() + 1;

										log.info("Ultioma Fecha Mes Obtenida--->" + ultimaFechaMesRegistrado);

										log.info("Ultioma Fecha Mes Enviada --->" + ultimaFechaEnviado);

										if (list2.size() >= list2.get(list2.size() - 1).getBankAccounts()
												.getTypeBankAccounts().getMaximumLimit()) {

											return Mono
													.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
															"Supero el Limite De movimientos Mensual"));

										} else {
											
											
											if (movementsBankAccounts.getAmount() > list1.get(list1.size() - 1)
													.getAvailableBalanceAccount()) {
												
												return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
														"La cuenta que envia el deposito, no dispone del saldo suficiente del deposito que desea realizar."));
											}	
											
											else {
													 
											Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount()
													- movementsBankAccounts.getAmount();

											list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

											BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(
													list1, movementsBankAccounts.getBankAccounts());

											this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

											Double depositodestination = list1Destination
													.get(list1Destination.size() - 1).getAvailableBalanceAccount()
													+ movementsBankAccounts.getAmount();

											list1Destination.get(list1Destination.size() - 1)
													.setAvailableBalanceAccount(depositodestination);

											BankAccounts updateBankAccountsDestination = this
													.validationUpdateBankAccountsDestinationRequest(list1Destination,
															bankAccountsDestination);

											this.bankAccountsService
													.createBankAccountsRepository(updateBankAccountsDestination).subscribe();

											movementsBankAccounts
													.setMovementsBankAccountsCode(UUID.randomUUID().toString());

											return this.movementsBankAccountsService
													.createMovementsBankAccounts(movementsBankAccounts);
											
										  }	
											
										}

									}

								}

								else if (list1.get(0).getTypeBankAccounts().getId().equals(2)) {

									// Corriente

									if (movementsBankAccounts.getAmount() > list1.get(list1.size() - 1)
											.getAvailableBalanceAccount()) {

										return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
												"La cuenta que envia el deposito, no dispone del saldo suficiente del deposito que desea realizar."));

									} else {

										Double comision = movementsBankAccounts.getAmount()
												+ movementsBankAccounts.getAmount() / list1.get(list1.size() - 1)
														.getTypeBankAccounts().getCommission();

										log.info("Total Descuento " + comision);

										Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount()
												- comision;

										list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

										BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(
												list1, movementsBankAccounts.getBankAccounts());

										this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

										Double depositodestination = list1Destination.get(list1Destination.size() - 1)
												.getAvailableBalanceAccount() + movementsBankAccounts.getAmount();
										
										list1Destination.get(list1Destination.size() - 1)
												.setAvailableBalanceAccount(depositodestination);
										
										log.info("Deposito Destinatorio " + movementsBankAccounts.getAmount());

										BankAccounts updateBankAccountsDestination = this
												.validationUpdateBankAccountsDestinationRequest(list1Destination,
														bankAccountsDestination);

										this.bankAccountsService
												.createBankAccountsRepository(updateBankAccountsDestination).subscribe();

										movementsBankAccounts
												.setMovementsBankAccountsCode(UUID.randomUUID().toString());

										return this.movementsBankAccountsService
												.createMovementsBankAccounts(movementsBankAccounts);

									}

								} else {

									DateFormat dateFormat = new SimpleDateFormat(Utils.formatoDate);
									String strDateEstimado = dateFormat.format(list1.get(list1.size() - 1).getDateLastBankAccount());

									log.info("Fecha estimanda para el retiro " + strDateEstimado);

									String strDateActual = dateFormat.format(new Date());

									log.info("Fecha Actual " + strDateActual);

									if (strDateEstimado.equals(strDateActual)) {

										Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount()
												- movementsBankAccounts.getAmount();

										list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

										BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(
												list1, movementsBankAccounts.getBankAccounts());

										this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

										Double depositodestination = list1Destination.get(list1Destination.size() - 1)
												.getAvailableBalanceAccount() + movementsBankAccounts.getAmount();

										list1Destination.get(list1Destination.size() - 1)
												.setAvailableBalanceAccount(depositodestination);

										BankAccounts updateBankAccountsDestination = this
												.validationUpdateBankAccountsDestinationRequest(list1Destination,
														bankAccountsDestination);

										this.bankAccountsService
												.createBankAccountsRepository(updateBankAccountsDestination).subscribe();

										movementsBankAccounts
												.setMovementsBankAccountsCode(UUID.randomUUID().toString());

										return this.movementsBankAccountsService
												.createMovementsBankAccounts(movementsBankAccounts);

									} else {
										return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
												"Fecha no permitido, para retirar dinero."));
									}

								}

							} else {
								return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
										"Clave de Retiro es incorrecto"));
							}

						}

					}

				} else {

					// Depositar a la Numero de Cuenta dependiendo al number Account

					if (codigoValidatorMovementsBankAccounts.equals("")) {

						return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
								"El Numero de Cuenta no existe"));

					} else {

						if (list1.get(0).getKeyAccount() == movementsBankAccounts.getBankAccounts().getKeyAccount()) {

							if (list1.get(0).getTypeBankAccounts().getId().equals(1)) {

								// Ahorro

								Integer ultimaFechaEnviado = movementsBankAccounts.getDateMovement().getMonth() + 1;

								Flux<MovementsBankAccounts> movementbankAccount = this.movementsBankAccountsService
										.getAllMovementsBankAccountsByNumberAccount(
												movementsBankAccounts.getBankAccounts().getNumberAccount(),
												ultimaFechaEnviado);

								List<MovementsBankAccounts> list2 = new ArrayList<>();

								movementbankAccount.collectList().subscribe(list2::addAll);

								long temporizador5 = (5 * 1000);

								Thread.sleep(temporizador5);

								log.info("Obtener Valor de Movimientos realizados de la numero de cuenta enviado ---> " + list2);

								if (list2.isEmpty()) {

										Double descuento = list1.get(list1.size() - 1).getAvailableBalanceAccount()
												+ movementsBankAccounts.getAmount();

										list1.get(list1.size() - 1).setAvailableBalanceAccount(descuento);

										BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(
												list1, movementsBankAccounts.getBankAccounts());

										this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

										movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

										return this.movementsBankAccountsService.createMovementsBankAccounts(movementsBankAccounts);

								} else {

									log.info("Obtener maximo Limite --->"+ list2.get(0).getBankAccounts().getTypeBankAccounts().getMaximumLimit());
									log.info("Tamaño de la lista --->" + list2.size());

									Integer ultimaFechaMesRegistrado = list2.get(list2.size() - 1).getDateMovement().getMonth() + 1;

									log.info("Ultioma Fecha Mes Obtenida--->" + ultimaFechaMesRegistrado);

									log.info("Ultioma Fecha Mes Enviada --->" + ultimaFechaEnviado);

									if (list2.size() >= list2.get(list2.size() - 1).getBankAccounts().getTypeBankAccounts().getMaximumLimit()) {

										return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,Utils.superolimitsaldo));

									} else {

										Double deposito = list1.get(list1.size() - 1).getAvailableBalanceAccount()+ movementsBankAccounts.getAmount();

										list1.get(list1.size() - 1).setAvailableBalanceAccount(deposito);

										BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1, movementsBankAccounts.getBankAccounts());

										this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

										movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

										return this.movementsBankAccountsService.createMovementsBankAccounts(movementsBankAccounts);

									}

								}

							} else if (list1.get(0).getTypeBankAccounts().getId().equals(2)) {

								// Corriente

									Double depositodescunto = movementsBankAccounts.getAmount()
											/ list1.get(list1.size() - 1).getTypeBankAccounts().getCommission();

									log.info("Total Descuento " + depositodescunto);
									
									
									Double depositototal = list1.get(list1.size() - 1).getAvailableBalanceAccount()
											+ movementsBankAccounts.getAmount() - depositodescunto;

									list1.get(list1.size() - 1).setAvailableBalanceAccount(depositototal);

									BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1,
											movementsBankAccounts.getBankAccounts());

									this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

									movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

									// Seteo de amount enviado mas la comision calculada
									movementsBankAccounts.setAmount(movementsBankAccounts.getAmount() - depositodescunto);
									
									return this.movementsBankAccountsService.createMovementsBankAccounts(movementsBankAccounts); 
									
							} else {

								DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
								String strDateEstimado = dateFormat
										.format(list1.get(list1.size() - 1).getDateLastBankAccount());

								log.info("Fecha estimanda para el depositar " + strDateEstimado);

								String strDateActual = dateFormat.format(new Date());

								log.info("Fecha Actual " + strDateActual);

								if (strDateEstimado.equals(strDateActual)) {

									Double deposito = list1.get(list1.size() - 1).getAvailableBalanceAccount()+ movementsBankAccounts.getAmount();

									list1.get(list1.size() - 1).setAvailableBalanceAccount(deposito);

									BankAccounts updateBankAccounts = this.validationUpdateBankAccountsRequest(list1,
											movementsBankAccounts.getBankAccounts());

									this.bankAccountsService.createBankAccountsRepository(updateBankAccounts).subscribe();

									movementsBankAccounts.setMovementsBankAccountsCode(UUID.randomUUID().toString());

									return this.movementsBankAccountsService
											.createMovementsBankAccounts(movementsBankAccounts);

								} else {
									return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
											"Fecha no permitido, para depositar el dinero."));
								}

							}

						} else {
							return Mono.error(new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
									"Clave de Retiro es incorrecto"));
						}

					}

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

	@GetMapping(value = "/MovementsBankAccountsbyCodeCustomer/{codeCustomer}")
	public Mono<ResponseEntity<Flux<MovementsBankAccounts>>> getMovementsBankAccountsbyCodeCustomer(
			@PathVariable String codeCustomer) {
		Flux<MovementsBankAccounts> list = this.movementsBankAccountsService
				.getAllMovementsBankAccountsbyCodeCustomer(codeCustomer);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping
	public Mono<ResponseEntity<Flux<MovementsBankAccounts>>> getAllMovementsBankAccount() {
		Flux<MovementsBankAccounts> list = this.movementsBankAccountsService.getAllMovementsBankAccount();
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list));
	}

	@GetMapping(value = "/MovementsBankAccountsbyNumberAccount/{numberAccount}")
	public Mono<ResponseEntity<Flux<MovementsBankAccounts>>> getMovementsBankAccountsbyNumberAccount(
			@PathVariable String numberAccount) {
		Flux<MovementsBankAccounts> list = this.movementsBankAccountsService
				.getMovementsBankAccountsbyNumberAccount(numberAccount);
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(list))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	public boolean validationRegisterRequest(MovementsBankAccounts movementsBankAccounts) {

		boolean validator;

		if (movementsBankAccounts.getBankAccounts().getNumberAccount() == null
				|| movementsBankAccounts.getBankAccounts().getNumberAccount().equals("")) {
			validator = false;
		} else if (movementsBankAccounts.getAmount() == 0.00) {
			validator = false;
		} else if (movementsBankAccounts.getBankAccounts().getKeyAccount() == 0) {
			validator = false;
		} else if (movementsBankAccounts.getDescription() == null || movementsBankAccounts.getDescription().equals("")) {
			validator = false;
		} else {
			
			if (movementsBankAccounts.getDateMovement() == null) {
				movementsBankAccounts.setDateMovement(new Date());
			}
			
			validator = true;
		}

		return validator;
	}

	public String validardor(List<BankAccounts> list1, MovementsBankAccounts movementsBankAccounts) {

		if (list1.isEmpty()) {
			codigoValidatorMovementsBankAccounts = "";
		} else {
			codigoValidatorMovementsBankAccounts = list1.get(list1.size() - 1).getNumberAccount();

			movementsBankAccounts.getBankAccounts().setId(list1.get(list1.size() - 1).getId());
			movementsBankAccounts.getBankAccounts().setTypeBankAccounts(list1.get(list1.size() - 1).getTypeBankAccounts());
			movementsBankAccounts.getBankAccounts().setNumberAccount(codigoValidatorMovementsBankAccounts);
			movementsBankAccounts.getBankAccounts().setKeyAccount(list1.get(list1.size() - 1).getKeyAccount());
			movementsBankAccounts.getBankAccounts().setAvailableBalanceAccount(list1.get(list1.size() - 1).getAvailableBalanceAccount());
			movementsBankAccounts.getBankAccounts().setCustomer(list1.get(list1.size() - 1).getCustomer());
			
		}

		return codigoValidatorMovementsBankAccounts;
	}

	public BankAccounts validationUpdateBankAccountsRequest(List<BankAccounts> list1, BankAccounts bankAccounts) {

		bankAccounts.setId(list1.get(0).getId());
		bankAccounts.setTypeBankAccounts(list1.get(0).getTypeBankAccounts());
		bankAccounts.setNumberAccount(list1.get(0).getNumberAccount());
		bankAccounts.setKeyAccount(list1.get(0).getKeyAccount());
		bankAccounts.setAvailableBalanceAccount(list1.get(0).getAvailableBalanceAccount());
		bankAccounts.setCustomer(list1.get(0).getCustomer());
		bankAccounts.setStatusAccount(true);
		bankAccounts.setDateCreationBankAccount(list1.get(0).getDateCreationBankAccount());
		bankAccounts.setDateLastBankAccount(list1.get(0).getDateLastBankAccount());

		return bankAccounts;
	}

	public BankAccounts validationUpdateBankAccountsDestinationRequest(List<BankAccounts> list2,
			BankAccounts bankAccounts) {

		bankAccounts.setId(list2.get(0).getId());
		bankAccounts.setTypeBankAccounts(list2.get(0).getTypeBankAccounts());
		bankAccounts.setNumberAccount(list2.get(0).getNumberAccount());
		bankAccounts.setKeyAccount(list2.get(0).getKeyAccount());
		bankAccounts.setAvailableBalanceAccount(list2.get(0).getAvailableBalanceAccount());
		bankAccounts.setCustomer(list2.get(0).getCustomer());
		bankAccounts.setStatusAccount(true);
		bankAccounts.setDateCreationBankAccount(list2.get(0).getDateCreationBankAccount());
		bankAccounts.setDateLastBankAccount(list2.get(0).getDateLastBankAccount());

		return bankAccounts;
	}
}
