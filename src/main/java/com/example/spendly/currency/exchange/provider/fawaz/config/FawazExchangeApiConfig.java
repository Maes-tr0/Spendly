package com.example.spendly.currency.exchange.provider.fawaz.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(FawazExchangeApiProperties.class)
public class FawazExchangeApiConfig {

    @Bean
    public RestClient fawazExchangeApiRestClient(FawazExchangeApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}