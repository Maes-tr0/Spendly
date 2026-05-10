package com.example.spendly.bank.mono.parser;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.bank.common.source.TextStatementSource;
import com.example.spendly.bank.mono.mapper.MonoRawTransactionMapper;
import com.example.spendly.bank.mono.model.MonoRawTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class MonoPdfStatementParser implements BankStatementParser {

    private final MonoRawTransactionParser monoRawTransactionParser;
    private final MonoStatementMetadataParser monoStatementMetadataParser;
    private final MonoRawTransactionMapper monoRawTransactionMapper;

    @Override
    public BankCode getBankCode() {
        return BankCode.MONO;
    }

    @Override
    public ParsedStatement parse(File file) {
        TextStatementSource source = new TextStatementSource(extractText(file));

        StatementPeriod period = monoStatementMetadataParser.parsePeriod(source);
        StatementBalanceSummary balanceSummary = monoStatementMetadataParser.parseBalanceSummary(source);

        List<MonoRawTransaction> rawTransactions = monoRawTransactionParser.parse(source, period);

        return monoRawTransactionMapper.toParsedStatement(
                getBankCode(),
                period,
                balanceSummary,
                rawTransactions
        );
    }

    private String extractText(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            return stripper.getText(document);

        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("Cannot parse %s PDF statement", getBankCode()),
                    e
            );
        }
    }
}