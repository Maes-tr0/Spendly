package com.example.spendly.transaction.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record TransactionCalculationResult(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        Map<TransactionCategory, BigDecimal> expensesByCategory,
        int transactionCount
) {
    public TransactionCalculationResult {
        totalIncome = Objects.requireNonNullElse(totalIncome, BigDecimal.ZERO);
        totalExpense = Objects.requireNonNullElse(totalExpense, BigDecimal.ZERO);
        expensesByCategory = expensesByCategory == null
                ? Map.of()
                : Map.copyOf(expensesByCategory);

        if (transactionCount < 0) {
            throw new IllegalArgumentException("Transaction count cannot be negative");
        }
    }

    public BigDecimal difference() {
        return totalIncome.subtract(totalExpense);
    }
}