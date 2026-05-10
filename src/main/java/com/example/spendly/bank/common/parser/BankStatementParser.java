package com.example.spendly.bank.common.parser;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.statement.model.ParsedStatement;

import java.io.File;

/**
 * Main contract for parsing a bank statement file.
 * <p>
 * Each implementation is responsible for one specific bank and file format,
 * for example Monobank PDF, PrivatBank XLSX, or Rabobank PDF.
 */
public interface BankStatementParser {

    /**
     * Returns the bank code supported by this parser.
     *
     * @return bank code of the parser implementation
     */
    BankCode getBankCode();

    /**
     * Parses the provided bank statement file into a normalized parsed statement model.
     *
     * @param file source bank statement file
     * @return parsed statement with metadata and transactions
     */
    ParsedStatement parse(File file);
}