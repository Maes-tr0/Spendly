package com.example.spendly.currency.rate.provider.fawaz;

import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.currency.rate.provider.ExchangeRateProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class FawazExchangeRateProvider implements ExchangeRateProvider {

    private final RestClient restClient;

    public FawazExchangeRateProvider(
            @Qualifier("fawazExchangeApiRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    @Override
    public BigDecimal getExchangeRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    ) {
        String baseCurrencyCode = fromCurrency.getAlphabeticCode().toLowerCase();
        String targetCurrencyCode = toCurrency.getAlphabeticCode().toLowerCase();

        JsonNode response = restClient
                .get()
                .uri("%s/v1/currencies/%s.json".formatted(date, baseCurrencyCode))
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.isEmpty()) {
            throw new IllegalStateException("Fawaz exchange API returned empty response");
        }

        JsonNode baseCurrencyNode = response.get(baseCurrencyCode);

        if (baseCurrencyNode == null || baseCurrencyNode.isNull()) {
            throw new IllegalStateException(
                    "Fawaz exchange API response does not contain base currency: " + fromCurrency
            );
        }

        JsonNode targetRateNode = baseCurrencyNode.get(targetCurrencyCode);

        if (targetRateNode == null || targetRateNode.isNull()) {
            throw new IllegalStateException(
                    "Fawaz exchange API response does not contain target currency: " + toCurrency
            );
        }

        return targetRateNode.decimalValue();
    }
}