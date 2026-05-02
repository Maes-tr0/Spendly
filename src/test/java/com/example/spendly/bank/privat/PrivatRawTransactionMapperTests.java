package com.example.spendly.bank.privat;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.transaction.model.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrivatRawTransactionMapperTests {

    @Test
    @DisplayName("Map Privat transaction with same currency and different amounts calculates commission")
    void ToParsedStatement_WithSameCurrencyAndDifferentAmounts_CalculatesCommission() {
        PrivatRawTransaction rawTransaction = PrivatRawTransaction.builder()
                .transactionDateTime("26.04.2026 13:46:43")
                .bankCategoryName("Перекази")
                .maskedCardNumber("5168 **** **** 1234")
                .description("Переказ між картками")
                .cardAmount("-101.00")
                .cardCurrency("EUR")
                .operationAmount("-100.00")
                .operationCurrency("EUR")
                .balanceAfterTransaction("500.00")
                .balanceCurrency("EUR")
                .build();

        ParsedStatement statement = PrivatRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(LocalDate.of(2026, 4, 26), transaction.transactionDate()),
                () -> assertEquals(LocalTime.of(13, 46, 43), transaction.transactionDateTime()),
                () -> assertEquals(new BigDecimal("-101.00"), transaction.accountAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals(new BigDecimal("-100.00"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.operationCurrencyCode()),
                () -> assertEquals(new BigDecimal("1.00"), transaction.commissionAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.commissionCurrencyCode()),
                () -> assertEquals(new BigDecimal("500.00"), transaction.balanceAfterTransaction())
        );
    }

    @Test
    @DisplayName("Map Privat transaction with same currency and equal amounts returns null commission")
    void ToParsedStatement_WithSameCurrencyAndEqualAmounts_ReturnsNullCommission() {
        PrivatRawTransaction rawTransaction = PrivatRawTransaction.builder()
                .transactionDateTime("26.04.2026 13:46:43")
                .bankCategoryName("Перекази")
                .maskedCardNumber("5168 **** **** 1234")
                .description("Переказ між картками")
                .cardAmount("-100.00")
                .cardCurrency("EUR")
                .operationAmount("-100.00")
                .operationCurrency("EUR")
                .balanceAfterTransaction("500.00")
                .balanceCurrency("EUR")
                .build();

        ParsedStatement statement = PrivatRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(new BigDecimal("-100.00"), transaction.accountAmount()),
                () -> assertEquals(new BigDecimal("-100.00"), transaction.operationAmount()),
                () -> assertNull(transaction.commissionAmount()),
                () -> assertNull(transaction.commissionCurrencyCode())
        );
    }

    @Test
    @DisplayName("Map Privat transaction with different currencies does not calculate commission")
    void ToParsedStatement_WithDifferentCurrencies_DoesNotCalculateCommission() {
        PrivatRawTransaction rawTransaction = PrivatRawTransaction.builder()
                .transactionDateTime("26.04.2026 13:46:43")
                .bankCategoryName("Покупки")
                .maskedCardNumber("5168 **** **** 1234")
                .description("Оплата в іншій валюті")
                .cardAmount("-101.00")
                .cardCurrency("EUR")
                .operationAmount("-100.00")
                .operationCurrency("USD")
                .balanceAfterTransaction("500.00")
                .balanceCurrency("EUR")
                .build();

        ParsedStatement statement = PrivatRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals(CurrencyCode.USD, transaction.operationCurrencyCode()),
                () -> assertNull(transaction.commissionAmount()),
                () -> assertNull(transaction.commissionCurrencyCode())
        );
    }

    @Test
    @DisplayName("Map Privat transaction with comma decimal amount converts amount correctly")
    void ToParsedStatement_WithCommaDecimalAmount_ConvertsAmountCorrectly() {
        PrivatRawTransaction rawTransaction = PrivatRawTransaction.builder()
                .transactionDateTime("26.04.2026 13:46:43")
                .bankCategoryName("Покупки")
                .maskedCardNumber("5168 **** **** 1234")
                .description("Оплата")
                .cardAmount("-10,50")
                .cardCurrency("EUR")
                .operationAmount("-10,50")
                .operationCurrency("EUR")
                .balanceAfterTransaction("489,50")
                .balanceCurrency("EUR")
                .build();

        ParsedStatement statement = PrivatRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(new BigDecimal("-10.50"), transaction.accountAmount()),
                () -> assertEquals(new BigDecimal("-10.50"), transaction.operationAmount()),
                () -> assertEquals(new BigDecimal("489.50"), transaction.balanceAfterTransaction())
        );
    }
}