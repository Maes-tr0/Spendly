package com.example.spendly.currency.rate;

import com.example.spendly.currency.common.model.CurrencyCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class DefaultExchangeRateService implements ExchangeRateService {

    private static final String CURRENCY_RATE_URL = "{date}/v1/currencies/{baseCurrency}.min.json";

    private final RestClient currencyApiRestClient;

    @Override
    public BigDecimal getRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    ) {
        validateRateRequest(fromCurrency, toCurrency, date);

        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE;
        }

        String baseCurrencyCode = fromCurrency.getAlphabeticCode().toLowerCase();
        String targetCurrencyCode = toCurrency.getAlphabeticCode().toLowerCase();

        JsonNode response = fetchRatesResponse(date, baseCurrencyCode);

        return extractRateFromResponse(
                response,
                baseCurrencyCode,
                targetCurrencyCode,
                fromCurrency,
                toCurrency,
                date
        );
    }

    private void validateRateRequest(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    ) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }

        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }

    private JsonNode fetchRatesResponse(
            LocalDate date,
            String baseCurrencyCode
    ) {
        JsonNode response = currencyApiRestClient.get()
                .uri(CURRENCY_RATE_URL, date, baseCurrencyCode)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("Currency API response is empty");
        }

        return response;
    }

    private BigDecimal extractRateFromResponse(
            JsonNode response,
            String baseCurrencyCode,
            String targetCurrencyCode,
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    ) {
        JsonNode ratesNode = response.get(baseCurrencyCode);

        if (ratesNode == null || ratesNode.isNull()) {
            throw new IllegalStateException(
                    "Rates not found for base currency: " + fromCurrency
            );
        }

        JsonNode rateNode = ratesNode.get(targetCurrencyCode);

        if (rateNode == null || rateNode.isNull()) {
            throw new IllegalStateException(
                    "Exchange rate not found: " + fromCurrency + " to " + toCurrency + " on " + date
            );
        }

        return rateNode.decimalValue();
    }
}