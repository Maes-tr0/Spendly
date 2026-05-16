package com.example.spendly.currency.exchange.provider.fawaz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spendly.currency.exchange.provider.fawaz")
public record FawazExchangeApiProperties(
        String baseUrl
) {
}