package com.example.spendly.currency.exchange;

import com.example.spendly.currency.common.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateService {

    BigDecimal getRate(
            CurrencyCode fromCurrency,
            CurrencyCode toCurrency,
            LocalDate date
    );
}