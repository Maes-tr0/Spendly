package com.example.spendly.statement.model;

import java.time.LocalDate;

public record StatementPeriod(
        LocalDate from,
        LocalDate to
) {
}