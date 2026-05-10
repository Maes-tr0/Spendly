package com.example.spendly.bank.common.parser;

import com.example.spendly.bank.common.model.BankRawTransaction;
import com.example.spendly.bank.common.source.StatementSource;
import com.example.spendly.statement.model.StatementPeriod;

import java.util.List;

/**
 * Contract for extracting bank-specific raw transactions from a statement source.
 * <p>
 * Raw transactions keep the original structure of a specific bank.
 * They are later mapped to the common ParsedTransaction model.
 *
 * @param <S> statement source type
 * @param <R> bank-specific raw transaction type
 */
public interface RawTransactionParser<S extends StatementSource, R extends BankRawTransaction> {

    /**
     * Parses raw transactions from the statement source.
     * <p>
     * Statement period is passed because some banks may omit year in transaction dates,
     * so the parser may need statement-level context.
     *
     * @param source bank statement source
     * @param period parsed statement period
     * @return list of bank-specific raw transactions
     */
    List<R> parse(S source, StatementPeriod period);
}