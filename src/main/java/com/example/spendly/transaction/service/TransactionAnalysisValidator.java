package com.example.spendly.transaction.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TransactionAnalysisValidator {

    public void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new IllegalArgumentException("dateFrom is required");
        }

        if (to == null) {
            throw new IllegalArgumentException("dateTo is required");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }
    }
}