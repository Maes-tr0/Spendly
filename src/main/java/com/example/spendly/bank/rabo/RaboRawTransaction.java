package com.example.spendly.bank.rabo;

public record RaboRawTransaction(
        String valueDate,
        String transactionTypeCode,
        String counterpartyAccount,
        String description,
        String debitAmount,
        String creditAmount,
        String processingDate,
        String paymentReference,
        String endToEndId,
        String accountCurrency
) {
}