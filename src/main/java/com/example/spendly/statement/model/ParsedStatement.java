package com.example.spendly.statement.model;

import com.example.spendly.bank.common.model.ParsedTransaction;

import java.util.List;

public record ParsedStatement(
        List<ParsedTransaction> transactions
) {
}
