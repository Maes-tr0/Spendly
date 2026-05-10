package com.example.spendly.currency.rate;

import com.example.spendly.currency.common.exception.ExchangeRateException;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.currency.rate.provider.ExchangeRateProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DefaultExchangeRateService implements ExchangeRateService {

    private final ExchangeRateProvider exchangeRateProvider;

    @Override
    public BigDecimal getRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    ) {
        validateRateRequest(fromCurrency, toCurrency, date);

        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        return exchangeRateProvider.getExchangeRate(
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
        if (fromCurrency == null) {
            throw new ExchangeRateException("From currency must not be null");
        }

        if (toCurrency == null) {
            throw new ExchangeRateException("To currency must not be null");
        }

        if (date == null) {
            throw new ExchangeRateException("Exchange rate date must not be null");
        }
    }
}