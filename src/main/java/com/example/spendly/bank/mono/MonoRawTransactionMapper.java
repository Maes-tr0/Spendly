package com.example.spendly.bank.mono;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.transaction.model.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MonoRawTransactionMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");


    public static ParsedStatement toParsedStatement(List<MonoRawTransaction> monoRawTransactions) {

        List<ParsedTransaction> parsedTransactions = new ArrayList<>();

        for (MonoRawTransaction monoRawTransaction : monoRawTransactions) {
            ParsedTransaction transaction = new ParsedTransaction(
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
            parsedTransactions.add(transaction);
        }

        return new ParsedStatement(parsedTransactions);
    }


    private static BigDecimal getBigDecimal(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return new BigDecimal(
                value.trim()
                        .replace(" ", "")
        );
    }

    private static CurrencyCode getCurrencyCode(String value) {
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
