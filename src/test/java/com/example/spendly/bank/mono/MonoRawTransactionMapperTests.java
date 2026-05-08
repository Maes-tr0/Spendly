package com.example.spendly.bank.mono;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.currency.common.model.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonoRawTransactionMapperTests {

    @Test
    @DisplayName("Map raw transaction with amount containing thousand separator converts amount correctly")
    void ToParsedStatement_WithAmountContainingThousandSeparator_ConvertsAmountCorrectly() {
        MonoRawTransaction rawTransaction = new MonoRawTransaction(
                "21.02.2026",
                "11:03:44",
                "Money transfer",
                "4829",
                "-40.00",
                "EUR",
                "-2 024.80",
                "UAH",
                "50.62",
                "0.00",
                "EUR",
                "0.00",
                "UAH",
                "0.00"
        );

        ParsedStatement statement = MonoRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(LocalDate.of(2026, 2, 21), transaction.transactionDate()),
                () -> assertEquals(LocalTime.of(11, 3, 44), transaction.transactionDateTime()),
                () -> assertEquals(new BigDecimal("-40.00"), transaction.accountAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals(new BigDecimal("-2024.80"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.UAH, transaction.operationCurrencyCode()),
                () -> assertEquals(new BigDecimal("50.62"), transaction.exchangeRate()),
                () -> assertEquals(new BigDecimal("0.00"), transaction.balanceAfterTransaction())
        );
    }

    @Test
    @DisplayName("Map raw transaction with dash exchange rate returns null exchange rate")
    void ToParsedStatement_WithDashExchangeRate_ReturnsNullExchangeRate() {
        MonoRawTransaction rawTransaction = new MonoRawTransaction(
                "09.03.2026",
                "20:44:57",
                "Money transfers",
                "4829",
                "8.00",
                "EUR",
                "8.00",
                "EUR",
                "—",
                "0.00",
                "EUR",
                "0.00",
                "UAH",
                "16.37"
        );

        ParsedStatement statement = MonoRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(new BigDecimal("8.00"), transaction.accountAmount()),
                () -> assertEquals(new BigDecimal("8.00"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.operationCurrencyCode()),
                () -> assertNull(transaction.exchangeRate()),
                () -> assertEquals(new BigDecimal("16.37"), transaction.balanceAfterTransaction())
        );
    }
}