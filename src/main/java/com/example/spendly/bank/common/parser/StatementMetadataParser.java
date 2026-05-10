package com.example.spendly.bank.common.parser;

import com.example.spendly.bank.common.source.StatementSource;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;

/**
 * Contract for extracting statement-level metadata from a bank statement source.
 * <p>
 * Metadata belongs to the whole statement, not to a single transaction.
 *
 * @param <S> statement source type
 */
public interface StatementMetadataParser<S extends StatementSource> {

    /**
     * Parses the statement period from the source.
     *
     * @param source bank statement source
     * @return period covered by the statement
     */
    StatementPeriod parsePeriod(S source);

    /**
     * Parses balance-related information from the source.
     *
     * @param source bank statement source
     * @return opening balance, closing balance and account currency
     */
    StatementBalanceSummary parseBalanceSummary(S source);
}