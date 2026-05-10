package com.example.spendly.bank.privat;

import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivatXlsxStatementParser implements BankStatementParser {

    private static final int TITLE_ROW_INDEX = 0;
    private static final int TITLE_COLUMN_INDEX = 0;

    private static final int FIRST_TRANSACTION_ROW_INDEX = 2;

    private static final int TRANSACTION_DATE_TIME_COLUMN = 0;
    private static final int BANK_CATEGORY_NAME_COLUMN = 1;
    private static final int MASKED_CARD_NUMBER_COLUMN = 2;
    private static final int DESCRIPTION_COLUMN = 3;
    private static final int CARD_AMOUNT_COLUMN = 4;
    private static final int CARD_CURRENCY_COLUMN = 5;
    private static final int OPERATION_AMOUNT_COLUMN = 6;
    private static final int OPERATION_CURRENCY_COLUMN = 7;
    private static final int BALANCE_AFTER_TRANSACTION_COLUMN = 8;
    private static final int BALANCE_CURRENCY_COLUMN = 9;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Pattern STATEMENT_PERIOD_PATTERN = Pattern.compile(
            ".*?(\\d{2}\\.\\d{2}\\.\\d{4})\\s*-\\s*(\\d{2}\\.\\d{2}\\.\\d{4}).*"
    );

    @Override
    public ParsedStatement parse(File file) {
        try (
                FileInputStream inputStream = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);

            StatementPeriod period = getStatementPeriod(sheet);

            List<PrivatRawTransaction> rawTransactions = getPrivatRawTransactions(sheet);

            StatementBalanceSummary balanceSummary = getStatementBalanceSummary(rawTransactions);

            return PrivatRawTransactionMapper.toParsedStatement(
                    period,
                    balanceSummary,
                    rawTransactions
            );

        } catch (IOException e) {
            throw new RuntimeException("Cannot parse PrivatBank XLSX statement", e);
        }
    }

    private StatementPeriod getStatementPeriod(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();

        Row titleRow = sheet.getRow(TITLE_ROW_INDEX);

        if (titleRow == null) {
            throw new IllegalArgumentException("Cannot find PrivatBank statement title row");
        }

        String title = getCellValue(titleRow, TITLE_COLUMN_INDEX, formatter);

        Matcher matcher = STATEMENT_PERIOD_PATTERN.matcher(title);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse PrivatBank statement period: " + title);
        }

        LocalDate from = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
        LocalDate to = LocalDate.parse(matcher.group(2), DATE_FORMATTER);

        return new StatementPeriod(from, to);
    }

    private StatementBalanceSummary getStatementBalanceSummary(List<PrivatRawTransaction> rawTransactions) {
        if (rawTransactions == null || rawTransactions.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate PrivatBank balance summary without transactions");
        }

        PrivatRawTransaction newestTransaction = rawTransactions.get(0);
        PrivatRawTransaction oldestTransaction = rawTransactions.get(rawTransactions.size() - 1);

        BigDecimal closingBalance = getBigDecimal(newestTransaction.balanceAfterTransaction());

        BigDecimal oldestBalanceAfterTransaction = getBigDecimal(oldestTransaction.balanceAfterTransaction());
        BigDecimal oldestTransactionAmount = getBigDecimal(oldestTransaction.cardAmount());

        BigDecimal openingBalance = null;

        if (oldestBalanceAfterTransaction != null && oldestTransactionAmount != null) {
            openingBalance = oldestBalanceAfterTransaction.subtract(oldestTransactionAmount);
        }

        CurrencyCode accountCurrency = getCurrencyCode(newestTransaction.balanceCurrency());

        validateBalanceCurrencies(rawTransactions, accountCurrency);

        return new StatementBalanceSummary(
                openingBalance,
                closingBalance,
                accountCurrency
        );
    }

    private void validateBalanceCurrencies(
            List<PrivatRawTransaction> rawTransactions,
            CurrencyCode expectedCurrency
    ) {
        if (expectedCurrency == null) {
            return;
        }

        for (PrivatRawTransaction rawTransaction : rawTransactions) {
            CurrencyCode actualCurrency = getCurrencyCode(rawTransaction.balanceCurrency());

            if (actualCurrency != null && !actualCurrency.equals(expectedCurrency)) {
                throw new IllegalArgumentException("PrivatBank balance currencies are different in one statement");
            }
        }
    }

    private List<PrivatRawTransaction> getPrivatRawTransactions(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        List<PrivatRawTransaction> transactions = new ArrayList<>();

        for (int i = FIRST_TRANSACTION_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            String transactionDateTime = getCellValue(row, TRANSACTION_DATE_TIME_COLUMN, formatter);

            if (transactionDateTime.isBlank()) {
                continue;
            }

            transactions.add(mapRowToPrivatRawTransaction(row, formatter));
        }

        return transactions;
    }

    private PrivatRawTransaction mapRowToPrivatRawTransaction(Row row, DataFormatter formatter) {
        return new PrivatRawTransaction(
                getCellValue(row, TRANSACTION_DATE_TIME_COLUMN, formatter),
                getCellValue(row, BANK_CATEGORY_NAME_COLUMN, formatter),
                getCellValue(row, MASKED_CARD_NUMBER_COLUMN, formatter),
                getCellValue(row, DESCRIPTION_COLUMN, formatter),

                getCellValue(row, CARD_AMOUNT_COLUMN, formatter),
                getCellValue(row, CARD_CURRENCY_COLUMN, formatter),

                getCellValue(row, OPERATION_AMOUNT_COLUMN, formatter),
                getCellValue(row, OPERATION_CURRENCY_COLUMN, formatter),

                getCellValue(row, BALANCE_AFTER_TRANSACTION_COLUMN, formatter),
                getCellValue(row, BALANCE_CURRENCY_COLUMN, formatter)
        );
    }

    private String getCellValue(Row row, int columnIndex, DataFormatter formatter) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
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