package com.example.spendly.transaction.model;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.currency.common.model.CurrencyCode;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record ConvertedTransaction(
        BankCode bankCode,
        LocalDate transactionDate,
        TransactionCategory category,
        BigDecimal originalAmount,
        CurrencyCode originalCurrency,
        BigDecimal convertedAmount,
        CurrencyCode targetCurrency
) {
}
