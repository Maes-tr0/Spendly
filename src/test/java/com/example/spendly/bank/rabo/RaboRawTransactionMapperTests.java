package com.example.spendly.bank.rabo;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.currency.common.model.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RaboRawTransactionMapperTests {

    @Test
    @DisplayName("Map Rabo debit transaction converts Dutch amount format and makes amount negative")
    void ToParsedStatement_WithDebitAmount_ConvertsAmountAndMakesNegative() {
        RaboRawTransaction rawTransaction = new RaboRawTransaction(
                "12-03-2026",
                "bg",
                "GB85 CLJU 0099 7191 7282 86",
                "Nataliia Opara",
                "1.050,00",
                null,
                "12-03-2026",
                "OO9T005633549534",
                null,
                "EUR"
        );

        ParsedStatement statement = RaboRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(LocalDate.of(2026, 3, 12), transaction.transactionDate()),
                () -> assertEquals(LocalDate.of(2026, 3, 12), transaction.processingDate()),
                () -> assertEquals(new BigDecimal("-1050.00"), transaction.accountAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals(new BigDecimal("-1050.00"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.operationCurrencyCode()),
                () -> assertEquals("Nataliia Opara", transaction.description()),
                () -> assertEquals("bg", transaction.transactionTypeCode()),
                () -> assertEquals("OO9T005633549534", transaction.paymentReference())
        );
    }

    @Test
    @DisplayName("Map Rabo credit transaction converts amount and keeps amount positive")
    void ToParsedStatement_WithCreditAmount_ConvertsAmountAndKeepsPositive() {
        RaboRawTransaction rawTransaction = new RaboRawTransaction(
                "04-03-2026",
                "sb",
                "NL32 RABO 0132 4490 99",
                "HEAD Uitzendbureau B.V. Salaris tm week 9 / Kenmerk: 12411",
                null,
                "614,91",
                "04-03-2026",
                "OM1T004034952927",
                "1012411/990/1",
                "EUR"
        );

        ParsedStatement statement = RaboRawTransactionMapper.toParsedStatement(List.of(rawTransaction));

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(LocalDate.of(2026, 3, 4), transaction.transactionDate()),
                () -> assertEquals(new BigDecimal("614.91"), transaction.accountAmount()),
                () -> assertEquals(new BigDecimal("614.91"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals("sb", transaction.transactionTypeCode()),
                () -> assertEquals("OM1T004034952927", transaction.paymentReference()),
                () -> assertEquals("1012411/990/1", transaction.endToEndId())
        );
    }
}