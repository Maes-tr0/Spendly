package com.example.spendly.bank.rabo.model;

import com.example.spendly.bank.common.model.BankRawTransaction;

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
) implements BankRawTransaction {
}