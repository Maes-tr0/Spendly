package com.example.spendly.bank.rabo.parser;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.parser.BankStatementParser;
import com.example.spendly.bank.common.source.PdfTextExtractor;
import com.example.spendly.bank.common.source.TextStatementSource;
import com.example.spendly.bank.rabo.mapper.RaboRawTransactionMapper;
import com.example.spendly.bank.rabo.model.RaboRawTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RaboPdfStatementParser implements BankStatementParser {

    private final RaboRawTransactionParser raboRawTransactionParser;
    private final RaboStatementMetadataParser raboStatementMetadataParser;
    private final RaboRawTransactionMapper raboRawTransactionMapper;
    private final PdfTextExtractor pdfTextExtractor;

    @Override
    public BankCode getBankCode() {
        return BankCode.RABO;
    }

    @Override
    public ParsedStatement parse(File file) {
        TextStatementSource source = pdfTextExtractor.extract(file, getBankCode());

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
}