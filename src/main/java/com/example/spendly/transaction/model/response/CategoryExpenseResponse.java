package com.example.spendly.transaction.model.response;

import com.example.spendly.transaction.model.TransactionCategory;

import java.math.BigDecimal;

public record CategoryExpenseResponse(
        TransactionCategory category,
        String displayName,
        BigDecimal expense
) {
}