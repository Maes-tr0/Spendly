package com.example.spendly.transaction.model.request;

import java.util.List;

public record TransactionAnalysisRequest(
        List<String> bankCodes,
        String dateFrom,
        String dateTo,
        String targetCurrency
) {
}
