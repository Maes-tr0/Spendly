package com.example.spendly.bank.mono;

import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonoPdfStatementParser implements BankStatementParser {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Pattern HEADER_CURRENCY_PATTERN =
            Pattern.compile("\\(([A-Z]{3})\\)");

    private static final Pattern TRANSACTION_DATE_PATTERN =
            Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");

    private static final Pattern STATEMENT_PERIOD_PATTERN = Pattern.compile(
            "Period:\\s*(\\d{2}\\.\\d{2}\\.\\d{4})\\s*-\\s*(\\d{2}\\.\\d{2}\\.\\d{4})"
    );

    private static final Pattern OPENING_BALANCE_PATTERN = Pattern.compile(
            "Balance at the beginning of the period:\\s*(-?\\d+(?:\\s\\d{3})*[.,]\\d{2})\\s+([A-Z]{3})"
    );

    private static final Pattern CLOSING_BALANCE_PATTERN = Pattern.compile(
            "Balance at the end of the period:\\s*(-?\\d+(?:\\s\\d{3})*[.,]\\d{2})\\s+([A-Z]{3})"
    );

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" +
                    "(\\d{2}:\\d{2}:\\d{2})\\s+" +
                    "(.+?)\\s+" +
                    "(\\d{4})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[.,]\\d{2})\\s+" +
                    "([A-Z]{3})\\s+" +
                    "(—|-?\\d+(?:\\s\\d{3})*[.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[.,]\\d{2})$"
    );

    @Override
    public ParsedStatement parse(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String rawText = stripper.getText(document);

            StatementPeriod period = parseStatementPeriod(rawText);
            StatementBalanceSummary balanceSummary = parseStatementBalanceSummary(rawText);

            List<MonoRawTransaction> rawTransactions = parseRawTransactions(rawText);

            return MonoRawTransactionMapper.toParsedStatement(
                    period,
                    balanceSummary,
                    rawTransactions
            );

        } catch (IOException e) {
            throw new RuntimeException("Cannot parse Monobank PDF statement", e);
        }
    }

    private StatementPeriod parseStatementPeriod(String rawText) {
        Matcher matcher = STATEMENT_PERIOD_PATTERN.matcher(rawText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse Monobank statement period");
        }

        LocalDate from = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
        LocalDate to = LocalDate.parse(matcher.group(2), DATE_FORMATTER);

        return new StatementPeriod(from, to);
    }

    private StatementBalanceSummary parseStatementBalanceSummary(String rawText) {
        BalanceValue openingBalance = parseBalanceValue(rawText, OPENING_BALANCE_PATTERN);
        BalanceValue closingBalance = parseBalanceValue(rawText, CLOSING_BALANCE_PATTERN);

        if (openingBalance.currency() != closingBalance.currency()) {
            throw new IllegalArgumentException("Opening and closing balance currencies are different");
        }

        return new StatementBalanceSummary(
                openingBalance.amount(),
                closingBalance.amount(),
                openingBalance.currency()
        );
    }

    private BalanceValue parseBalanceValue(String rawText, Pattern pattern) {
        Matcher matcher = pattern.matcher(rawText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse Monobank balance value");
        }

        BigDecimal amount = parseBigDecimal(matcher.group(1));
        CurrencyCode currency = CurrencyCode.fromString(matcher.group(2));

        return new BalanceValue(amount, currency);
    }

    private List<MonoRawTransaction> parseRawTransactions(String rawText) {
        int begin = rawText.indexOf("Date and time");
        int end = rawText.indexOf("Operating Director");

        if (begin == -1) {
            throw new IllegalArgumentException("Cannot find Monobank transaction table start");
        }

        if (end == -1) {
            throw new IllegalArgumentException("Cannot find Monobank transaction table end");
        }

        String rawTable = rawText.substring(begin, end);

        TableCurrencies tableCurrencies = parseTableCurrencies(rawTable);

        List<String> rows = normalizeTransactionRows(rawTable);

        List<MonoRawTransaction> transactions = new ArrayList<>();

        for (String row : rows) {
            transactions.add(parseTransactionRow(row, tableCurrencies));
        }

        return transactions;
    }

    private MonoRawTransaction parseTransactionRow(String row, TableCurrencies tableCurrencies) {
        Matcher matcher = TRANSACTION_PATTERN.matcher(row);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse Monobank transaction row: " + row);
        }

        return new MonoRawTransaction(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                matcher.group(4),

                matcher.group(5),
                tableCurrencies.cardCurrency(),

                matcher.group(6),
                matcher.group(7),

                matcher.group(8),

                matcher.group(9),
                tableCurrencies.commissionCurrency(),

                matcher.group(10),
                tableCurrencies.cashbackCurrency(),

                matcher.group(11)
        );
    }

    private List<String> normalizeTransactionRows(String table) {
        List<String> transactions = new ArrayList<>();

        String[] lines = table.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            String firstLine = lines[i].trim();

            if (!isTransactionStartLine(firstLine)) {
                continue;
            }

            int secondLineIndex = i + 1;

            while (secondLineIndex < lines.length && lines[secondLineIndex].isBlank()) {
                secondLineIndex++;
            }

            if (secondLineIndex >= lines.length) {
                throw new IllegalArgumentException("Cannot find Monobank transaction time line");
            }

            String secondLine = lines[secondLineIndex].trim();

            if (!isTransactionTimeLine(secondLine)) {
                throw new IllegalArgumentException("Cannot parse Monobank transaction time line: " + secondLine);
            }

            transactions.add(normalizeTransaction(firstLine, secondLine));

            i = secondLineIndex;
        }

        return transactions;
    }

    private boolean isTransactionStartLine(String line) {
        return line.matches("^\\d{2}\\.\\d{2}\\.\\d{4}\\s+.+");
    }

    private boolean isTransactionTimeLine(String line) {
        return line.matches("^\\d{2}:\\d{2}:\\d{2}.*");
    }

    private String normalizeTransaction(String firstLine, String secondLine) {
        String[] firstParts = firstLine.split("\\s+");

        String date = firstParts[0];

        int mccIndex = findMccIndex(firstParts, firstLine);

        String[] secondParts = secondLine.split("\\s+", 2);

        String time = secondParts[0];
        String descriptionTail = secondParts.length > 1 ? secondParts[1] : "";

        StringBuilder description = new StringBuilder();

        for (int i = 1; i < mccIndex; i++) {
            description.append(firstParts[i]).append(" ");
        }

        if (!descriptionTail.isBlank()) {
            description.append(descriptionTail).append(" ");
        }

        StringBuilder tail = new StringBuilder();

        for (int i = mccIndex; i < firstParts.length; i++) {
            tail.append(firstParts[i]).append(" ");
        }

        return (date + " " + time + " " + description + tail)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int findMccIndex(String[] parts, String sourceLine) {
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].matches("\\d{4}")) {
                return i;
            }
        }

        throw new IllegalArgumentException("Cannot find MCC in line: " + sourceLine);
    }

    private TableCurrencies parseTableCurrencies(String rawTable) {
        int firstTransactionIndex = findFirstTransactionIndex(rawTable);

        String header = rawTable.substring(0, firstTransactionIndex);

        Matcher matcher = HEADER_CURRENCY_PATTERN.matcher(header);

        List<String> currencies = new ArrayList<>();

        while (matcher.find()) {
            currencies.add(matcher.group(1));
        }

        if (currencies.size() < 3) {
            throw new IllegalArgumentException("Cannot detect Monobank table currencies");
        }

        return new TableCurrencies(
                currencies.get(0),
                currencies.get(1),
                currencies.get(2)
        );
    }

    private int findFirstTransactionIndex(String rawTable) {
        Matcher matcher = TRANSACTION_DATE_PATTERN.matcher(rawTable);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot find first transaction in Monobank table");
        }

        return matcher.start();
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return new BigDecimal(
                value.trim()
                        .replace(" ", "")
                        .replace(",", ".")
        );
    }

    private record TableCurrencies(
            String cardCurrency,
            String commissionCurrency,
            String cashbackCurrency
    ) {
    }

    private record BalanceValue(
            BigDecimal amount,
            CurrencyCode currency
    ) {
    }
}