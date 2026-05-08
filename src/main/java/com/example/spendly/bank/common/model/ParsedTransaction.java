package com.example.spendly.bank.common.model;

import com.example.spendly.currency.common.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ParsedTransaction(
        LocalDate transactionDate,
        LocalTime transactionDateTime,
        LocalDate processingDate,

        BigDecimal accountAmount,
        CurrencyCode accountCurrencyCode,

        BigDecimal operationAmount,
        CurrencyCode operationCurrencyCode,

        String merchantName,
        String description,
        String bankCategoryName,

        String mccCode,
        String transactionTypeCode,

        BigDecimal exchangeRate,
        BigDecimal commissionAmount,
        CurrencyCode commissionCurrencyCode,

        BigDecimal cashbackAmount,
        CurrencyCode cashbackCurrencyCode,

        BigDecimal balanceAfterTransaction,

        String paymentReference,
        String endToEndId
) {
}