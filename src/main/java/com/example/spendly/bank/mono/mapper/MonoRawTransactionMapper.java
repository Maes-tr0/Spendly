package com.example.spendly.bank.mono.mapper;

import com.example.spendly.bank.common.mapper.RawTransactionMapper;
import com.example.spendly.bank.mono.model.MonoRawTransaction;
import com.example.spendly.statement.model.ParsedTransaction;
import org.springframework.stereotype.Component;


@Component
public class MonoRawTransactionMapper implements RawTransactionMapper<MonoRawTransaction> {

    @Override
    public ParsedTransaction toParsedTransaction(MonoRawTransaction rawTransaction) {
        return null;
    }
}