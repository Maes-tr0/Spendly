package com.example.spendly.bank.common.parser;

import com.example.spendly.statement.model.ParsedStatement;

import java.io.File;

public interface BankStatementParser {
    ParsedStatement parse(File file);
}
