package com.example.spendly.bank.privat;

import com.example.spendly.bank.common.model.ParsedTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.transaction.model.CurrencyCode;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrivatXlsxStatementParserTests {

    private final PrivatXlsxStatementParser parser = new PrivatXlsxStatementParser();

    @Test
    @DisplayName("Parse Privat XLSX with valid transaction returns parsed statement")
    void Parse_WithValidPrivatXlsx_ReturnsParsedStatement() throws Exception {
        File file = createPrivatXlsx(
                new String[]{
                        "26.04.2026 13:46:43",
                        "Перекази",
                        "5168 **** **** 1234",
                        "Переказ між картками",
                        "-101.00",
                        "EUR",
                        "-100.00",
                        "EUR",
                        "500.00",
                        "EUR"
                }
        );

        ParsedStatement statement = parser.parse(file);

        ParsedTransaction transaction = statement.transactions().getFirst();

        assertAll(
                () -> assertEquals(1, statement.transactions().size()),
                () -> assertEquals(LocalDate.of(2026, 4, 26), transaction.transactionDate()),
                () -> assertEquals(LocalTime.of(13, 46, 43), transaction.transactionDateTime()),
                () -> assertEquals("Переказ між картками", transaction.description()),
                () -> assertEquals("Перекази", transaction.bankCategoryName()),
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
    @DisplayName("Parse Privat XLSX with empty rows skips empty rows")
    void Parse_WithEmptyRows_SkipsEmptyRows() throws Exception {
        File file = createPrivatXlsxWithEmptyRow();

        ParsedStatement statement = parser.parse(file);

        assertEquals(1, statement.transactions().size());
    }

    private File createPrivatXlsx(String[] transactionValues) throws Exception {
        File file = Files.createTempFile("privat-test-", ".xlsx").toFile();

        try (
                Workbook workbook = new XSSFWorkbook();
                FileOutputStream outputStream = new FileOutputStream(file)
        ) {
            Sheet sheet = workbook.createSheet("Statement");

            sheet.createRow(0).createCell(0).setCellValue("Службовий рядок");

            Row header = sheet.createRow(1);
            header.createCell(0).setCellValue("Дата");
            header.createCell(1).setCellValue("Категорія");
            header.createCell(2).setCellValue("Картка");
            header.createCell(3).setCellValue("Опис операції");
            header.createCell(4).setCellValue("Сума в валюті картки");
            header.createCell(5).setCellValue("Валюта картки");
            header.createCell(6).setCellValue("Сума в валюті транзакції");
            header.createCell(7).setCellValue("Валюта транзакції");
            header.createCell(8).setCellValue("Залишок");
            header.createCell(9).setCellValue("Валюта залишку");

            Row transaction = sheet.createRow(2);

            for (int i = 0; i < transactionValues.length; i++) {
                transaction.createCell(i).setCellValue(transactionValues[i]);
            }

            workbook.write(outputStream);
        }

        return file;
    }

    private File createPrivatXlsxWithEmptyRow() throws Exception {
        File file = Files.createTempFile("privat-empty-row-test-", ".xlsx").toFile();

        try (
                Workbook workbook = new XSSFWorkbook();
                FileOutputStream outputStream = new FileOutputStream(file)
        ) {
            Sheet sheet = workbook.createSheet("Statement");

            sheet.createRow(0).createCell(0).setCellValue("Службовий рядок");

            Row header = sheet.createRow(1);
            header.createCell(0).setCellValue("Дата");
            header.createCell(1).setCellValue("Категорія");
            header.createCell(2).setCellValue("Картка");
            header.createCell(3).setCellValue("Опис операції");
            header.createCell(4).setCellValue("Сума в валюті картки");
            header.createCell(5).setCellValue("Валюта картки");
            header.createCell(6).setCellValue("Сума в валюті транзакції");
            header.createCell(7).setCellValue("Валюта транзакції");
            header.createCell(8).setCellValue("Залишок");
            header.createCell(9).setCellValue("Валюта залишку");

            sheet.createRow(2);

            Row transaction = sheet.createRow(3);
            transaction.createCell(0).setCellValue("26.04.2026 13:46:43");
            transaction.createCell(1).setCellValue("Перекази");
            transaction.createCell(2).setCellValue("5168 **** **** 1234");
            transaction.createCell(3).setCellValue("Переказ між картками");
            transaction.createCell(4).setCellValue("-101.00");
            transaction.createCell(5).setCellValue("EUR");
            transaction.createCell(6).setCellValue("-100.00");
            transaction.createCell(7).setCellValue("EUR");
            transaction.createCell(8).setCellValue("500.00");
            transaction.createCell(9).setCellValue("EUR");

            workbook.write(outputStream);
        }

        return file;
    }
}