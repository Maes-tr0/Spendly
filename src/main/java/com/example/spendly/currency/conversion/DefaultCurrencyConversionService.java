package com.example.spendly.currency.conversion;

import com.example.spendly.currency.common.exception.ExchangeRateException;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.currency.rate.ExchangeRateService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class DefaultCurrencyConversionService implements CurrencyConversionService {

    private final ExchangeRateService exchangeRateService;

    @Override
    public BigDecimal convert(
            CurrencyCode from,
            CurrencyCode to,
            BigDecimal amount,
            LocalDate transactionDate
    ) {
        if (amount == null) {
            throw new ExchangeRateException("Amount cannot be null");
        }

        if (from == null || to == null) {
            throw new ExchangeRateException("Currency cannot be null");
        }

        if (transactionDate == null) {
            throw new ExchangeRateException("Transaction date cannot be null");
        }

        if (from == to) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal rate = exchangeRateService.getRate(from, to, transactionDate);

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}