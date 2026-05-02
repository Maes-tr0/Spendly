package com.example.spendly.bank.mono;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.statement.model.ParsedStatement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonoPdfStatementParser implements BankStatementParser {

    private static final Pattern HEADER_CURRENCY_PATTERN = Pattern.compile("\\(([A-Z]{3})\\)");

    private static final Pattern TRANSACTION_DATE_PATTERN = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");

    private static final Pattern TRANSACTION_PATTERN = Pattern.compile(
            "^(\\d{2}\\.\\d{2}\\.\\d{4})\\s+" +         // 1 transactionDate
                    "(\\d{2}:\\d{2}:\\d{2})\\s+" +              // 2 transactionTime
                    "(.+?)\\s+" +                               // 3 description
                    "(\\d{4})\\s+" +                            // 4 mccCode
                    "(-?\\d+(?:\\s\\d{3})*\\.\\d{2})\\s+" +     // 5 cardAmount
                    "(-?\\d+(?:\\s\\d{3})*\\.\\d{2})\\s+" +     // 6 operationAmount
                    "([A-Z]{3})\\s+" +                          // 7 operationCurrency
                    "(—|-?\\d+(?:\\s\\d{3})*\\.\\d{2})\\s+" +   // 8 exchangeRate
                    "(-?\\d+(?:\\s\\d{3})*\\.\\d{2})\\s+" +     // 9 commissionAmount
                    "(-?\\d+(?:\\s\\d{3})*\\.\\d{2})\\s+" +     // 10 cashbackAmount
                    "(-?\\d+(?:\\s\\d{3})*\\.\\d{2})$"          // 11 balanceAfterTransaction
    );

    private String cardCurrency;
    private String commissionCurrency;
    private String cashbackCurrency;

    @Override
    public ParsedStatement parse(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String rawText = stripper.getText(document);

            List<MonoRawTransaction> rawTransactions = getMonoRawTransactions(rawText);

            return MonoRawTransactionMapper.toParsedStatement(rawTransactions);

        } catch (IOException e) {
            throw new RuntimeException("Cannot parse Monobank PDF statement", e);
        }
    }

    private List<MonoRawTransaction> getMonoRawTransactions(String rawText) {
        int begin = rawText.indexOf("Date and time");
        int end = rawText.indexOf("Operating Director");

        if (begin == -1) {
            throw new IllegalArgumentException("Cannot find Monobank transaction table start");
        }

        if (end == -1) {
            throw new IllegalArgumentException("Cannot read Monobank transaction table");
        }

        String rawTable = rawText.substring(begin, end);

        extractTableCurrencies(rawTable);

        List<String> rows = normalizeTransactionRows(rawTable);

        List<MonoRawTransaction> transactions = new ArrayList<>();

        for (String row : rows) {
            transactions.add(mapRowToMonoRawTransaction(row));
        }

        return transactions;
    }

    private MonoRawTransaction mapRowToMonoRawTransaction(String row) {
        Matcher matcher = TRANSACTION_PATTERN.matcher(row);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse Monobank transaction row: " + row);
        }

        return new MonoRawTransaction(
                matcher.group(1),        // transactionDate
                matcher.group(2),        // transactionTime
                matcher.group(3),        // description
                matcher.group(4),        // mccCode

                matcher.group(5),        // cardAmount
                cardCurrency,            // cardCurrency

                matcher.group(6),        // operationAmount
                matcher.group(7),        // operationCurrency

                matcher.group(8),        // exchangeRate

                matcher.group(9),        // commissionAmount
                commissionCurrency,      // commissionCurrency

                matcher.group(10),       // cashbackAmount
                cashbackCurrency,        // cashbackCurrency

                matcher.group(11)        // balanceAfterTransaction
        );
    }

    private List<String> normalizeTransactionRows(String table) {
        final List<String> transactions = new ArrayList<>();

        String[] lines = table.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            String firstLine = lines[i].trim();

            if (!isTransactionStartLine(firstLine)) {
                continue;
            }

            int secondLineIndex = i + 1;

            while (secondLineIndex < lines.length && lines[secondLineIndex].isBlank()) {
                secondLineIndex++;
            }

            if (secondLineIndex >= lines.length) {
                throw new IllegalArgumentException("Cannot find transaction time line");
            }

            String secondLine = lines[secondLineIndex].trim();

            if (!isTransactionTimeLine(secondLine)) {
                throw new IllegalArgumentException("Cannot parse transaction time line: " + secondLine);
            }

            transactions.add(normalizeTransaction(firstLine, secondLine));

            i = secondLineIndex;
        }

        return transactions;
    }

    private boolean isTransactionStartLine(String line) {
        return line.matches("^\\d{2}\\.\\d{2}\\.\\d{4}\\s+.+");
    }

    private boolean isTransactionTimeLine(String line) {
        return line.matches("^\\d{2}:\\d{2}:\\d{2}.*");
    }

    private String normalizeTransaction(String firstLine, String secondLine) {
        String[] firstParts = firstLine.split("\\s+");

        String date = firstParts[0];

        int mccIndex = -1;

        for (int i = 1; i < firstParts.length; i++) {
            if (firstParts[i].matches("\\d{4}")) {
                mccIndex = i;
                break;
            }
        }

        if (mccIndex == -1) {
            throw new IllegalArgumentException("Cannot find MCC in line: " + firstLine);
        }

        String[] secondParts = secondLine.split("\\s+", 2);

        String time = secondParts[0];
        String descriptionTail = secondParts.length > 1 ? secondParts[1] : "";

        StringBuilder description = new StringBuilder();

        for (int i = 1; i < mccIndex; i++) {
            description.append(firstParts[i]).append(" ");
        }

        if (!descriptionTail.isBlank()) {
            description.append(descriptionTail).append(" ");
        }

        StringBuilder tail = new StringBuilder();

        for (int i = mccIndex; i < firstParts.length; i++) {
            tail.append(firstParts[i]).append(" ");
        }

        return (date + " " + time + " " + description + tail)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int findFirstTransactionIndex(String rawTable) {
        Matcher matcher = TRANSACTION_DATE_PATTERN.matcher(rawTable);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot find first transaction in Monobank table");
        }

        return matcher.start();
    }

    private void extractTableCurrencies(String rawTable) {
        int firstTransactionIndex = findFirstTransactionIndex(rawTable);

        String header = rawTable.substring(0, firstTransactionIndex);

        Matcher matcher = HEADER_CURRENCY_PATTERN.matcher(header);

        List<String> currencies = new ArrayList<>();

        while (matcher.find()) {
            currencies.add(matcher.group(1));
        }

        if (currencies.size() < 3) {
            throw new IllegalArgumentException("Cannot detect Monobank table currencies");
        }

        cardCurrency = currencies.get(0);
        commissionCurrency = currencies.get(1);
        cashbackCurrency = currencies.get(2);
    }

    void main() {

        ParsedStatement parsedStatement = parse(new File("src/main/resources/statements/mono.pdf"));

        for (ParsedTransaction transaction : parsedStatement.transactions()) {
            System.out.println(transaction);
        }
    }

}
