package com.example.spendly.bank.privat.mapper;

import com.example.spendly.bank.common.mapper.RawTransactionMapper;
import com.example.spendly.bank.privat.model.PrivatRawTransaction;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.ParsedTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class PrivatRawTransactionMapper implements RawTransactionMapper<PrivatRawTransaction> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public ParsedTransaction toParsedTransaction(PrivatRawTransaction rawTransaction) {
        LocalDateTime transactionDateTime = getLocalDateTime(rawTransaction.transactionDateTime());

        LocalDate transactionDate = transactionDateTime == null
                ? null
                : transactionDateTime.toLocalDate();

        LocalTime transactionTime = transactionDateTime == null
                ? null
                : transactionDateTime.toLocalTime();

        BigDecimal commissionAmount = getCommissionAmount(rawTransaction);

        CurrencyCode commissionCurrencyCode = commissionAmount == null
                ? null
                : getCurrencyCode(rawTransaction.cardCurrency());

        return new ParsedTransaction(
                transactionDate,
                transactionTime,
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

    private BigDecimal getCommissionAmount(PrivatRawTransaction rawTransaction) {
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

    private LocalDateTime getLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.matches("\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}$")) {
            normalized = normalized + ":00";
        }

        return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
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