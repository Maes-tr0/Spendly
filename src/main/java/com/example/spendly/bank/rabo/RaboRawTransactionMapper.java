package com.example.spendly.bank.rabo;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RaboRawTransactionMapper {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static ParsedStatement toParsedStatement(
            StatementPeriod period,
            StatementBalanceSummary balanceSummary,
            List<RaboRawTransaction> rawTransactions
    ) {
        List<ParsedTransaction> parsedTransactions = new ArrayList<>();

        for (RaboRawTransaction rawTransaction : rawTransactions) {
            parsedTransactions.add(toParsedTransaction(rawTransaction));
        }

        return new ParsedStatement(
                BankCode.RABO,
                period,
                balanceSummary,
                parsedTransactions
        );
    }

    private static ParsedTransaction toParsedTransaction(RaboRawTransaction rawTransaction) {
        BigDecimal accountAmount = getSignedAmount(rawTransaction);
        CurrencyCode accountCurrency = getCurrencyCode(rawTransaction.accountCurrency());

        return new ParsedTransaction(
                getLocalDate(rawTransaction.valueDate()),
                null,
                getLocalDate(rawTransaction.processingDate()),

                accountAmount,
                accountCurrency,

                accountAmount,
                accountCurrency,

                null,
                rawTransaction.description(),
                null,

                null,
                rawTransaction.transactionTypeCode(),

                null,

                null,
                null,

                null,
                null,

                null,

                rawTransaction.paymentReference(),
                rawTransaction.endToEndId()
        );
    }

    private static BigDecimal getSignedAmount(RaboRawTransaction rawTransaction) {
        if (rawTransaction.debitAmount() != null && !rawTransaction.debitAmount().isBlank()) {
            return getBigDecimal(rawTransaction.debitAmount()).negate();
        }

        if (rawTransaction.creditAmount() != null && !rawTransaction.creditAmount().isBlank()) {
            return getBigDecimal(rawTransaction.creditAmount());
        }

        return null;
    }

    private static BigDecimal getBigDecimal(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return new BigDecimal(
                value.trim()
                        .replace("\u00A0", "")
                        .replace(" ", "")
                        .replace(".", "")
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
}