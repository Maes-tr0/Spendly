package com.example.spendly.bank.mono.parser;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.bank.common.source.PdfTextExtractor;
import com.example.spendly.bank.common.source.TextStatementSource;
import com.example.spendly.bank.mono.mapper.MonoRawTransactionMapper;
import com.example.spendly.bank.mono.model.MonoRawTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
@Component
public class MonoPdfStatementParser implements BankStatementParser {

    private final MonoRawTransactionParser monoRawTransactionParser;
    private final MonoStatementMetadataParser monoStatementMetadataParser;
    private final MonoRawTransactionMapper monoRawTransactionMapper;
    private final PdfTextExtractor pdfTextExtractor;

    @Override
    public BankCode getBankCode() {
        return BankCode.MONO;
    }

    @Override
    public ParsedStatement parse(File file) {
        TextStatementSource source = pdfTextExtractor.extract(file, getBankCode());

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
}