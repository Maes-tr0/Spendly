package com.example.spendly.statement.validation;

import com.example.spendly.statement.model.ParsedStatement;
import com.example.spendly.statement.model.StatementPeriod;
import com.example.spendly.statement.validation.exception.ParsedStatementValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SameStatementPeriodValidator implements ParsedStatementValidator {

    @Override
    public void validate(List<ParsedStatement> statements) {
        if (statements == null || statements.isEmpty()) {
            throw new ParsedStatementValidationException("Statements list must not be empty");
        }

        StatementPeriod expectedPeriod = statements.getFirst().period();

        for (ParsedStatement statement : statements) {
            if (!expectedPeriod.equals(statement.period())) {
                throw new ParsedStatementValidationException(
                        "All parsed statements must have the same statement period"
                );
            }
        }
    }
}