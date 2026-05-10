package com.example.spendly.bank.common.mapper;

import com.example.spendly.bank.common.model.BankCode;
import com.example.spendly.bank.common.model.BankRawTransaction;
import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.ParsedTransaction;
import com.example.spendly.statement.model.StatementBalanceSummary;
import com.example.spendly.statement.model.StatementPeriod;

import java.util.List;

/**
 * Contract for mapping bank-specific raw transactions to common parsed transaction models.
 *
 * @param <R> bank-specific raw transaction type
 */
public interface RawTransactionMapper<R extends BankRawTransaction> {

    /**
     * Converts one bank-specific raw transaction into a common parsed transaction.
     *
     * @param rawTransaction bank-specific raw transaction
     * @return normalized parsed transaction
     */
    ParsedTransaction toParsedTransaction(R rawTransaction);

    /**
     * Converts raw transactions and statement-level metadata into a parsed statement.
     *
     * @param bankCode        code of the bank that produced the statement
     * @param period          statement period
     * @param balanceSummary  statement balance summary
     * @param rawTransactions bank-specific raw transactions
     * @return parsed statement with normalized transactions
     */
    default ParsedStatement toParsedStatement(
            BankCode bankCode,
            StatementPeriod period,
            StatementBalanceSummary balanceSummary,
            List<R> rawTransactions
    ) {
        List<ParsedTransaction> parsedTransactions = rawTransactions.stream()
                .map(this::toParsedTransaction)
                .toList();

        return new ParsedStatement(
                bankCode,
                period,
                balanceSummary,
                parsedTransactions
        );
    }
}