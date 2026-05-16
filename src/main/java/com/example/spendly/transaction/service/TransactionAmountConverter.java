package com.example.spendly.transaction.service;

import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.currency.converter.MoneyConverterService;
import com.example.spendly.transaction.model.ConvertedTransaction;
import com.example.spendly.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionAmountConverter {

    private final MoneyConverterService moneyConverterService;

    public List<ConvertedTransaction> convertAll(
            List<Transaction> transactions,
            CurrencyCode targetCurrency
    ){

        return transactions.stream()
                .map(transaction -> convert(transaction, targetCurrency))
                .toList();
    }


    private ConvertedTransaction convert(
            Transaction transaction,
            CurrencyCode targetCurrency
    ){

        LocalDate transactionDate = transaction.getTransactionDate();
        BigDecimal originalAmount = transaction.getAmount();
        CurrencyCode originalCurrency = transaction.getCurrency();

        BigDecimal convertedAmount = moneyConverterService.convert(originalCurrency, targetCurrency, originalAmount, transactionDate);

        return ConvertedTransaction.builder()
                .transactionDate(transactionDate)
                .originalAmount(originalAmount)
                .originalCurrency(originalCurrency)
                .targetCurrency(targetCurrency)
                .convertedAmount(convertedAmount)
                .bankCode(transaction.getBankCode())
                .category(transaction.getCategory())
                .build();
    }
}
