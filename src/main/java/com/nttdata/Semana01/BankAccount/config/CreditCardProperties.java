package com.nttdata.Semana01.BankAccount.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Setter
@Getter
@ConfigurationProperties(prefix = "credit-api")
public class CreditCardProperties {
	private String baseUrl;
}
