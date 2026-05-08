package com.example.spendly.currency.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CurrencyApiConfig {

    @Bean
    public RestClient currencyApiRestClient() {
        return RestClient.builder()
                .baseUrl("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@")
                .build();
    }
}
