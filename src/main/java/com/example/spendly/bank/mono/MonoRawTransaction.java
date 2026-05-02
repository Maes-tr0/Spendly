package com.example.spendly.bank.mono;

public record MonoRawTransaction(
        String transactionDate,
        String transactionTime,
        String description,
        String mccCode,
        String cardAmount,
        String cardCurrency,
        String operationAmount,
        String operationCurrency,
        String exchangeRate,
        String commissionAmount,
        String commissionCurrency,
        String cashbackAmount,
        String cashbackCurrency,
        String balanceAfterTransaction
) {
}