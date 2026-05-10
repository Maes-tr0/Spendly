package com.example.spendly.bank.privat.parser;

import com.example.spendly.bank.common.exception.StatementParseException;
import com.example.spendly.bank.common.parser.StatementMetadataParser;
import com.example.spendly.bank.common.source.TableStatementSource;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PrivatStatementMetadataParser implements StatementMetadataParser<TableStatementSource> {

    private static final int TITLE_ROW_INDEX = 0;
    private static final int TITLE_COLUMN_INDEX = 0;

    private static final int FIRST_TRANSACTION_ROW_INDEX = 2;

    private static final int TRANSACTION_DATE_TIME_COLUMN = 0;
    private static final int CARD_AMOUNT_COLUMN = 4;
    private static final int BALANCE_AFTER_TRANSACTION_COLUMN = 8;
    private static final int BALANCE_CURRENCY_COLUMN = 9;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Pattern STATEMENT_PERIOD_PATTERN = Pattern.compile(
            ".*?(\\d{2}\\.\\d{2}\\.\\d{4})\\s*-\\s*(\\d{2}\\.\\d{2}\\.\\d{4}).*"
    );

    @Override
    public StatementPeriod parsePeriod(TableStatementSource source) {
        String title = source.getValue(TITLE_ROW_INDEX, TITLE_COLUMN_INDEX);

        Matcher matcher = STATEMENT_PERIOD_PATTERN.matcher(title);

        if (!matcher.matches()) {
            throw new StatementParseException("Cannot parse PrivatBank statement period: " + title);
        }

        LocalDate from = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
        LocalDate to = LocalDate.parse(matcher.group(2), DATE_FORMATTER);

        return new StatementPeriod(from, to);
    }

    @Override
    public StatementBalanceSummary parseBalanceSummary(TableStatementSource source) {
        int newestTransactionRowIndex = findFirstTransactionRowIndex(source);
        int oldestTransactionRowIndex = findLastTransactionRowIndex(source);

        BigDecimal closingBalance = getBigDecimal(
                source.getValue(newestTransactionRowIndex, BALANCE_AFTER_TRANSACTION_COLUMN)
        );

        BigDecimal oldestBalanceAfterTransaction = getBigDecimal(
                source.getValue(oldestTransactionRowIndex, BALANCE_AFTER_TRANSACTION_COLUMN)
        );

        BigDecimal oldestTransactionAmount = getBigDecimal(
                source.getValue(oldestTransactionRowIndex, CARD_AMOUNT_COLUMN)
        );

        BigDecimal openingBalance = null;

        if (oldestBalanceAfterTransaction != null && oldestTransactionAmount != null) {
            openingBalance = oldestBalanceAfterTransaction.subtract(oldestTransactionAmount);
        }

        CurrencyCode accountCurrency = getCurrencyCode(
                source.getValue(newestTransactionRowIndex, BALANCE_CURRENCY_COLUMN)
        );

        validateBalanceCurrencies(source, accountCurrency);

        return new StatementBalanceSummary(
                openingBalance,
                closingBalance,
                accountCurrency
        );
    }

    private int findFirstTransactionRowIndex(TableStatementSource source) {
        for (int i = FIRST_TRANSACTION_ROW_INDEX; i < source.rowCount(); i++) {
            String transactionDateTime = source.getValue(i, TRANSACTION_DATE_TIME_COLUMN);

            if (!transactionDateTime.isBlank()) {
                return i;
            }
        }

        throw new StatementParseException("Cannot find first PrivatBank transaction row");
    }

    private int findLastTransactionRowIndex(TableStatementSource source) {
        for (int i = source.rowCount() - 1; i >= FIRST_TRANSACTION_ROW_INDEX; i--) {
            String transactionDateTime = source.getValue(i, TRANSACTION_DATE_TIME_COLUMN);

            if (!transactionDateTime.isBlank()) {
                return i;
            }
        }

        throw new StatementParseException("Cannot find last PrivatBank transaction row");
    }

    private void validateBalanceCurrencies(
            TableStatementSource source,
            CurrencyCode expectedCurrency
    ) {
        if (expectedCurrency == null) {
            return;
        }

        for (int i = FIRST_TRANSACTION_ROW_INDEX; i < source.rowCount(); i++) {
            String transactionDateTime = source.getValue(i, TRANSACTION_DATE_TIME_COLUMN);

            if (transactionDateTime.isBlank()) {
                continue;
            }

            CurrencyCode actualCurrency = getCurrencyCode(source.getValue(i, BALANCE_CURRENCY_COLUMN));

            if (actualCurrency != null && !actualCurrency.equals(expectedCurrency)) {
                throw new StatementParseException("PrivatBank balance currencies are different in one statement");
            }
        }
    }

    private BigDecimal getBigDecimal(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return new BigDecimal(
                value.trim()
                        .replace(" ", "")
                        .replace(",", ".")
        );
    }

    private CurrencyCode getCurrencyCode(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return CurrencyCode.fromString(value);
    }
}