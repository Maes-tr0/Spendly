package com.example.spendly.bank.mono;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MonoRawTransactionMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static ParsedStatement toParsedStatement(
            StatementPeriod period,
            StatementBalanceSummary balanceSummary,
            List<MonoRawTransaction> monoRawTransactions
    ) {
        List<ParsedTransaction> parsedTransactions = new ArrayList<>();

        for (MonoRawTransaction monoRawTransaction : monoRawTransactions) {
            ParsedTransaction transaction = toParsedTransaction(monoRawTransaction);
            parsedTransactions.add(transaction);
        }

        return new ParsedStatement(
                BankCode.MONO,
                period,
                balanceSummary,
                parsedTransactions
        );
    }

    private static ParsedTransaction toParsedTransaction(MonoRawTransaction monoRawTransaction) {
        return new ParsedTransaction(
                getLocalDate(monoRawTransaction.transactionDate()),
                getLocalTime(monoRawTransaction.transactionTime()),
                null,

                getBigDecimal(monoRawTransaction.cardAmount()),
                getCurrencyCode(monoRawTransaction.cardCurrency()),

                getBigDecimal(monoRawTransaction.operationAmount()),
                getCurrencyCode(monoRawTransaction.operationCurrency()),

                null,
                monoRawTransaction.description(),
                monoRawTransaction.description(),

                monoRawTransaction.mccCode(),
                null,

                getBigDecimal(monoRawTransaction.exchangeRate()),

                getBigDecimal(monoRawTransaction.commissionAmount()),
                getCurrencyCode(monoRawTransaction.commissionCurrency()),

                getBigDecimal(monoRawTransaction.cashbackAmount()),
                getCurrencyCode(monoRawTransaction.cashbackCurrency()),

                getBigDecimal(monoRawTransaction.balanceAfterTransaction()),

                null,
                null
        );
    }

    private static BigDecimal getBigDecimal(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return new BigDecimal(
                value.trim()
                        .replace(" ", "")
                        .replace(",", ".")
        );
    }

    private static CurrencyCode getCurrencyCode(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return CurrencyCode.fromString(value);
    }

    private static LocalDate getLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value.trim(), DATE_FORMATTER);
    }

    private static LocalTime getLocalTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalTime.parse(value.trim(), TIME_FORMATTER);
    }
}