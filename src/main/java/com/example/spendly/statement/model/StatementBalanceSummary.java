package com.example.spendly.statement.model;

import com.example.spendly.currency.common.model.CurrencyCode;

import java.math.BigDecimal;

public record StatementBalanceSummary(
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        CurrencyCode accountCurrency
) {
}