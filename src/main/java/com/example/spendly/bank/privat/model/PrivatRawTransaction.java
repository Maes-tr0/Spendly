package com.example.spendly.bank.privat.model;

import com.example.spendly.bank.common.model.BankRawTransaction;

public record PrivatRawTransaction(
        String transactionDateTime,
        String bankCategoryName,
        String maskedCardNumber,
        String description,
        String cardAmount,
        String cardCurrency,
        String operationAmount,
        String operationCurrency,
        String balanceAfterTransaction,
        String balanceCurrency
) implements BankRawTransaction {
}
