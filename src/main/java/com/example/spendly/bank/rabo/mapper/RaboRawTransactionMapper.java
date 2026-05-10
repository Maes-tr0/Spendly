package com.example.spendly.bank.rabo.mapper;

import com.example.spendly.bank.common.mapper.RawTransactionMapper;
import com.example.spendly.bank.rabo.model.RaboRawTransaction;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.ParsedTransaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RaboRawTransactionMapper implements RawTransactionMapper<RaboRawTransaction> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public ParsedTransaction toParsedTransaction(RaboRawTransaction rawTransaction) {
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

    private BigDecimal getSignedAmount(RaboRawTransaction rawTransaction) {
        if (rawTransaction.debitAmount() != null && !rawTransaction.debitAmount().isBlank()) {
            return getBigDecimal(rawTransaction.debitAmount()).negate();
        }

        if (rawTransaction.creditAmount() != null && !rawTransaction.creditAmount().isBlank()) {
            return getBigDecimal(rawTransaction.creditAmount());
        }

        return null;
    }

    private BigDecimal getBigDecimal(String value) {
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

    private CurrencyCode getCurrencyCode(String value) {
        if (value == null || value.isBlank() || value.equals("—")) {
            return null;
        }

        return CurrencyCode.fromString(value);
    }

    private LocalDate getLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value.trim(), DATE_FORMATTER);
    }
}