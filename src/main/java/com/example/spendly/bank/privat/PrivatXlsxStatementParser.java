package com.example.spendly.bank.privat;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.statement.model.ParsedStatement;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrivatXlsxStatementParser implements BankStatementParser {

    private static final int FIRST_TRANSACTION_ROW_INDEX = 2;
    private static final int TRANSACTION_DATE_TIME_COLUMN = 0;
    private static final int BANK_CATEGORY_NAME_COLUMN = 1;
    private static final int MASKED_CARD_NUMBER_COLUMN = 2;
    private static final int DESCRIPTION_COLUMN = 3;
    private static final int CARD_AMOUNT_COLUMN = 4;
    private static final int CARD_CURRENCY_COLUMN = 5;
    private static final int OPERATION_AMOUNT_COLUMN = 6;
    private static final int OPERATION_CURRENCY_COLUMN = 7;
    private static final int BALANCE_AFTER_TRANSACTION_COLUMN = 8;
    private static final int BALANCE_CURRENCY_COLUMN = 9;

    @Override
    public ParsedStatement parse(File file) {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(file))) {

            Sheet sheet = workbook.getSheetAt(0);

            List<PrivatRawTransaction> rawTransactions = getPrivatRawTransactions(sheet);

            return PrivatRawTransactionMapper.toParsedStatement(rawTransactions);

        } catch (IOException e) {
            throw new RuntimeException("Cannot parse PrivatBank XLSX statement", e);
        }
    }

    private List<PrivatRawTransaction> getPrivatRawTransactions(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        List<PrivatRawTransaction> transactions = new ArrayList<>();

        for (int i = FIRST_TRANSACTION_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            String transactionDateTime = getCellValue(row, TRANSACTION_DATE_TIME_COLUMN, formatter);

            if (transactionDateTime.isBlank()) {
                continue;
            }

            transactions.add(mapRowToPrivatRawTransaction(row, formatter));
        }

        return transactions;
    }

    private PrivatRawTransaction mapRowToPrivatRawTransaction(Row row, DataFormatter formatter) {
        return new PrivatRawTransaction(
                getCellValue(row, TRANSACTION_DATE_TIME_COLUMN, formatter),
                getCellValue(row, BANK_CATEGORY_NAME_COLUMN, formatter),
                getCellValue(row, MASKED_CARD_NUMBER_COLUMN, formatter),
                getCellValue(row, DESCRIPTION_COLUMN, formatter),

                getCellValue(row, CARD_AMOUNT_COLUMN, formatter),
                getCellValue(row, CARD_CURRENCY_COLUMN, formatter),

                getCellValue(row, OPERATION_AMOUNT_COLUMN, formatter),
                getCellValue(row, OPERATION_CURRENCY_COLUMN, formatter),

                getCellValue(row, BALANCE_AFTER_TRANSACTION_COLUMN, formatter),
                getCellValue(row, BALANCE_CURRENCY_COLUMN, formatter)
        );
    }

    private String getCellValue(Row row, int columnIndex, DataFormatter formatter) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }


    void main() {

        ParsedStatement parsedStatement = parse(new File("src/main/resources/statements/privat.xlsx"));

        for (ParsedTransaction transaction : parsedStatement.transactions()) {
            System.out.println(transaction);
        }

    }
}
