package com.example.spendly.bank.privat;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.transaction.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PrivatRawTransactionMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");


    public static ParsedStatement toParsedStatement(List<PrivatRawTransaction> rawTransactions) {
        List<ParsedTransaction> parsedTransactions = new ArrayList<>();

        for (PrivatRawTransaction rawTransaction : rawTransactions) {
            parsedTransactions.add(toParsedTransaction(rawTransaction));
        }

        return new ParsedStatement(parsedTransactions);
    }

    private static BigDecimal getCommissionAmount(PrivatRawTransaction rawTransaction) {
        CurrencyCode cardCurrency = getCurrencyCode(rawTransaction.cardCurrency());
        CurrencyCode operationCurrency = getCurrencyCode(rawTransaction.operationCurrency());

        if (cardCurrency == null || operationCurrency == null) {
            return null;
        }

        if (!cardCurrency.equals(operationCurrency)) {
            return null;
        }

        BigDecimal cardAmount = getBigDecimal(rawTransaction.cardAmount());
        BigDecimal operationAmount = getBigDecimal(rawTransaction.operationAmount());

        if (cardAmount == null || operationAmount == null) {
            return null;
        }

        BigDecimal commission = cardAmount.abs()
                .subtract(operationAmount.abs())
                .abs();

        if (commission.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return commission;
    }


    private static ParsedTransaction toParsedTransaction(PrivatRawTransaction rawTransaction) {
        LocalDateTime transactionDateTime = getLocalDateTime(rawTransaction.transactionDateTime());

        BigDecimal commissionAmount = getCommissionAmount(rawTransaction);
        CurrencyCode commissionCurrencyCode = commissionAmount == null
                ? null
                : getCurrencyCode(rawTransaction.cardCurrency());

        return new ParsedTransaction(
                transactionDateTime.toLocalDate(),
                transactionDateTime.toLocalTime(),
                null,
                getBigDecimal(rawTransaction.cardAmount()),
                getCurrencyCode(rawTransaction.cardCurrency()),
                getBigDecimal(rawTransaction.operationAmount()),
                getCurrencyCode(rawTransaction.operationCurrency()),
                null,
                rawTransaction.description(),
                rawTransaction.bankCategoryName(),
                null,
                null,
                null,
                commissionAmount,
                commissionCurrencyCode,
                null,
                null,
                getBigDecimal(rawTransaction.balanceAfterTransaction()),
                null,
                null
        );
    }

    private static LocalDateTime getLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.matches("\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}$")) {
            normalized = normalized + ":00";
        }

        return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
    }

    private static BigDecimal getBigDecimal(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        String normalized = value.trim()
                .replace(" ", "")
                .replace(",", ".");

        return new BigDecimal(normalized);
    }

    private static CurrencyCode getCurrencyCode(String value) {
        return CurrencyCode.fromString(value);
    }
}