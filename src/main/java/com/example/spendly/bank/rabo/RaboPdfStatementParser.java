package com.example.spendly.bank.rabo;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.statement.model.ParsedStatement;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RaboPdfStatementParser implements BankStatementParser {

    private static final String AMOUNT_REGEX =
            "\\d{1,3}(?:\\.\\d{3})*,\\d{2}|\\d+,\\d{2}";

    private static final Pattern TRANSACTION_START_PATTERN = Pattern.compile(
            "^(\\d{2}-\\d{2})\\s+" +
                    "([a-z]{2})\\s+" +
                    "(.+?)\\s+" +
                    "(" + AMOUNT_REGEX + ")$"
    );

    private static final Pattern PROCESSING_DATE_PATTERN = Pattern.compile(
            "^Processing date:\\s+(\\d{2}-\\d{2}-\\d{4})$"
    );

    private static final Pattern PAYMENT_REFERENCE_PATTERN = Pattern.compile(
            "^Payment ref\\.\\s+(.+)$"
    );

    private static final Pattern END_TO_END_ID_PATTERN = Pattern.compile(
            "^End-to-End ID:\\s*(.*)$"
    );

    private static final Pattern COUNTERPARTY_ACCOUNT_PATTERN = Pattern.compile(
            "^([A-Z]{2}\\d{2}\\s+[A-Z]{4}(?:\\s+\\d{2,4}){2,4})\\s*(.*)$"
    );

    private static final Pattern ACCOUNT_CURRENCY_PATTERN = Pattern.compile(
            "\\bNL\\d{2}\\s+RABO(?:\\s+\\d{4}){2}\\s+\\d{2}\\s+([A-Z]{3})\\b"
    );

    private static final Pattern STATEMENT_YEAR_PATTERN = Pattern.compile(
            "\\b\\d{2}-\\d{2}-(\\d{4})\\b"
    );

    @Override
    public ParsedStatement parse(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String rawText = stripper.getText(document);

            List<RaboRawTransaction> rawTransactions = getRaboRawTransactions(rawText);

            return RaboRawTransactionMapper.toParsedStatement(rawTransactions);

        } catch (IOException e) {
            throw new RuntimeException("Cannot parse Rabobank PDF statement", e);
        }
    }

    private List<RaboRawTransaction> getRaboRawTransactions(String rawText) {
        String accountCurrency = extractAccountCurrency(rawText);
        String statementYear = extractStatementYear(rawText);

        List<String> blocks = getRawTransactionBlocks(rawText);

        List<RaboRawTransaction> transactions = new ArrayList<>();

        for (String block : blocks) {
            transactions.add(mapBlockToRaboRawTransaction(block, statementYear, accountCurrency));
        }

        return transactions;
    }

    private List<String> getRawTransactionBlocks(String rawText) {
        List<String> transactions = new ArrayList<>();

        String[] lines = rawText.split("\\R");

        StringBuilder currentTransaction = null;

        for (String rawLine : lines) {
            String line = normalizeLine(rawLine);

            if (line.isBlank()) {
                continue;
            }

            if (isTransactionStartLine(line)) {
                if (currentTransaction != null) {
                    throw new IllegalArgumentException(
                            "Previous Rabobank transaction was not closed by processing date: "
                                    + currentTransaction
                    );
                }

                currentTransaction = new StringBuilder(line);
                continue;
            }

            if (currentTransaction == null) {
                continue;
            }

            currentTransaction.append("\n").append(line);

            if (isProcessingDateLine(line)) {
                transactions.add(currentTransaction.toString());
                currentTransaction = null;
            }
        }

        if (currentTransaction != null) {
            throw new IllegalArgumentException(
                    "Last Rabobank transaction was not closed by processing date: "
                            + currentTransaction
            );
        }

        return transactions;
    }

    private RaboRawTransaction mapBlockToRaboRawTransaction(
            String block,
            String statementYear,
            String accountCurrency
    ) {
        String[] lines = block.split("\\R");

        if (lines.length == 0) {
            throw new IllegalArgumentException("Empty Rabobank transaction block");
        }

        Matcher startMatcher = TRANSACTION_START_PATTERN.matcher(lines[0]);

        if (!startMatcher.matches()) {
            throw new IllegalArgumentException("Cannot parse Rabobank transaction start line: " + lines[0]);
        }

        String valueDate = startMatcher.group(1) + "-" + statementYear;
        String transactionTypeCode = startMatcher.group(2);
        String firstDescriptionPart = startMatcher.group(3);
        String amount = startMatcher.group(4);

        String counterpartyAccount = null;

        Matcher accountMatcher = COUNTERPARTY_ACCOUNT_PATTERN.matcher(firstDescriptionPart);

        if (accountMatcher.matches()) {
            counterpartyAccount = normalizeLine(accountMatcher.group(1));
            firstDescriptionPart = normalizeLine(accountMatcher.group(2));
        }

        String paymentReference = null;
        String endToEndId = null;
        String processingDate = null;

        boolean nextLineIsEndToEndId = false;

        List<String> descriptionParts = new ArrayList<>();

        if (!firstDescriptionPart.isBlank()) {
            descriptionParts.add(firstDescriptionPart);
        }

        for (int i = 1; i < lines.length; i++) {
            String line = normalizeLine(lines[i]);

            if (line.isBlank()) {
                continue;
            }

            Matcher processingDateMatcher = PROCESSING_DATE_PATTERN.matcher(line);
            if (processingDateMatcher.matches()) {
                processingDate = processingDateMatcher.group(1);
                nextLineIsEndToEndId = false;
                continue;
            }

            Matcher paymentReferenceMatcher = PAYMENT_REFERENCE_PATTERN.matcher(line);
            if (paymentReferenceMatcher.matches()) {
                paymentReference = paymentReferenceMatcher.group(1).trim();
                continue;
            }

            Matcher endToEndMatcher = END_TO_END_ID_PATTERN.matcher(line);
            if (endToEndMatcher.matches()) {
                String value = endToEndMatcher.group(1).trim();

                if (value.isBlank()) {
                    nextLineIsEndToEndId = true;
                } else {
                    endToEndId = value;
                }

                continue;
            }

            if (nextLineIsEndToEndId) {
                endToEndId = line;
                nextLineIsEndToEndId = false;
                continue;
            }

            if (isNoiseLine(line) || isCardTechnicalLine(line)) {
                continue;
            }

            descriptionParts.add(line);
        }

        if (processingDate == null) {
            throw new IllegalArgumentException("Cannot find processing date in Rabobank transaction block: " + block);
        }

        String description = String.join(" ", descriptionParts)
                .replaceAll("\\s+", " ")
                .trim();

        String debitAmount = null;
        String creditAmount = null;

        if (isCreditTransactionType(transactionTypeCode)) {
            creditAmount = amount;
        } else {
            debitAmount = amount;
        }

        return new RaboRawTransaction(
                valueDate,
                transactionTypeCode,
                counterpartyAccount,
                description,

                debitAmount,
                creditAmount,

                processingDate,
                paymentReference,
                endToEndId,

                accountCurrency
        );
    }

    private String extractAccountCurrency(String rawText) {
        Matcher matcher = ACCOUNT_CURRENCY_PATTERN.matcher(rawText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot find Rabobank account currency");
        }

        return matcher.group(1);
    }

    private String extractStatementYear(String rawText) {
        Matcher matcher = STATEMENT_YEAR_PATTERN.matcher(rawText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot find Rabobank statement year");
        }

        return matcher.group(1);
    }

    private boolean isTransactionStartLine(String line) {
        return TRANSACTION_START_PATTERN.matcher(line).matches();
    }

    private boolean isProcessingDateLine(String line) {
        return PROCESSING_DATE_PATTERN.matcher(line).matches();
    }

    private boolean isCreditTransactionType(String transactionTypeCode) {
        return switch (transactionTypeCode) {
            case "sb", "cb", "st", "te" -> true;
            default -> false;
        };
    }

    private boolean isCardTechnicalLine(String line) {
        return line.startsWith(".")
                || line.startsWith("4xxxx")
                || line.startsWith("xxx");
    }

    private boolean isNoiseLine(String line) {
        return line.equals("Page")
                || line.matches("^\\d+ of \\d+$")
                || line.equals("Account statement")
                || line.equals("Account statement continued")
                || line.equals("Rabo Standaard")
                || line.equals("In the name of")
                || line.equals("N. Opara")
                || line.equals("IBAN / account number")
                || line.equals("From (date)")
                || line.equals("To (date)")
                || line.equals("Value")
                || line.equals("date")
                || line.equals("Value date")
                || line.startsWith("Type Counterparty account")
                || line.startsWith("Value date Type Counterparty account")
                || line.equals("Koopmanstraat 7")
                || line.equals("km 211")
                || line.equals("1315 HD ALMERE")
                || line.matches("^NL\\d{2}\\s+RABO(?:\\s+\\d{4}){2}\\s+\\d{2}\\s+[A-Z]{3}$")
                || line.matches("^\\d{2}-\\d{2}-\\d{4}$")
                || line.equals("CR = credit")
                || line.equals("D = debit")
                || line.matches("^[a-z]{2}\\s+=\\s+.+");
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

    void main() {
        ParsedStatement parsedStatement = parse(new File("src/main/resources/statements/rabo.pdf"));

        for (ParsedTransaction transaction : parsedStatement.transactions()) {
            System.out.println(transaction);
        }
    }
}