package com.example.spendly.bank.rabo.parser;

import com.example.spendly.bank.common.exception.StatementParseException;
import com.example.spendly.bank.common.parser.StatementMetadataParser;
import com.example.spendly.bank.common.source.TextStatementSource;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RaboStatementMetadataParser implements StatementMetadataParser<TextStatementSource> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final String AMOUNT_REGEX =
            "\\d{1,3}(?:\\.\\d{3})*,\\d{2}|\\d+,\\d{2}";

    private static final Pattern TRANSACTION_START_PATTERN = Pattern.compile(
            "^(\\d{2}-\\d{2})\\s+" +
                    "([a-z]{2})\\s+" +
                    "(.+?)\\s+" +
                    "(" + AMOUNT_REGEX + ")$"
    );

    private static final Pattern ACCOUNT_CURRENCY_PATTERN = Pattern.compile(
            "\\bNL\\d{2}\\s+RABO(?:\\s+\\d{4}){2}\\s+\\d{2}\\s+([A-Z]{3})\\b"
    );

    private static final Pattern FULL_DATE_PATTERN = Pattern.compile(
            "\\b\\d{2}-\\d{2}-\\d{4}\\b"
    );

    private static final Pattern BALANCE_VALUE_PATTERN = Pattern.compile(
            "\\b(" + AMOUNT_REGEX + ")\\s+(CR|D)\\b"
    );

    @Override
    public StatementPeriod parsePeriod(TextStatementSource source) {
        String statementHeader = extractStatementHeader(source.text());

        List<LocalDate> dates = extractFullDates(statementHeader);

        if (dates.size() >= 3) {
            return new StatementPeriod(
                    dates.get(1),
                    dates.get(2)
            );
        }

        if (dates.size() >= 2) {
            return new StatementPeriod(
                    dates.get(0),
                    dates.get(1)
            );
        }

        throw new StatementParseException("Cannot parse Rabobank statement period from header: " + statementHeader);
    }

    @Override
    public StatementBalanceSummary parseBalanceSummary(TextStatementSource source) {
        String rawText = source.text();
        String statementHeader = extractStatementHeader(rawText);

        List<RaboBalanceValue> balances = extractBalanceValues(statementHeader);

        if (balances.size() < 2) {
            throw new StatementParseException("Cannot parse Rabobank opening and closing balance from header: " + statementHeader);
        }

        CurrencyCode accountCurrency = CurrencyCode.fromString(extractAccountCurrency(rawText));

        return new StatementBalanceSummary(
                balances.get(0).amount(),
                balances.get(1).amount(),
                accountCurrency
        );
    }

    private String extractStatementHeader(String rawText) {
        List<String> lines = getNormalizedLines(rawText);

        StringBuilder header = new StringBuilder();

        for (String line : lines) {
            if (TRANSACTION_START_PATTERN.matcher(line).matches()) {
                break;
            }

            header.append(line).append("\n");
        }

        if (header.isEmpty()) {
            throw new StatementParseException("Cannot extract Rabobank statement header");
        }

        return header.toString();
    }

    private List<LocalDate> extractFullDates(String text) {
        Matcher matcher = FULL_DATE_PATTERN.matcher(text);

        List<LocalDate> dates = new ArrayList<>();

        while (matcher.find()) {
            dates.add(LocalDate.parse(matcher.group(), DATE_FORMATTER));
        }

        return dates;
    }

    private List<RaboBalanceValue> extractBalanceValues(String text) {
        Matcher matcher = BALANCE_VALUE_PATTERN.matcher(text);

        List<RaboBalanceValue> balances = new ArrayList<>();

        while (matcher.find()) {
            BigDecimal amount = parseAmount(matcher.group(1));
            String type = matcher.group(2);

            if ("D".equals(type)) {
                amount = amount.negate();
            }

            balances.add(new RaboBalanceValue(amount, type));
        }

        return balances;
    }

    private String extractAccountCurrency(String rawText) {
        Matcher matcher = ACCOUNT_CURRENCY_PATTERN.matcher(rawText);

        if (!matcher.find()) {
            throw new StatementParseException("Cannot find Rabobank account currency");
        }

        return matcher.group(1);
    }

    private List<String> getNormalizedLines(String rawText) {
        List<String> lines = new ArrayList<>();

        for (String rawLine : rawText.split("\\R")) {
            String line = normalizeLine(rawLine);

            if (!line.isBlank()) {
                lines.add(line);
            }
        }

        return lines;
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
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

    private String normalizeLine(String line) {
        if (line == null) {
            return "";
        }

        return line
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record RaboBalanceValue(
            BigDecimal amount,
            String type
    ) {
    }
}