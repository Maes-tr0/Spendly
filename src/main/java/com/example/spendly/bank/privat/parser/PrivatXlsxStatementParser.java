package com.example.spendly.bank.privat.parser;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.bank.common.source.TableStatementSource;
import com.example.spendly.bank.privat.mapper.PrivatRawTransactionMapper;
import com.example.spendly.bank.privat.model.PrivatRawTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class PrivatXlsxStatementParser implements BankStatementParser {

    private final PrivatRawTransactionParser privatRawTransactionParser;
    private final PrivatStatementMetadataParser privatStatementMetadataParser;
    private final PrivatRawTransactionMapper privatRawTransactionMapper;

    @Override
    public BankCode getBankCode() {
        return BankCode.PRIVAT;
    }

    @Override
    public ParsedStatement parse(File file) {
        TableStatementSource source = extractTableSource(file);

        StatementPeriod period = privatStatementMetadataParser.parsePeriod(source);
        StatementBalanceSummary balanceSummary = privatStatementMetadataParser.parseBalanceSummary(source);

        List<PrivatRawTransaction> rawTransactions = privatRawTransactionParser.parse(source, period);

        return privatRawTransactionMapper.toParsedStatement(
                getBankCode(),
                period,
                balanceSummary,
                rawTransactions
        );
    }

    private TableStatementSource extractTableSource(File file) {
        try (
                FileInputStream inputStream = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            List<List<String>> rows = new ArrayList<>();

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    rows.add(List.of());
                    continue;
                }

                List<String> values = new ArrayList<>();

                int lastCellIndex = Math.max(row.getLastCellNum(), 10);

                for (int cellIndex = 0; cellIndex < lastCellIndex; cellIndex++) {
                    values.add(getCellValue(row, cellIndex, formatter));
                }

                rows.add(values);
            }

            return new TableStatementSource(rows);

        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("Cannot parse %s XLSX statement", getBankCode().getBankName()),
                    e
            );
        }
    }

    private String getCellValue(Row row, int columnIndex, DataFormatter formatter) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }
}