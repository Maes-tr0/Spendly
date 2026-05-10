package com.example.spendly.statement.validation;

import com.example.spendly.statement.model.ParsedStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParsedStatementValidationService {

    private final List<ParsedStatementValidator> validators;

    public void validate(List<ParsedStatement> statements) {
        for (ParsedStatementValidator validator : validators) {
            validator.validate(statements);
        }
    }
}