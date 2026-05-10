package com.example.spendly.statement.validation;

import com.example.spendly.statement.model.ParsedStatement;

import java.util.List;

public interface ParsedStatementValidator {
    void validate(List<ParsedStatement> statements);
}