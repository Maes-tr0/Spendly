package com.example.spendly.transaction.model.response;

import com.example.spendly.currency.common.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransactionAnalysisResponse(
        LocalDate dateFrom,
        LocalDate dateTo,
        CurrencyCode targetCurrency,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal difference,
        List<CategoryExpenseResponse> categories,
        int transactionCount
) {
}