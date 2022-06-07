package com.nttdata.Semana01.BankAccount.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nttdata.Semana01.BankAccount.DTO.Customer;
import com.nttdata.Semana01.BankAccount.config.CustomerApiProperties;
import com.nttdata.Semana01.BankAccount.response.CustomerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerApiClient {

	private final WebClient webClient;
	private final CustomerApiProperties customerApiProperties;
	
	public List<CustomerResponse> getCustomertbyDNI(String dni) throws InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		List<CustomerResponse> result = new ArrayList<>();
		webClient.get().uri(customerApiProperties.getBaseUrl() + "/customer/customerbydni/".concat(dni)) 
				.accept(MediaType.TEXT_EVENT_STREAM).retrieve().bodyToFlux(CustomerResponse.class)
				.publishOn(Schedulers.fromExecutor(executor))
				.subscribe(customerResponse -> result.add(customerResponse));

		executor.awaitTermination(2, TimeUnit.SECONDS);
		log.info("Assurance list " + result);
		return result;
	}
	
}
