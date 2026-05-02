package com.example.spendly.bank.rabo;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.transaction.model.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class RaboPdfStatementParserTests {

    private final RaboPdfStatementParser parser = new RaboPdfStatementParser();

    @Test
    @DisplayName("Parse valid Rabo PDF returns all transaction blocks")
    void Parse_WithValidRaboPdf_ReturnsAllTransactionBlocks() throws Exception {
        ParsedStatement statement = parser.parse(getRaboPdfFile());

        assertAll(
                () -> assertNotNull(statement),
                () -> assertNotNull(statement.transactions()),
                () -> assertEquals(79, statement.transactions().size())
        );
    }

    @Test
    @DisplayName("Parse Rabo PDF debit transaction maps amount as negative")
    void Parse_WithDebitTransaction_MapsAmountAsNegative() throws Exception {
        ParsedStatement statement = parser.parse(getRaboPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 1),
                "POTAE Almere",
                new BigDecimal("-15.97")
        );

        assertAll(
                () -> assertEquals(LocalDate.of(2026, 3, 1), transaction.transactionDate()),
                () -> assertEquals(LocalDate.of(2026, 3, 1), transaction.processingDate()),
                () -> assertEquals("bc", transaction.transactionTypeCode()),
                () -> assertEquals(new BigDecimal("-15.97"), transaction.accountAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertTrue(transaction.description().contains("POTAE Almere"))
        );
    }

    @Test
    @DisplayName("Parse Rabo PDF credit salary transaction maps amount as positive and keeps identifiers")
    void Parse_WithCreditSalaryTransaction_MapsAmountAsPositiveAndKeepsIdentifiers() throws Exception {
        ParsedStatement statement = parser.parse(getRaboPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 4),
                "HEAD Uitzendbureau B.V.",
                new BigDecimal("614.91")
        );

        assertAll(
                () -> assertEquals("sb", transaction.transactionTypeCode()),
                () -> assertEquals(new BigDecimal("614.91"), transaction.accountAmount()),
                () -> assertEquals(new BigDecimal("614.91"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.accountCurrencyCode()),
                () -> assertEquals("OM1T004034952927", transaction.paymentReference()),
                () -> assertEquals("1012411/990/1", transaction.endToEndId()),
                () -> assertTrue(transaction.description().contains("Salaris tm week 9"))
        );
    }

    @Test
    @DisplayName("Parse Rabo PDF transaction with Dutch thousand separator maps amount correctly")
    void Parse_WithDutchThousandSeparator_MapsAmountCorrectly() throws Exception {
        ParsedStatement statement = parser.parse(getRaboPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 12),
                "Nataliia Opara",
                new BigDecimal("-1050.00")
        );

        assertAll(
                () -> assertEquals("bg", transaction.transactionTypeCode()),
                () -> assertEquals(new BigDecimal("-1050.00"), transaction.accountAmount()),
                () -> assertEquals(new BigDecimal("-1050.00"), transaction.operationAmount()),
                () -> assertEquals(CurrencyCode.EUR, transaction.operationCurrencyCode()),
                () -> assertEquals("OO9T005633549534", transaction.paymentReference())
        );
    }

    @Test
    @DisplayName("Parse Rabo PDF transaction continued across page does not include page header in description")
    void Parse_WithTransactionContinuedAcrossPage_DoesNotIncludePageHeaderInDescription() throws Exception {
        ParsedStatement statement = parser.parse(getRaboPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 9),
                "NYX*VendingWork",
                new BigDecimal("-4.25")
        );

        assertAll(
                () -> assertEquals("bc", transaction.transactionTypeCode()),
                () -> assertEquals(new BigDecimal("-4.25"), transaction.accountAmount()),
                () -> assertFalse(transaction.description().contains("Account statement")),
                () -> assertFalse(transaction.description().contains("Rabo Standaard")),
                () -> assertFalse(transaction.description().contains("IBAN / account number")),
                () -> assertFalse(transaction.description().contains("N. Opara"))
        );
    }

    @Test
    @DisplayName("Parse Rabo PDF iDEAL transaction maps payment reference and end to end ID")
    void Parse_WithIdealTransaction_MapsPaymentReferenceAndEndToEndId() throws Exception {
        ParsedStatement statement = parser.parse(getRaboPdfFile());

        ParsedTransaction transaction = findTransaction(
                statement,
                LocalDate.of(2026, 3, 3),
                "Amazon Payments Europe SCA",
                new BigDecimal("-8.98")
        );

        assertAll(
                () -> assertEquals("id", transaction.transactionTypeCode()),
                () -> assertEquals("OO9T005604127122", transaction.paymentReference()),
                () -> assertEquals("03-03-2026 00:23 0220154478884804", transaction.endToEndId()),
                () -> assertTrue(transaction.description().contains("Amazon"))
        );
    }

    private ParsedTransaction findTransaction(
            ParsedStatement statement,
            LocalDate date,
            String descriptionPart,
            BigDecimal amount
    ) {
        return statement.transactions().stream()
                .filter(transaction -> date.equals(transaction.transactionDate()))
                .filter(transaction -> transaction.description() != null)
                .filter(transaction -> transaction.description().contains(descriptionPart))
                .filter(transaction -> amount.compareTo(transaction.accountAmount()) == 0)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Transaction not found: date=" + date
                                + ", descriptionPart=" + descriptionPart
                                + ", amount=" + amount
                ));
    }

    private File getRaboPdfFile() throws Exception {
        return Path.of(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("statements/rabo.pdf"),
                        "Test resource statements/rabo.pdf not found"
                ).toURI()
        ).toFile();
    }
}