package com.example.spendly.currency.conversion;

import com.example.spendly.currency.common.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CurrencyConversionService {

    BigDecimal convert(
            CurrencyCode from,
            CurrencyCode to,
            BigDecimal amount,
            LocalDate transactionDate
    );
}