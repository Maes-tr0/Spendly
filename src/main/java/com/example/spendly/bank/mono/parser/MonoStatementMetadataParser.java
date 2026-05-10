package com.example.spendly.bank.mono.parser;

import com.example.spendly.bank.common.parser.StatementMetadataParser;
import com.example.spendly.bank.common.source.TextStatementSource;
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
public class MonoStatementMetadataParser implements StatementMetadataParser<TextStatementSource> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Pattern STATEMENT_PERIOD_PATTERN = Pattern.compile(
            "Period:\\s*(\\d{2}\\.\\d{2}\\.\\d{4})\\s*-\\s*(\\d{2}\\.\\d{2}\\.\\d{4})"
    );

    private static final Pattern OPENING_BALANCE_PATTERN = Pattern.compile(
            "Balance at the beginning of the period:\\s*(-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})\\s+([A-Z]{3})"
    );

    private static final Pattern CLOSING_BALANCE_PATTERN = Pattern.compile(
            "Balance at the end of the period:\\s*(-?\\d+(?:\\s\\d{3})*[\\.,]\\d{2})\\s+([A-Z]{3})"
    );

    @Override
    public StatementPeriod parsePeriod(TextStatementSource source) {
        String rawText = source.text();

        Matcher matcher = STATEMENT_PERIOD_PATTERN.matcher(rawText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse Monobank statement period");
        }

        LocalDate from = LocalDate.parse(matcher.group(1), DATE_FORMATTER);
        LocalDate to = LocalDate.parse(matcher.group(2), DATE_FORMATTER);

        return new StatementPeriod(from, to);
    }

    @Override
    public StatementBalanceSummary parseBalanceSummary(TextStatementSource source) {
        String rawText = source.text();

        MonoBalanceValue openingBalance = parseBalanceValue(rawText, OPENING_BALANCE_PATTERN);
        MonoBalanceValue closingBalance = parseBalanceValue(rawText, CLOSING_BALANCE_PATTERN);

        if (!openingBalance.currency().equals(closingBalance.currency())) {
            throw new IllegalArgumentException("Opening and closing balance currencies are different");
        }

        return new StatementBalanceSummary(
                openingBalance.amount(),
                closingBalance.amount(),
                openingBalance.currency()
        );
    }

    private MonoBalanceValue parseBalanceValue(String rawText, Pattern pattern) {
        Matcher matcher = pattern.matcher(rawText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse Monobank balance value");
        }

        BigDecimal amount = parseBigDecimal(matcher.group(1));
        CurrencyCode currency = CurrencyCode.fromString(matcher.group(2));

        return new MonoBalanceValue(amount, currency);
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

    private record MonoBalanceValue(
            BigDecimal amount,
            CurrencyCode currency
    ) {
    }
}