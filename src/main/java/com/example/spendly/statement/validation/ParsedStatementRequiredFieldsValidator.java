package com.example.spendly.statement.validation;

import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.validation.exception.ParsedStatementValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParsedStatementRequiredFieldsValidator implements ParsedStatementValidator {

    @Override
    public void validate(List<ParsedStatement> statements) {
        if (statements == null || statements.isEmpty()) {
            throw new ParsedStatementValidationException("Statements list must not be empty");
        }

        for (int i = 0; i < statements.size(); i++) {
            validateStatement(statements.get(i), i);
        }
    }

    private void validateStatement(ParsedStatement statement, int index) {
        if (statement == null) {
            throw new ParsedStatementValidationException("Parsed statement at index " + index + " must not be null");
        }

        if (statement.bankCode() == null) {
            throw new ParsedStatementValidationException("Bank code must not be null for statement at index " + index);
        }

        validatePeriod(statement, index);
        validateBalanceSummary(statement, index);
        validateTransactions(statement, index);
    }

    private void validatePeriod(ParsedStatement statement, int index) {
        if (statement.period() == null) {
            throw new ParsedStatementValidationException("Statement period must not be null for statement at index " + index);
        }

        if (statement.period().from() == null) {
            throw new ParsedStatementValidationException("Statement period from date must not be null for statement at index " + index);
        }

        if (statement.period().to() == null) {
            throw new ParsedStatementValidationException("Statement period to date must not be null for statement at index " + index);
        }

        if (statement.period().from().isAfter(statement.period().to())) {
            throw new ParsedStatementValidationException("Statement period from date must not be after to date for statement at index " + index);
        }
    }

    private void validateBalanceSummary(ParsedStatement statement, int index) {
        if (statement.balanceSummary() == null) {
            throw new ParsedStatementValidationException("Statement balance summary must not be null for statement at index " + index);
        }

        if (statement.balanceSummary().openingBalance() == null) {
            throw new ParsedStatementValidationException("Opening balance must not be null for statement at index " + index);
        }

        if (statement.balanceSummary().closingBalance() == null) {
            throw new ParsedStatementValidationException("Closing balance must not be null for statement at index " + index);
        }

        if (statement.balanceSummary().accountCurrency() == null) {
            throw new ParsedStatementValidationException("Account currency must not be null for statement at index " + index);
        }
    }

    private void validateTransactions(ParsedStatement statement, int index) {
        if (statement.transactions() == null || statement.transactions().isEmpty()) {
            throw new ParsedStatementValidationException("Statement transactions must not be empty for statement at index " + index);
        }
    }
}