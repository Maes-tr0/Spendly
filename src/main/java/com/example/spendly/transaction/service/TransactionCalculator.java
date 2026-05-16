package com.example.spendly.transaction.service;

import com.example.spendly.transaction.model.ConvertedTransaction;
import com.example.spendly.transaction.model.TransactionCalculationResult;
import com.example.spendly.transaction.model.TransactionCategory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionCalculator {

    public TransactionCalculationResult calculate(List<ConvertedTransaction> convertedTransactions) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        Map<TransactionCategory, BigDecimal> expensesByCategory =
                new EnumMap<>(TransactionCategory.class);

        for (ConvertedTransaction transaction : convertedTransactions) {
            BigDecimal amount = transaction.convertedAmount();

            if (amount == null) {
                continue;
            }

            if (amount.signum() > 0) {
                totalIncome = totalIncome.add(amount);
                continue;
            }

            if (amount.signum() < 0) {
                BigDecimal expenseAmount = amount.abs();

                totalExpense = totalExpense.add(expenseAmount);

                TransactionCategory category = transaction.category() == null
                        ? TransactionCategory.UNCATEGORIZED
                        : transaction.category();

                expensesByCategory.merge(
                        category,
                        expenseAmount,
                        BigDecimal::add
                );
            }
        }

        return new TransactionCalculationResult(
                totalIncome,
                totalExpense,
                expensesByCategory,
                convertedTransactions.size()
        );
    }
}