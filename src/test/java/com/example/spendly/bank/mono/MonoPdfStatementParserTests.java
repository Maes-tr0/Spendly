package com.example.spendly.bank.mono;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.currency.common.model.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class MonoPdfStatementParserTests {

    private final MonoPdfStatementParser parser = new MonoPdfStatementParser();

    @Test
    @DisplayName("Parse valid Monobank PDF returns parsed statement with all transactions")
    void Parse_WithValidMonobankPdf_ReturnsParsedStatementWithAllTransactions() throws Exception {
        ParsedStatement statement = parser.parse(getMonoPdfFile());

        assertAll(
                () -> assertNotNull(statement),
                () -> assertNotNull(statement.transactions()),
                () -> assertEquals(33, statement.transactions().size())
        );
    }

    @Test
    @DisplayName("Parse Monobank PDF with multiline description joins description correctly")
    void Parse_WithMultilineDescription_JoinsDescriptionCorrectly() throws Exception {
        ParsedStatement statement = parser.parse(getMonoPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 9),
                LocalTime.of(12, 39, 14)
        );

        assertAll(
                () -> assertEquals("Products and supermarkets", transaction.description()),
                () -> assertEquals("Products and supermarkets", transaction.bankCategoryName()),
                () -> assertEquals("5411", transaction.mccCode()),
                () -> assertEquals(new BigDecimal("-6.79"), transaction.accountAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals(new BigDecimal("-6.79"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.operationCurrencyCode()),
                () -> assertEquals(new BigDecimal("3.41"), transaction.cashbackAmount()),
                () -> assertEquals(CurrencyCode.UAH, transaction.cashbackCurrencyCode())
        );
    }

    @Test
    @DisplayName("Parse Monobank PDF with header currencies maps card commission and cashback currencies")
    void Parse_WithHeaderCurrencies_MapsTransactionCurrenciesCorrectly() throws Exception {
        ParsedStatement statement = parser.parse(getMonoPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 9),
                LocalTime.of(20, 46, 9)
        );

        assertAll(
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals(CurrencyCode.EUR, transaction.commissionCurrencyCode()),
                () -> assertEquals(CurrencyCode.UAH, transaction.cashbackCurrencyCode())
        );
    }

    @Test
    @DisplayName("Parse Monobank PDF with amount containing thousand separator maps amount correctly")
    void Parse_WithAmountContainingThousandSeparator_MapsAmountCorrectly() throws Exception {
        ParsedStatement statement = parser.parse(getMonoPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 2, 21),
                LocalTime.of(11, 3, 44)
        );

        assertAll(
                () -> assertEquals("Money transfer", transaction.description()),
                () -> assertEquals(new BigDecimal("-40.00"), transaction.accountAmount()),
                () -> assertEquals(new BigDecimal("-2024.80"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.UAH, transaction.operationCurrencyCode()),
                () -> assertEquals(new BigDecimal("50.62"), transaction.exchangeRate())
        );
    }

    @Test
    @DisplayName("Parse Monobank PDF with dash exchange rate maps exchange rate to null")
    void Parse_WithDashExchangeRate_MapsExchangeRateToNull() throws Exception {
        ParsedStatement statement = parser.parse(getMonoPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 9),
                LocalTime.of(20, 44, 57)
        );

        assertAll(
                () -> assertEquals("Money transfers", transaction.description()),
                () -> assertEquals(CurrencyCode.EUR, transaction.operationCurrencyCode()),
                () -> assertNull(transaction.exchangeRate())
        );
    }

    @Test
    @DisplayName("Parse Monobank PDF with foreign operation currency maps operation currency correctly")
    void Parse_WithForeignOperationCurrency_MapsOperationCurrencyCorrectly() throws Exception {
        ParsedStatement statement = parser.parse(getMonoPdfFile());

        ParsedTransaction thbTransaction = findTransaction(
                statement,
                LocalDate.of(2026, 1, 27),
                LocalTime.of(14, 55, 28)
        );

        ParsedTransaction inrTransaction = findTransaction(
                statement,
                LocalDate.of(2026, 1, 28),
                LocalTime.of(1, 23, 41)
        );

        assertAll(
                () -> assertEquals(CurrencyCode.THB, thbTransaction.operationCurrencyCode()),
                () -> assertEquals(new BigDecimal("-370.00"), thbTransaction.operationAmount()),
                () -> assertEquals(new BigDecimal("36.60"), thbTransaction.exchangeRate()),

                () -> assertEquals(CurrencyCode.INR, inrTransaction.operationCurrencyCode()),
                () -> assertEquals(new BigDecimal("-300.00"), inrTransaction.operationAmount()),
                () -> assertEquals(new BigDecimal("108.70"), inrTransaction.exchangeRate())
        );
    }

    private ParsedTransaction findTransaction(
            ParsedStatement statement,
            LocalDate date,
            LocalTime time
    ) {
        return statement.transactions().stream()
                .filter(transaction -> date.equals(transaction.transactionDate()))
                .filter(transaction -> time.equals(transaction.transactionDateTime()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Transaction not found: date=" + date + ", time=" + time
                ));
    }

    private File getMonoPdfFile() throws Exception {
        return Path.of(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("statements/mono.pdf"),
                        "Test resource statements/mono.pdf not found"
                ).toURI()
        ).toFile();
    }
}