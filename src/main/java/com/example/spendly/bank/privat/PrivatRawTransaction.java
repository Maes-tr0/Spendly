package com.example.spendly.bank.privat;

import lombok.Builder;

@Builder
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
) {
}
