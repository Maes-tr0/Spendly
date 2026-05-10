package com.example.spendly.bank.rabo.parser;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.bank.common.source.TextStatementSource;
import com.example.spendly.bank.rabo.mapper.RaboRawTransactionMapper;
import com.example.spendly.bank.rabo.model.RaboRawTransaction;
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
public class RaboPdfStatementParser implements BankStatementParser {

    private final RaboRawTransactionParser raboRawTransactionParser;
    private final RaboStatementMetadataParser raboStatementMetadataParser;
    private final RaboRawTransactionMapper raboRawTransactionMapper;

    @Override
    public BankCode getBankCode() {
        return BankCode.RABO;
    }

    @Override
    public ParsedStatement parse(File file) {
        TextStatementSource source = new TextStatementSource(extractText(file));

        StatementPeriod period = raboStatementMetadataParser.parsePeriod(source);
        StatementBalanceSummary balanceSummary = raboStatementMetadataParser.parseBalanceSummary(source);

        List<RaboRawTransaction> rawTransactions = raboRawTransactionParser.parse(source, period);

        return raboRawTransactionMapper.toParsedStatement(
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