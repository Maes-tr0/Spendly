package com.example.spendly.statement.model;

import com.example.spendly.bank.common.model.BankCode;

import java.util.List;

public record ParsedStatement(
        BankCode bankCode,
        StatementPeriod period,
        StatementBalanceSummary balanceSummary,
        List<ParsedTransaction> transactions
) {
}