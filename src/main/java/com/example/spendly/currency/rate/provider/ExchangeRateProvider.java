package com.example.spendly.currency.rate.provider;

import com.example.spendly.currency.common.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contract for obtaining exchange rates from a specific rate source.
 * <p>
 * Implementations may use an external API, database, cache, file, or any other source.
 * This interface hides the technical details of how the exchange rate is received.
 */
public interface ExchangeRateProvider {

    /**
     * Returns the exchange rate for converting money from one currency to another
     * on a specific date.
     *
     * @param fromCurrency source currency
     * @param toCurrency target currency
     * @param date date for which the exchange rate should be received
     * @return exchange rate from source currency to target currency
     */
    BigDecimal getExchangeRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    );
}