package com.example.spendly.bank.mono.parser;

import com.example.spendly.bank.common.exception.StatementParseException;
import com.example.spendly.bank.common.parser.RawTransactionParser;
import com.example.spendly.bank.common.source.TextStatementSource;
import com.example.spendly.bank.mono.model.MonoRawTransaction;
import com.example.spendly.statement.model.StatementPeriod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MonoRawTransactionParser implements RawTransactionParser<TextStatementSource, MonoRawTransaction> {

    private static final Pattern HEADER_CURRENCY_PATTERN = Pattern.compile("\\(([A-Z]{3})\\)");

    private static final Pattern TRANSACTION_DATE_PATTERN = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" +
                    "(\\d{2}:\\d{2}:\\d{2})\\s+" +
                    "(.+?)\\s+" +
                    "(\\d{4})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})\\s+" +
                    "([A-Z]{3})\\s+" +
                    "(—|-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})\\s+" +
                    "(-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})$"
    );

    @Override
    public List<MonoRawTransaction> parse(TextStatementSource source, StatementPeriod period) {
        String rawText = source.text();

        String rawTable = extractTransactionTable(rawText);

        TableCurrencies tableCurrencies = parseTableCurrencies(rawTable);

        List<String> rows = normalizeTransactionRows(rawTable);

        List<MonoRawTransaction> transactions = new ArrayList<>();

        for (String row : rows) {
            transactions.add(parseTransactionRow(row, tableCurrencies));
        }

        return transactions;
    }

    private String extractTransactionTable(String rawText) {
        int begin = rawText.indexOf("Date and time");
        int end = rawText.indexOf("Operating Director");

        if (begin == -1) {
            throw new StatementParseException("Cannot find Monobank transaction table start");
        }

        if (end == -1) {
            throw new StatementParseException("Cannot find Monobank transaction table end");
        }

        return rawText.substring(begin, end);
    }

    private MonoRawTransaction parseTransactionRow(String row, TableCurrencies tableCurrencies) {
        Matcher matcher = TRANSACTION_PATTERN.matcher(row);

        if (!matcher.matches()) {
            throw new StatementParseException("Cannot parse Monobank transaction row: " + row);
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
                throw new StatementParseException("Cannot find Monobank transaction time line");
            }

            String secondLine = lines[secondLineIndex].trim();

            if (!isTransactionTimeLine(secondLine)) {
                throw new StatementParseException("Cannot parse Monobank transaction time line: " + secondLine);
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

        throw new StatementParseException("Cannot find MCC in line: " + sourceLine);
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
            throw new StatementParseException("Cannot detect Monobank table currencies");
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
            throw new StatementParseException("Cannot find first transaction in Monobank table");
        }

        return matcher.start();
    }

    private record TableCurrencies(
            String cardCurrency,
            String commissionCurrency,
            String cashbackCurrency
    ) {
    }
}