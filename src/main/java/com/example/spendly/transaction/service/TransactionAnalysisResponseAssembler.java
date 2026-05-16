package com.example.spendly.transaction.service;

import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.transaction.model.TransactionCalculationResult;
import com.example.spendly.transaction.model.response.CategoryExpenseResponse;
import com.example.spendly.transaction.model.response.TransactionAnalysisResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class TransactionAnalysisResponseAssembler {

    public TransactionAnalysisResponse assemble(
            TransactionCalculationResult calculationResult,
            CurrencyCode targetCurrency,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<CategoryExpenseResponse> categories = calculationResult.expensesByCategory()
                .entrySet()
                .stream()
                .map(entry -> new CategoryExpenseResponse(
                        entry.getKey(),
                        entry.getKey().getDisplayName(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparing(CategoryExpenseResponse::expense).reversed())
                .toList();

        return new TransactionAnalysisResponse(
                dateFrom,
                dateTo,
                targetCurrency,
                calculationResult.totalIncome(),
                calculationResult.totalExpense(),
                calculationResult.difference(),
                categories,
                calculationResult.transactionCount()
        );
    }
}