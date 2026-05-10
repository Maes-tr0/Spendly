package com.example.spendly.bank.rabo;

import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.currency.common.model.CurrencyCode;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RaboPdfStatementParser implements BankStatementParser {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter VALUE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM");

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

    private static final Pattern FULL_DATE_PATTERN = Pattern.compile(
            "\\b\\d{2}-\\d{2}-\\d{4}\\b"
    );

    private static final Pattern BALANCE_VALUE_PATTERN = Pattern.compile(
            "\\b(" + AMOUNT_REGEX + ")\\s+(CR|D)\\b"
    );

    @Override
    public ParsedStatement parse(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String rawText = stripper.getText(document);

            String statementHeader = extractStatementHeader(rawText);

            StatementPeriod period = parseStatementPeriod(statementHeader);
            StatementBalanceSummary balanceSummary = parseStatementBalanceSummary(rawText, statementHeader);

            List<RaboRawTransaction> rawTransactions = getRaboRawTransactions(rawText, period);

            return RaboRawTransactionMapper.toParsedStatement(
                    period,
                    balanceSummary,
                    rawTransactions
            );

        } catch (IOException e) {
            throw new RuntimeException("Cannot parse Rabobank PDF statement", e);
        }
    }

    private String extractStatementHeader(String rawText) {
        List<String> lines = getNormalizedLines(rawText);

        StringBuilder header = new StringBuilder();

        for (String line : lines) {
            if (isTransactionStartLine(line)) {
                break;
            }

            header.append(line).append("\n");
        }

        if (header.isEmpty()) {
            throw new IllegalArgumentException("Cannot extract Rabobank statement header");
        }

        return header.toString();
    }

    private StatementPeriod parseStatementPeriod(String statementHeader) {
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

        throw new IllegalArgumentException("Cannot parse Rabobank statement period from header: " + statementHeader);
    }

    private List<LocalDate> extractFullDates(String text) {
        Matcher matcher = FULL_DATE_PATTERN.matcher(text);

        List<LocalDate> dates = new ArrayList<>();

        while (matcher.find()) {
            dates.add(LocalDate.parse(matcher.group(), DATE_FORMATTER));
        }

        return dates;
    }

    private StatementBalanceSummary parseStatementBalanceSummary(
            String rawText,
            String statementHeader
    ) {
        List<BalanceValue> balances = extractBalanceValues(statementHeader);

        if (balances.size() < 2) {
            throw new IllegalArgumentException("Cannot parse Rabobank opening and closing balance from header: " + statementHeader);
        }

        CurrencyCode accountCurrency = CurrencyCode.fromString(extractAccountCurrency(rawText));

        return new StatementBalanceSummary(
                balances.get(0).amount(),
                balances.get(1).amount(),
                accountCurrency
        );
    }

    private List<BalanceValue> extractBalanceValues(String text) {
        Matcher matcher = BALANCE_VALUE_PATTERN.matcher(text);

        List<BalanceValue> balances = new ArrayList<>();

        while (matcher.find()) {
            BigDecimal amount = parseAmount(matcher.group(1));
            String type = matcher.group(2);

            if ("D".equals(type)) {
                amount = amount.negate();
            }

            balances.add(new BalanceValue(amount, type));
        }

        return balances;
    }

    private List<RaboRawTransaction> getRaboRawTransactions(
            String rawText,
            StatementPeriod period
    ) {
        String accountCurrency = extractAccountCurrency(rawText);

        List<String> blocks = getRawTransactionBlocks(rawText);

        List<RaboRawTransaction> transactions = new ArrayList<>();

        for (String block : blocks) {
            transactions.add(mapBlockToRaboRawTransaction(block, period, accountCurrency));
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
            StatementPeriod period,
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

        String valueDate = resolveFullValueDate(startMatcher.group(1), period);
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

    private String resolveFullValueDate(String valueDateWithoutYear, StatementPeriod period) {
        MonthDay valueMonthDay = MonthDay.parse(valueDateWithoutYear, VALUE_DATE_FORMATTER);

        LocalDate valueDate = valueMonthDay.atYear(period.from().getYear());

        if (valueDate.isBefore(period.from())) {
            valueDate = valueMonthDay.atYear(period.to().getYear());
        }

        return valueDate.format(DATE_FORMATTER);
    }

    private String extractAccountCurrency(String rawText) {
        Matcher matcher = ACCOUNT_CURRENCY_PATTERN.matcher(rawText);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot find Rabobank account currency");
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
                || line.equals("Previous balance")
                || line.equals("Closing balance")
                || line.equals("Statement creation date")
                || line.equals("BIC")
                || line.equals("RABONL2U")
                || line.equals("Total amount debited")
                || line.equals("Total amount credited")
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
                || line.matches("^" + AMOUNT_REGEX + "\\s+(CR|D)$")
                || line.matches("^" + AMOUNT_REGEX + "$")
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

    private record BalanceValue(
            BigDecimal amount,
            String type
    ) {
    }
}