package com.example.spendly.bank.privat.parser;

import com.example.spendly.bank.common.parser.RawTransactionParser;
import com.example.spendly.bank.common.source.TableStatementSource;
import com.example.spendly.bank.privat.model.PrivatRawTransaction;
import com.example.spendly.statement.model.StatementPeriod;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PrivatRawTransactionParser implements RawTransactionParser<TableStatementSource, PrivatRawTransaction> {

    private static final int FIRST_TRANSACTION_ROW_INDEX = 2;

    private static final int TRANSACTION_DATE_TIME_COLUMN = 0;
    private static final int BANK_CATEGORY_NAME_COLUMN = 1;
    private static final int MASKED_CARD_NUMBER_COLUMN = 2;
    private static final int DESCRIPTION_COLUMN = 3;
    private static final int CARD_AMOUNT_COLUMN = 4;
    private static final int CARD_CURRENCY_COLUMN = 5;
    private static final int OPERATION_AMOUNT_COLUMN = 6;
    private static final int OPERATION_CURRENCY_COLUMN = 7;
    private static final int BALANCE_AFTER_TRANSACTION_COLUMN = 8;
    private static final int BALANCE_CURRENCY_COLUMN = 9;

    @Override
    public List<PrivatRawTransaction> parse(TableStatementSource source, StatementPeriod period) {
        List<PrivatRawTransaction> transactions = new ArrayList<>();

        for (int i = FIRST_TRANSACTION_ROW_INDEX; i < source.rowCount(); i++) {
            String transactionDateTime = source.getValue(i, TRANSACTION_DATE_TIME_COLUMN);

            if (transactionDateTime.isBlank()) {
                continue;
            }

            transactions.add(mapRowToPrivatRawTransaction(source, i));
        }

        return transactions;
    }

    private PrivatRawTransaction mapRowToPrivatRawTransaction(
            TableStatementSource source,
            int rowIndex
    ) {
        return new PrivatRawTransaction(
                source.getValue(rowIndex, TRANSACTION_DATE_TIME_COLUMN),
                source.getValue(rowIndex, BANK_CATEGORY_NAME_COLUMN),
                source.getValue(rowIndex, MASKED_CARD_NUMBER_COLUMN),
                source.getValue(rowIndex, DESCRIPTION_COLUMN),

                source.getValue(rowIndex, CARD_AMOUNT_COLUMN),
                source.getValue(rowIndex, CARD_CURRENCY_COLUMN),

                source.getValue(rowIndex, OPERATION_AMOUNT_COLUMN),
                source.getValue(rowIndex, OPERATION_CURRENCY_COLUMN),

                source.getValue(rowIndex, BALANCE_AFTER_TRANSACTION_COLUMN),
                source.getValue(rowIndex, BALANCE_CURRENCY_COLUMN)
        );
    }
}